package au.org.biodiversity.nsl

import grails.transaction.Transactional
import groovy.sql.Sql
import groovy.transform.Synchronized
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import javax.sql.DataSource

@Transactional
class AdminService {

    DataSource dataSource

    private Host host

    Host preferredHost() {
        if (!host) {
            host = Host.findByPreferred(true)
        }
        return host
    }

    /**
     * Expects a list of maps in the format [s: nameSpace, o: objectType, i: idNumber, v: versionNumber, u: uri]
     * @param identifiers
     */
    void bulkAddIdentifiers(List<Map> identifiers, String username) {
        log.debug "Adding ${identifiers.size()} identifiers"
        Sql sql = Sql.newInstance(dataSource)
        int count = 0
        for (Map ident in identifiers) {
            sql.executeInsert('''INSERT INTO mapper.match (uri, updated_at, updated_by) VALUES (?,now(),?)''', [ident.u, username])
            sql.executeInsert('''INSERT INTO mapper.identifier (name_space, object_type, id_number, version_number, preferred_uri_id) 
                VALUES (?,?,?,?,(SELECT id FROM mapper.match WHERE uri = ?))''', [ident.s, ident.o, ident.i, ident.v, ident.u])
            sql.executeInsert('''INSERT INTO mapper.identifier_identities (match_id, identifier_id) 
                SELECT preferred_uri_id, id 
                  FROM mapper.identifier 
                  WHERE name_space = ? 
                        AND object_type = ? 
                        AND id_number = ? 
                        AND version_number = ?''', [ident.s, ident.o, ident.i, ident.v])
            if (++count % 1000 == 0) {
                log.debug "$count inserts done."
            }
        }
        log.debug "Inserting match hosts"
        sql.executeInsert('''INSERT INTO mapper.match_host (match_hosts_id, host_id)
  SELECT
    m.id,
    (SELECT h.id
     FROM mapper.host h
     WHERE h.preferred)
  FROM mapper.match m
  WHERE
    NOT exists(SELECT 1
               FROM mapper.match_host mh
               WHERE mh.match_hosts_id = m.id)''')
    }

    @Synchronized
    Identifier addIdentifier(String nameSpace, String objectType, Long idNumber, Long versionNumber, String uri) {
        Identifier identifier = new Identifier(nameSpace: nameSpace, objectType: objectType, idNumber: idNumber, versionNumber: versionNumber)
        Match match = new Match(uri: uri ?: identifier.toUrn())
        identifier.addToIdentities(match)
        identifier.preferredUri = match
        match.addToIdentifiers(identifier)
        if (identifier.validate() && match.validate()) {
            identifier.save()
            Host host = preferredHost()
            if (host) {
                match.addToHosts(host)
                match.save()
            }
            return identifier
        } else {
            List<String> errors = ["Error trying to add identifier nameSpace: $nameSpace, objectType: $objectType, idNumber: $idNumber, versionNumber: $versionNumber".toString()]
            if (identifier.errors.hasErrors()) {
                errors.add('Identifier: ' + buildErrorString(identifier.errors))
            }
            if (match.errors.hasErrors()) {
                errors.add('URI: ' + buildErrorString(match.errors))
            }
            throw new ServiceException(errors.join(', \n'))
        }
    }

    @Synchronized
    void bulkRemoveIdentifiers(List<Map> identifiers) {
        log.debug "Removing ${identifiers.size()} identifiers"
        Sql sql = Sql.newInstance(dataSource)
        int count = 0

        sql.execute('''
CREATE TABLE mapper.bulk_remove (
        identifier_id INT8 NOT NULL,
        match_id INT8 NOT NULL ,
        PRIMARY KEY (identifier_id)
    );''')

        for (Map ident in identifiers) {
            sql.execute('''
INSERT INTO mapper.bulk_remove 
  SELECT ii.identifier_id, ii.match_id FROM mapper.identifier i JOIN mapper.identifier_identities ii ON i.id = ii.identifier_id
        WHERE i.id_number = ? 
          AND i.object_type = ?
          AND i.version_number = ?
          AND i.name_space = ?''', [ident.i, ident.o, ident.v, ident.s])
            if (++count % 1000 == 0) {
                log.debug "Found $count identifiers to remove."
            }
        }
        log.debug "Removing identifiers"
        sql.execute('''
DELETE FROM mapper.identifier_identities ii WHERE identifier_id IN (SELECT DISTINCT (identifier_id) FROM mapper.bulk_remove);
DELETE FROM mapper.identifier i WHERE i.id IN (SELECT DISTINCT (identifier_id) FROM mapper.bulk_remove);
''')
        log.debug "Finding orphaned matches"
        sql.execute('''
DELETE FROM mapper.bulk_remove r WHERE exists(SELECT 1 FROM mapper.identifier_identities ii WHERE ii.match_id = r.match_id);
DELETE FROM mapper.bulk_remove r WHERE exists(SELECT 1 FROM mapper.identifier i WHERE i.preferred_uri_id = r.match_id);
''')
        log.debug "Removing hosts"
        sql.execute('''
DELETE FROM mapper.match_host mh using mapper.bulk_remove where mh.match_hosts_id = match_id;
''')
        log.debug "Removing orphaned matches"
        sql.execute('''
DELETE FROM mapper.match m using mapper.bulk_remove where m.id = match_id;
        ''')
        log.debug "Dropping temp table."
        sql.execute('''
TRUNCATE mapper.bulk_remove;
DROP TABLE IF EXISTS mapper.bulk_remove;
''')
        sql.commit()
        log.debug "Removed ${identifiers.size()} identifiers and orphaned matches."
    }

    /**
     * This permanently removes an identifier and it's exclusive Matches. It can't be undone and if someone tries to
     * resolve a match on this identity they will get a 404 - this is meant for draft identities only
     * @param identifier
     */
    @Synchronized
    void removeIdentifier(Identifier identifier) {
        identifier.identities.each { Match match ->
            identifier.removeFromIdentities(match)
            match.removeFromIdentifiers(identifier)
            if (match.identifiers.size() == 0) {
                log.debug "$match has ${match.identifiers.size()} identifiers so deleting"
                match.hosts.each { Host host ->
                    match.removeFromHosts(host)
                }
                match.delete()
            }
        }
        identifier.preferredUri = null
        identifier.delete()
    }

    private static String buildErrorString(Errors errors) {
        errors.fieldErrors.collect { FieldError e ->
            "${e.field} cannot be ${e.rejectedValue}."
        }.join(', \n')
    }
}

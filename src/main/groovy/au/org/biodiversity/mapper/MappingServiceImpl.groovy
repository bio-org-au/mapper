/*
    Copyright (c) 2019 Australian National Botanic Gardens and Authors

    This file is part of National Species List project.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy
    of the License at http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package au.org.biodiversity.mapper

import groovy.sql.GroovyResultSet
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Async

import javax.inject.Inject
import javax.inject.Singleton
import javax.sql.DataSource

/**
 * User: pmcneil
 * Date: 4/9/19
 *
 */
@Slf4j
@CompileStatic
@Singleton
@Requires(beans = DataSource.class)
class MappingServiceImpl implements MappingService {

    @Inject
    DataSource dataSource

    @Property(name = 'mapper.default-protocol')
    String defaultProtocol
    @Property(name = 'mapper.resolver-url')
    String resolverUrl

    /**
     * Returns a tuple of the match and the identifier.
     * NOTE: ignore the ID of the Identifier and Match objects here
     * @param uri
     * @return
     */
    Tuple2<Identifier, Match> getMatchIdentity(String uri) {
        log.info "URI Passed to match from db -> $uri"
        withSql { Sql sql ->
            GroovyRowResult row = sql.firstRow('''
            select m.id               as m_id,
                   m.uri              as m_uri,
                   m.deprecated       as m_deprecated,
                   m.updated_at       as m_updated_at,
                   m.updated_by       as m_updated_by,
                   i.id               as i_id,
                   i.id_number        as i_id_number,
                   i.name_space       as i_name_space,
                   i.object_type      as i_object_type,
                   i.deleted          as i_deleted,
                   i.reason_deleted   as i_reason_deleted,
                   i.updated_at       as i_updated_at,
                   i.updated_by       as i_updated_by,
                   i.preferred_uri_id as i_preferred_uri_id,
                   i.version_number   as i_version_number
            from mapper.match m
                     join mapper.identifier_identities ii on m.id = ii.match_id
                     join mapper.identifier i on ii.identifier_id = i.id
            where uri = :uri''', [uri: uri])
            if (row) {
                Identifier identifier = new Identifier(this, row)
                Match match = new Match(row)
                return new Tuple2<Identifier, Match>(identifier, match)
            }
            return null
        }
    }

    List<Identifier> getMatchIdentities(String path) {
        List<Identifier> identifiers = []
        Boolean matchFound = false
        withSql { Sql sql ->
            sql.eachRow('''
            select m.id as m_id,
                   i.id               as i_id,
                   i.id_number        as i_id_number,
                   i.name_space       as i_name_space,
                   i.object_type      as i_object_type,
                   i.deleted          as i_deleted,
                   i.reason_deleted   as i_reason_deleted,
                   i.updated_at       as i_updated_at,
                   i.updated_by       as i_updated_by,
                   i.preferred_uri_id as i_preferred_uri_id,
                   i.version_number   as i_version_number
            from mapper.match m
                     left outer join mapper.identifier_identities ii on m.id = ii.match_id
                     left outer join mapper.identifier i on ii.identifier_id = i.id
            where uri = :path''', [path: path]) { GroovyResultSet row ->
                matchFound = (matchFound || row.getLong("m_id") != 0)
                if (row["i_id"]) {
                    identifiers.add(new Identifier(this, row))
                }
            }
        }
        return (matchFound ? identifiers : null)
    }

    /**
     * Gets the preferred host String
     * @return the preferred host string e.g. http://id.biodiversity.org.au
     */
    String getPreferredHost() {
        withSql { Sql sql ->
            GroovyRowResult row = sql.firstRow('select * from mapper.host where preferred = true')
            if (row) {
                return "${defaultProtocol}://${row.host_name}"
            }
            return resolverUrl
        }
    }

    /**
     * Get the preferred link for the given identifier determined by name space object type and id
     *
     * Note this shouldn't be used for versioned resources
     *
     * @param nameSpace
     * @param objectType
     * @param idNumber
     * @return String link or null
     */
    String getPreferredLink(String nameSpace, String objectType, Long idNumber) {
        withSql { Sql sql ->
            GroovyRowResult row = sql.firstRow('''
            select h.host_name || '/' || m.uri as uri
            from mapper.identifier i 
                join mapper.match m on i.preferred_uri_id = m.id
                join mapper.match_host mh on m.id = mh.match_hosts_id
                join mapper.host h on mh.host_id = h.id
            where name_space = :nameSpace and object_type = :objectType and id_number = :idNumber
            order by h.preferred desc''',
                    [nameSpace: nameSpace, objectType: objectType, idNumber: idNumber])
            return row ? "${defaultProtocol}://${row.uri}" : null
        }
    }

    /**
     * get the list of links associated with an identifier.
     * @param nameSpace
     * @param objectType
     * @param idNumber
     * @return a List of Maps containing [link, resourceCount, preferred, deprecated, deleted]
     */
    List<LinkResult> getlinks(String nameSpace, String objectType, long idNumber) {
        List<LinkResult> links = []
        withSql { Sql sql ->
            sql.eachRow('''
            select host.host_name || '/' || m.uri as link,
                   (select count(*) from mapper.identifier_identities where match_id = m.id) as resourceCount,
                   (m.id = i.preferred_uri_id and host.preferred) as preferred,
                   m.deprecated,
                   i.deleted
            from mapper.identifier i 
                join mapper.identifier_identities ii on i.id = ii.identifier_id
                join mapper.match m on ii.match_id = m.id
                join mapper.match_host mh on m.id = mh.match_hosts_id
                join mapper.host on mh.host_id = host.id
            where name_space = :nameSpace and object_type = :objectType and id_number = :idNumber
            order by preferred desc, resourceCount''',
                    [nameSpace: nameSpace, objectType: objectType, idNumber: idNumber]) { GroovyResultSet row ->
                links.add(new LinkResult(row, defaultProtocol))
            }
        }
        return links
    }

    Identifier findIdentifier(String nameSpace, String objectType, Long idNumber, Long versionNumber) {
        String query = '''select 
                    i.id               as i_id,
                    i.id_number        as i_id_number,
                    i.name_space       as i_name_space,
                    i.object_type      as i_object_type,
                    i.deleted          as i_deleted,
                    i.reason_deleted   as i_reason_deleted,
                    i.updated_at       as i_updated_at,
                    i.updated_by       as i_updated_by,
                    i.preferred_uri_id as i_preferred_uri_id,
                    i.version_number   as i_version_number
            from mapper.identifier i 
            where name_space = :nameSpace 
              and object_type = :objectType 
              and id_number = :idNumber
              '''
        if (versionNumber) {
            query += 'and version_number = :versionNumber'
        } else {
            query += 'and version_number is null'
        }
        withSql { Sql sql ->
            GroovyRowResult row = sql.firstRow(query,
                    [nameSpace: nameSpace, objectType: objectType, idNumber: idNumber, versionNumber: versionNumber])
            return row ? new Identifier(this, row) : null
        }
    }

    /**
     * Gets a Match by ID
     * @param id
     * @return a Match object
     */
    Match getMatch(Long id) {
        withSql { Sql sql ->
            GroovyRowResult row = sql.firstRow('''
            select m.id               as m_id,
                   m.uri              as m_uri,
                   m.deprecated       as m_deprecated,
                   m.updated_at       as m_updated_at,
                   m.updated_by       as m_updated_by
            from mapper.match m where id = :id''', [id: id])
            return row ? new Match(row) : null
        }
    }

    /**
     * Finds a Match by uri
     * @param uri String
     * @return a Match object
     */
    Match findMatch(String uri) {
        withSql { Sql sql ->
            GroovyRowResult row = sql.firstRow('''
            select m.id               as m_id,
                   m.uri              as m_uri,
                   m.deprecated       as m_deprecated,
                   m.updated_at       as m_updated_at,
                   m.updated_by       as m_updated_by
            from mapper.match m where uri = :uri''', [uri: uri])
            return row ? new Match(row) : null
        }
    }

    Identifier addIdentifier(String nameSpace, String objectType, Long idNumber, Long versionNumber, String uri, String userName) {
        Identifier exists = findIdentifier(nameSpace, objectType, idNumber, versionNumber)
        if (exists) {
            return exists
        }

        if (uri) {
            Match existingMatch = findMatch(uri)
            if (existingMatch) {
                // don't need to check identity matches since previous search should have found it
                throw new MatchExistsException("URI $uri already exists")
            }
        } else {
            uri = defaultUri(nameSpace, objectType, idNumber, versionNumber)
        }

        // do the inserts
        withSql { Sql sql ->
            sql.withTransaction {
                List<List<Object>> m = sql.executeInsert('''Insert into mapper.match
                (uri, deprecated, updated_at, updated_by) VALUES (:uri, false, now(), :userName)''',
                        [uri: uri, userName: userName]
                )
                log.info m as String
                Long matchId = m[0][0] as Long
                sql.executeInsert('''insert into mapper.match_host (match_hosts_id, host_id) 
                    VALUES (:matchId, (select id from mapper.host where preferred limit 1))''', [matchId: matchId])
                List<List<Object>> i = sql.executeInsert('''Insert into mapper.identifier
                (id_number, name_space, object_type, deleted, reason_deleted, updated_at, updated_by, preferred_uri_id, version_number)
                values (:idNumber, :nameSpace, :objectType, false, null, now(), :userName, :matchId, :versionNumber)
                 ''', [idNumber: idNumber, nameSpace: nameSpace, objectType: objectType,
                       userName: userName, matchId: matchId, versionNumber: versionNumber])
                sql.executeInsert('''insert into mapper.identifier_identities (match_id, identifier_id) 
                    VALUES (:matchId, :identifierId)''', [matchId: matchId, identifierId: i[0][0] as Long])
            }
        }
        return findIdentifier(nameSpace, objectType, idNumber, versionNumber)
    }

    List<Match> getMatches(Long identifierId) {
        List<Match> matches = []
        withSql { Sql sql ->
            sql.eachRow('''select m.* from mapper.identifier_identities ii 
                join mapper.match m on ii.match_id = m.id
                where ii.identifier_id = :identifierId''', [identifierId: identifierId]) { row ->
                matches.add(new Match(row, ''))
            }
        }
        return matches
    }

    /**
     * This permanently removes an identifier and it's exclusive Matches. It can't be undone and if someone tries to
     * resolve a match on this identity they will get a 404 - this is meant for draft identities only
     * @param identifier
     */
    @Synchronized
    Boolean removeIdentifier(Identifier identifier) {
        //remove the identifier from identifier_identities
        //delete the identifier
        log.info("Removing identifier ${identifier.toString()}")
        Boolean success = false
        withSql { Sql sql ->
            sql.withTransaction {
                sql.execute('''
                    delete from mapper.identifier_identities ii where identifier_id = :identifierID;
                    delete from mapper.identifier i where id = :identifierID;''', [identifierID: identifier.id])
                success = true
            }
        }
        if (success) {
            log.info("Successfully Removed identifier: ${identifier.toString()}")
            cleanupOrphanMatch()
        }
        return success
    }

    @Async
    void cleanupOrphanMatch() {
        log.info "cleaning up orphans"
        withSql { Sql sql ->
            sql.withTransaction {
                sql.execute('''
                    create temp table orphanMatch on commit drop as select id from mapper.match m
                    where not exists(select 1 from mapper.identifier_identities where match_id = m.id)
                        and not exists(select 1 from mapper.identifier where preferred_uri_id = m.id);
                    delete from mapper.match_host where match_hosts_id in (select id from orphanMatch);
                    delete from mapper.match where id in (select id from orphanMatch);''')
            }
            log.info "clean up orphans transaction complete"
        }
    }

/**
 * Add a list of Identifiers with uri's.
 *
 * @param identifiers , a list of maps in the format [s: nameSpace, o: objectType, i: idNumber, v: versionNumber, u: uri]
 * @param username
 */
    Boolean bulkAddIdentifiers(Collection<Map> identifiers, String username) {
        log.info "Adding ${identifiers.size()} identifiers"
        withSql { Sql sql ->
            Boolean success = false
            sql.withTransaction {
                sql.execute('''
                    create temp table bulkInsert
                        (
                            m_id             bigint default nextval('mapper.mapper_sequence'::regclass) not null,
                            m_uri            varchar(255)                                               not null,
                            i_id             bigint default nextval('mapper.mapper_sequence'::regclass) not null,
                            i_id_number      bigint                                                     not null,
                            i_name_space     varchar(255)                                               not null,
                            i_object_type    varchar(255)                                               not null,
                            i_version_number bigint
                        ) on commit drop''')
                log.info "inserting into temp bulk"
                sql.execute(insertBulkTempTable(identifiers, username))

                log.info "Creating identifier, matches, and linking."
                sql.execute('''
                    insert into mapper.match (id, uri, deprecated, updated_at, updated_by) select m_id, m_uri, false, now(), 'test' from bulkInsert;
                    insert into mapper.identifier (id, id_number, name_space, object_type, version_number, preferred_uri_id, deleted, reason_deleted, updated_at, updated_by)
                    select i_id, i_id_number, i_name_space, i_object_type, i_version_number, m_id, false, null, now(), 'test' from bulkInsert;
                    insert into mapper.identifier_identities (match_id, identifier_id) (select m_id, i_id from bulkInsert);
                    insert into mapper.match_host (match_hosts_id, host_id) SELECT m_id,ph.id from bulkInsert, (SELECT h.id FROM mapper.host h WHERE h.preferred) ph;''')
                success = true
                log.info "Creating identifier, matches, and linking -> Done"
            }
            return success
        }
    }

    private static String insertBulkTempTable(Collection<Map> identifiers, String username) {
        List<String> insert = []
        log.info "Start making temp bulk insert"
        for (Map ident in identifiers) {
            insert.add("('${ident.u}', '${ident.s}', '${ident.o}', ${ident.i}, ${ident.v})".toString())
        }
        String stmt = 'INSERT INTO bulkInsert (m_uri, i_name_space, i_object_type, i_id_number, i_version_number) VALUES ' + insert.join(',')
        log.info "Finished making temp bulk insert"
        return stmt
    }

    /**
     * permanently remove a set of identifiers, for example when you delete a draft tree.
     * @param identifiers : a list of maps in the format [s: nameSpace, o: objectType, i: idNumber, v: versionNumber]
     */
    @Synchronized
    Boolean bulkRemoveIdentifiers(Collection<Map> identifiers) {
        log.info "Start removing ${identifiers.size()} identifiers"
        Boolean success = false
        withSql { Sql sql ->
            sql.withTransaction {
                int count = 0

                sql.execute('''
                    create temp table bulkRemove
                    (
                        identifier_id  INT8,
                        match_id       INT8,
                        id_number      bigint       not null,
                        name_space     varchar(255) not null,
                        object_type    varchar(255) not null,
                        version_number bigint
                    ) on commit drop;''')
                log.info "inserting into temp bulkRemove"
                sql.execute(insertBulkRemoveTable(identifiers))

                log.debug "Removing identifiers"
                sql.execute('''
                    update bulkRemove br set identifier_id = i.id, match_id = ii.match_id
                    from mapper.identifier i
                        JOIN mapper.identifier_identities ii ON i.id = ii.identifier_id
                    WHERE i.id_number = br.id_number
                      AND i.object_type = br.object_type
                      AND i.version_number = br.version_number
                      AND i.name_space = br.name_space;
                    DELETE FROM mapper.identifier_identities ii WHERE identifier_id IN (SELECT DISTINCT (identifier_id) FROM bulkRemove);
                    DELETE FROM mapper.identifier i WHERE i.id IN (SELECT DISTINCT (identifier_id) FROM bulkRemove);''')
                success = true
            }
        }
        cleanupOrphanMatch()
        log.info "Finish removing ${identifiers.size()} identifiers"
        return success
    }

    private static String insertBulkRemoveTable(Collection<Map> identifiers) {
        List<String> insert = []
        log.info "Start making temp bulk remove insert"
        for (Map ident in identifiers) {
            insert.add("('${ident.s}', '${ident.o}', ${ident.i}, ${ident.v})".toString())
        }
        String stmt = 'INSERT INTO bulkRemove (name_space, object_type, id_number, version_number) VALUES ' + insert.join(',')
        log.info "Finished making temp bulk remove insert"
        return stmt
    }

    @Override
    Host addHost(String hostName) {
        withSql { Sql sql ->
            Host newHost = findHost(hostName, sql)
            if (!newHost) {
                sql.withTransaction {
                    List<List<Object>> h = sql.executeInsert('''insert into mapper.host (host_name, preferred) values  (:hostName, false)''', [hostName: hostName])
                    newHost = new Host(h[0])
                }
            }
            return newHost
        }
    }

    /**
     * sets the host with the hostname as the preferred host and all others as not. There can only be one.
     * throws a NotFoundException if the host doesn't exist.
     * @param hostName
     * @return Host
     */
    @Override
    Host setPreferredHost(String hostName) {
        withSql { Sql sql ->
            Host host = findHost(hostName, sql)
            if (!host) {
                throw new NotFoundException("Host $hostName not found")
            }
            if (host.preferred) {
                return host
            }
            sql.withTransaction {
                sql.execute('''
                    update mapper.host set preferred = false where preferred;
                    update mapper.host set preferred = true where host_name = :hostName''', [hostName: hostName])
            }
            return findHost(hostName, sql) //get it from the DB
        }
    }

    @Override
    Boolean addUriToIdentifier(Identifier identifier, Match match, Boolean setAsPreferred) {
        Match existing = getMatches(identifier.id).find { it.id == match.id }
        if (existing) {
            if (setAsPreferred && identifier.preferredUriID != match.id) {
                withSql { Sql sql ->
                    sql.withTransaction {
                        sql.executeUpdate('update mapper.identifier set preferred_uri_id = :mId where id = :iId',
                                [iId: identifier.id, mId: match.id])
                    }
                }
            }
            return true
        }
        Boolean success = false
        withSql { Sql sql ->
            sql.withTransaction {
                sql.executeInsert('insert into mapper.identifier_identities (match_id, identifier_id) values (:matchId, :identifierId)', [matchId: match.id, identifierId: identifier.id])
                if (setAsPreferred) {
                    sql.executeUpdate('update mapper.identifier set preferred_uri_id = :mId where id = :iId',
                            [iId: identifier.id, mId: match.id])
                }
                success = true
            }
        }
        return success
    }

    @Override
    Match addMatch(String uri, String username) {
        Match match = findMatch(uri)
        if (match) {
            return match
        }
        withSql { Sql sql ->
            sql.withTransaction {
                List<List<Object>> m = sql.executeInsert('insert into mapper.match (uri, updated_at, updated_by) values (:uri, now(), :username)',
                        [uri: uri, username: username])
                match = new Match(m[0])
                sql.executeInsert('''insert into mapper.match_host (match_hosts_id, host_id) 
                    VALUES (:matchId, (select id from mapper.host where preferred limit 1))''', [matchId: match.id])
            }
        }
        return match
    }

    Map stats() {
        Map stats = [:]
        withSql { Sql sql ->
            stats.identifiers = sql.firstRow('select count(1) c from mapper.identifier')?."c"
            stats.matches = sql.firstRow('select count(1) c from mapper.match')?."c"
            stats.hosts = sql.firstRow('select count(1) c from mapper.host')?."c"
            stats.orphanMatch = sql.firstRow('''select count(id) c from mapper.match m
            where not exists(select 1 from mapper.identifier_identities where match_id = m.id)
            and not exists(select 1 from mapper.identifier where preferred_uri_id = m.id)''')?."c"
            stats.orphanIdentifier = sql.firstRow('''select count(id) c from mapper.identifier i
            where not exists(select 1 from mapper.identifier_identities where identifier_id = i.id)
            and (preferred_uri_id is null or not exists(select 1 from mapper.match m where i.preferred_uri_id = m.id))''')?."c"
        }
        log.info "Stats: $stats"
        return stats
    }

    Boolean moveUris(Identifier from, Identifier to) {
        Boolean success = false
        withSql { Sql sql ->
            sql.withTransaction {
                // 1. Delete records for 'from' that already exist for 'to' to prevent unique constraint violations
                sql.executeUpdate('''
                DELETE FROM mapper.identifier_identities 
                WHERE identifier_id = :fromId 
                AND match_id IN (
                    SELECT match_id FROM mapper.identifier_identities WHERE identifier_id = :toId
                )
            ''', [fromId: from.id, toId: to.id])
                // 2. Now update the remaining records that don't have a match_id conflict
                sql.executeUpdate('''
                UPDATE mapper.identifier_identities 
                SET identifier_id = :toId 
                WHERE identifier_id = :fromId
            ''', [toId: to.id, fromId: from.id])
                success = true
            }
        }
        return success
    }

    @Override
    Boolean removeIdentityFromUri(Match match, Identifier identifier) {
        Boolean success = false
        withSql { Sql sql ->
            sql.withTransaction {
                sql.execute('''delete from mapper.identifier_identities ii where identifier_id = :identifierId and match_id = :matchId''',
                        [identifierId: identifier.id, matchId: match.id])
                if (identifier.preferredUriID == match.id) {
                    sql.executeUpdate('update mapper.identifier set preferred_uri_id = null where id = :identifierId',
                            [identifierId: identifier.id])
                }
                success = true
            }
        }
        return success
    }

    @Override
    Host getHost(Match match) {
        withSql { Sql sql ->
            GroovyRowResult row = sql.firstRow('''
                select h.* from mapper.match_host mh 
                    join mapper.host h on mh.host_id = h.id 
                where mh.match_hosts_id = :matchId''', [matchId: match.id])
            return row ? new Host(row, '') : null
        }
    }

    @Override
    Identifier getIdentifier(Long id) {
        withSql { Sql sql ->
            GroovyRowResult row = sql.firstRow('select * from mapper.identifier where id = :id', [id: id])
            return new Identifier(this, row, '')
        }
    }

    @Override
    Identifier deleteIdentifier(Identifier identifier, String reason) {
        if(!reason) {
            throw new IllegalArgumentException("Reason cannot be null or blank.")
        }
        withSql {Sql sql ->
            sql.withTransaction {
                sql.executeUpdate('update mapper.identifier set deleted = true, reason_deleted = :reason where id = :id',
                [reason: reason, id: identifier.id])
            }
            return getIdentifier(identifier.id)
        }
    }

// *** private ***

    static Host findHost(String hostName, Sql sql) {
        GroovyRowResult row = sql.firstRow('select * from mapper.host where host_name = :hostName', [hostName: hostName])
        if (row) {
            return new Host(row, '')
        }
        return null
    }

    private static String defaultUri(String nameSpace, String objectType, Long idNumber, Long versionNumber) {
        if (versionNumber) {
            return "$objectType/$versionNumber/$idNumber"
        }
        return "$objectType/$nameSpace/$idNumber"
    }

    @SuppressWarnings("GrUnnecessaryPublicModifier")
    public <T> T withSql(Closure<T> work) {
        Sql sql = Sql.newInstance(dataSource)
        try {
            return work(sql)
        } finally {
            sql.close()
        }
    }
}


package au.org.biodiversity.nsl

import grails.transaction.Transactional
import groovy.sql.Sql
import org.apache.shiro.authz.annotation.RequiresRoles
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import javax.servlet.ServletOutputStream

import static org.springframework.http.HttpStatus.NOT_FOUND

@Transactional
class AdminController {

    @SuppressWarnings("GroovyUnusedDeclaration")
    static responseFormats = ['json']

    private static Sql getNSL() {
        Sql.newInstance('jdbc:postgresql://localhost:5432/nsl', 'nsldev', 'nsldev', 'org.postgresql.Driver')
    }


    @RequiresRoles('admin')
    def index() {
        Map stats = [:]
        stats.resources = Identifier.count()
        stats.uris = Match.count()
        stats.nameSpaces = Identifier.executeQuery("select distinct(nameSpace) from Identifier")
        stats.objectTypes = Identifier.executeQuery("select distinct(objectType) from Identifier")
        [stats: stats]
    }

    @RequiresRoles('admin')
    def addIdentifier(String nameSpace, String objectType, Long idNumber) {
        log.debug "Add identifier $nameSpace, $objectType, $idNumber"
        Identifier exists = exists(nameSpace, objectType, idNumber)
        if (exists) {
            render(contentType: 'application/json') { [error: 'Identity already exists.', identity: exists] }
            return
        }
        Identifier identifier = new Identifier(nameSpace: nameSpace, objectType: objectType, idNumber: idNumber)
        identifier.addToIdentities(new Match(uri: identifier.toUrn()))
        if (identifier.validate()) {
            identifier.save(flush: true)
            render(contentType: 'application/json') {
                [success: 'Identity saved with default uri.', identity: identifier]
            }
        } else {
            render(contentType: 'application/json') {
                [error: 'Identity is not valid, see errors.', identity: identifier, errors: buildErrorString(identifier.errors)]
            }
        }
    }

    @RequiresRoles('admin')
    def deleteIdentifier(String nameSpace, String objectType, Long idNumber, String reason) {
        log.debug "Delete identifier $nameSpace, $objectType, $idNumber"
        Identifier identifier = exists(nameSpace, objectType, idNumber)
        if (identifier) {
            identifier.deleted = true
            identifier.reasonDeleted = reason
            identifier.save()
            render(contentType: 'application/json') {
                [success: 'Identity marked as deleted.', identity: identifier]
            }
        } else {
            render(contentType: 'application/json') { [error: 'Identity not found.'] }
        }
    }

    @RequiresRoles('admin')
    def addURI(String nameSpace, String objectType, Long idNumber, String uri) {
        Identifier identifier = exists(nameSpace, objectType, idNumber)
        if (identifier) {
            Match m = Match.findByUri(uri)
            if (m) {
                render(contentType: 'application/json') { [error: 'URI already exists.', identity: m] }
                return
            }
            Match match = new Match(uri: uri)
            identifier.addToIdentities(match)
            identifier.save()
            render(contentType: 'application/json') { [success: 'URI saved with identity.', identity: identifier] }
            return
        }
        render(contentType: 'application/json') { [error: "Identifier doesn't exist.", params: params] }
    }

    @RequiresRoles('admin')
    def addIdentityToURI(String nameSpace, String objectType, Long idNumber, String uri) {
        Identifier identifier = exists(nameSpace, objectType, idNumber)
        if (identifier) {
            Match m = Match.findByUri(uri)
            if (m) {
                identifier.addToIdentities(m)
                identifier.save()
                render(contentType: 'application/json') { [success: 'URI linked with identity.', identity: identifier] }
                return
            }
            render(contentType: 'application/json') { [error: "URI doesn't exist.", uri: uri] }
            return
        }
        render(contentType: 'application/json') { [error: "Identifier doesn't exist.", params: params] }
    }

    @RequiresRoles('admin')
    def removeIdentityFromURI(String nameSpace, String objectType, Long idNumber, String uri) {
        Identifier identifier = exists(nameSpace, objectType, idNumber)
        if (identifier) {
            Match m = Match.findByUri(uri)
            if (m) {
                identifier.removeFromIdentities(m)
                identifier.save()
                render(contentType: 'application/json') {
                    [success: 'Identity removed from URI.', identity: identifier]
                }
                return
            }
            render(contentType: 'application/json') { [error: "URI doesn't exist.", uri: uri] }
            return
        }
        render(contentType: 'application/json') { [error: "Identifier doesn't exist.", params: params] }
    }

    /**
     * move all Match (uris) from one identity to another
     * @param fromNameSpace
     * @param fromObjectType
     * @param fromIdNumber
     * @param toNameSpace
     * @param toObjectType
     * @param toIdNumber
     * @return
     */
    @RequiresRoles('admin')
    def moveIdentity(String fromNameSpace, String fromObjectType, Long fromIdNumber, String toNameSpace, String toObjectType, Long toIdNumber) {
        Identifier from = exists(fromNameSpace, fromObjectType, fromIdNumber)
        Identifier to = exists(toNameSpace, toObjectType, toIdNumber)
        if (to && from) {
            log.debug "moving identities from $from to $to"
            List<Match> fromMatches = from.identities.collect { it }
            fromMatches.each { Match match ->
                log.debug "Moving $match.uri"
                from.removeFromIdentities(match)
                to.addToIdentities(match)
            }
            to.save()
            from.save()
            render(contentType: 'application/json') {
                [success: "Identities moved.", from: from, to: to]
            }
        } else {
            log.error "To: $to or From: $from not found. $params"
            respond(status: NOT_FOUND)
        }
    }

    def identifier(String nameSpace, String objectType, Long idNumber) {
        Identifier identifier = exists(nameSpace, objectType, idNumber)
        if (identifier) {
            respond identifier
        } else {
            respond(status: NOT_FOUND)
        }
    }

    def uri(String uri) {
        Match m = Match.findByUri(uri)
        if (m) {
            respond m
        } else {
            respond(status: NOT_FOUND)
        }
    }

    @RequiresRoles('admin')
    def export() {
        Sql sql = getNSL()
        try {
            response.contentType = 'application/octet-stream'
            ServletOutputStream out = response.outputStream
            String queryStr = """select
  i.object_type || '.' || i.name_space || '.' || i.id_number || ' <- ' ||
  (select string_agg(uri, ', ') from mapper.match m, mapper.identifier_identities mi where mi.identifier_id = i.id and m.id = mi.match_id) as thing
   from mapper.identifier i"""
            int i = 0
            sql.eachRow(queryStr) { row ->
                out << row[0] + '\n'
                i++
                if (i % 1000 == 0) {
                    response.outputStream.flush()
                    log.debug "$i done"
                }
            }
        } finally {
            sql.close()
            response.outputStream.flush()
            response.outputStream.close()
        }
    }

    private static Identifier exists(String nameSpace, String objectType, Long idNumber) {
        Identifier.findByNameSpaceAndObjectTypeAndIdNumber(nameSpace, objectType, idNumber)
    }

    private static String buildErrorString(Errors errors) {
        errors.fieldErrors.collect { FieldError e ->
            "${e.field} cannot be ${e.rejectedValue}."
        }.join(' ')

    }
}

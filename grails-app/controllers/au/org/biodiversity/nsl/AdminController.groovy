/*
    Copyright 2015 Australian National Botanic Gardens

    This file is part of NSL mapper project.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy
    of the License at http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package au.org.biodiversity.nsl

import grails.transaction.Transactional
import groovy.sql.Sql
import groovy.transform.Synchronized
import org.apache.shiro.authz.annotation.RequiresRoles
import org.postgresql.PGConnection
import org.postgresql.copy.CopyManager
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import java.sql.Connection

import static org.springframework.http.HttpStatus.NOT_FOUND

@Transactional
class AdminController {

    def grailsApplication
    def mappingService

    @SuppressWarnings("GroovyUnusedDeclaration")
    static responseFormats = ['json']

    //todo once gets are no longer used remove gets where a better option exists
    static allowedMethods = [
            addIdentifier        : ["GET", "PUT"],
            deleteIdentifier     : ["GET", "DELETE"],
            addHost              : ["GET", "PUT"],
            setPreferredHost     : ["GET", "POST"],
            addURI               : ["GET", "PUT"],
            addIdentityToURI     : ["GET", "PUT"],
            removeIdentityFromURI: ["GET", "DELETE"],
            moveIdentity         : ["GET", "POST"],
            export               : ["GET"],
            identifier           : ["GET"],
            uri                  : ["GET"]

    ]

    private Sql getNSL() {
        String dbUrl = grailsApplication.config.dataSource.url
        String username = grailsApplication.config.dataSource.username
        String password = grailsApplication.config.dataSource.password
        String driverClassName = grailsApplication.config.dataSource.driverClassName
        Sql.newInstance(dbUrl, username, password, driverClassName)
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
    @Synchronized
    def addIdentifier(String nameSpace, String objectType, Long idNumber, Long versionNumber) {
        log.debug "Add identifier $nameSpace, $objectType, $idNumber, $versionNumber"
        Identifier exists = exists(nameSpace, objectType, idNumber, versionNumber)
        if (exists) {
            render(contentType: 'application/json') { [error: 'Identity already exists.', identity: exists] }
            return
        }
        Identifier identifier = new Identifier(nameSpace: nameSpace, objectType: objectType, idNumber: idNumber)
        Match match = new Match(uri: identifier.toUrn())
        identifier.addToIdentities(match)
        identifier.preferredUri = match
        if (identifier.validate() && match.validate()) {
            identifier.save(flush: true)
            Host host = Host.findByPreferred(true)
            if (host) {
                match.addToHosts(host)
                match.save()
            }
            render(contentType: 'application/json') {
                [success: 'Identity saved with default uri.', identity: identifier.id, preferredURI: mappingService.makePrefLink(identifier.preferredUri)]
            }
        } else {
            render(contentType: 'application/json') {
                [error   : 'Identity is not valid, see errors.',
                 identity: identifier,
                 errors  : buildErrorString(identifier.errors) + '\n' + buildErrorString(match.errors)]
            }
        }
    }

    @RequiresRoles('admin')
    @Synchronized
    def deleteIdentifier(String nameSpace, String objectType, Long idNumber, Long versionNumber, String reason) {
        log.debug "Delete identifier $nameSpace, $objectType, $idNumber"
        Identifier identifier = exists(nameSpace, objectType, idNumber, versionNumber)
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
    def addHost(String hostname) {
        Host host = Host.findByHostName(hostname)
        if (!host) {
            host = new Host(hostName: hostname, preferred: false)
            host.save()
            render(contentType: 'application/json') { [success: 'Host saved.', host: host] }
        } else {
            render(contentType: 'application/json') { [error: "Host already exists.", params: params] }
        }
    }

    @RequiresRoles('admin')
    def setPreferredHost(String hostname) {
        Host host = Host.findByHostName(hostname)
        if (!host) {
            render(contentType: 'application/json') { [error: "Host not found.", params: params] }
            return
        }
        if (!host.preferred) {
            Host prefHost = Host.findByPreferred(true)
            if (prefHost) {
                prefHost.preferred = false
                prefHost.save()
            }
            host.preferred = true
            host.save()
        }
        render(contentType: 'application/json') { [success: 'Preferred host set.', host: host] }
    }

    @RequiresRoles('admin')
    def addURI(String nameSpace, String objectType, Long idNumber, Long versionNumber, String uri) {
        Identifier identifier = exists(nameSpace, objectType, idNumber, versionNumber)
        if (identifier) {
            Match m = Match.findByUri(uri)
            if (m) {
                render(contentType: 'application/json') { [error: 'URI already exists.', identity: m] }
                return
            }
            Match match = new Match(uri: uri)
            identifier.addToIdentities(match)
            identifier.save()
            Host host = Host.findByPreferred(true)
            if (host) {
                match.addToHosts(host)
                match.save()
            }
            render(contentType: 'application/json') { [success: 'URI saved with identity.', identity: identifier] }
            return
        }
        render(contentType: 'application/json') { [error: "Identifier doesn't exist.", params: params] }
    }

    @RequiresRoles('admin')
    def addIdentityToURI(String nameSpace, String objectType, Long idNumber, Long versionNumber, String uri) {
        Identifier identifier = exists(nameSpace, objectType, idNumber, versionNumber)
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
    def removeIdentityFromURI(String nameSpace, String objectType, Long idNumber, Long versionNumber, String uri) {
        Identifier identifier = exists(nameSpace, objectType, idNumber, versionNumber)
        if (identifier) {
            String muri = mappingService.extractMatchStringFromResolverURI(uri.decodeURL())
            Match m = identifier.identities.find { Match m ->
                m.uri == muri
            }
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
    def moveIdentity(String fromNameSpace, String fromObjectType, Long fromIdNumber, Long fromVersionNumber,
                     String toNameSpace, String toObjectType, Long toIdNumber, Long toVersionNumber) {
        Identifier from = exists(fromNameSpace, fromObjectType, fromIdNumber, fromVersionNumber)
        Identifier to = exists(toNameSpace, toObjectType, toIdNumber, toVersionNumber)
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

    def identifier(String nameSpace, String objectType, Long idNumber, Long versionNumber) {
        Identifier identifier = exists(nameSpace, objectType, idNumber, versionNumber)
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

        final Date date = new Date()
        final String tempFileDir = grailsApplication.config.mapper.temp.file.directory
        final String fileName = "mapper-export-${date.format('yyyy-MM-dd-mmss')}.csv"
        final File outputFile = new File(tempFileDir, fileName)

        final String query = """copy (SELECT
  i.object_type,
  i.name_space,
  i.id_number,
  h.host_name,
  m.uri
FROM mapper.identifier i
  LEFT JOIN mapper.identifier_identities mi ON i.id = mi.identifier_id
  LEFT JOIN mapper.match m ON mi.match_id = m.id
  LEFT JOIN mapper.host_matches hm ON m.id = hm.match_id
  LEFT JOIN mapper.host h ON hm.host_id = h.id
order BY i.id_number) to STDOUT WITH CSV HEADER"""

        Sql sql = getNSL()
        Connection connection = sql.getConnection()
        connection.setAutoCommit(false)
        CopyManager copyManager = ((PGConnection) connection).getCopyAPI()
        copyManager.copyOut(query, new FileWriter(outputFile))
        sql.close()

        render(file: outputFile, fileName: outputFile.name, contentType: 'text/csv')
    }

    private static Identifier exists(String nameSpace, String objectType, Long idNumber, Long versionNumber) {
        Identifier.findByNameSpaceAndObjectTypeAndIdNumberAndVersionNumber(nameSpace, objectType, idNumber, versionNumber)
    }

    private static String buildErrorString(Errors errors) {
        errors.fieldErrors.collect { FieldError e ->
            "${e.field} cannot be ${e.rejectedValue}."
        }.join(' ')
    }
}

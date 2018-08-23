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
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authz.annotation.RequiresRoles
import org.codehaus.groovy.grails.web.json.JSONObject

import static org.springframework.http.HttpStatus.NOT_FOUND

@Transactional
class AdminController {

    def grailsApplication
    def mappingService
    def adminService

    @SuppressWarnings("GroovyUnusedDeclaration")
    static responseFormats = ['json']

    //todo once gets are no longer used remove gets where a better option exists
    static allowedMethods = [
            addIdentifier        : ["GET", "PUT"],
            bulkAddIdentifiers   : ["POST"],
            deleteIdentifier     : ["GET", "DELETE"],
            addHost              : ["GET", "PUT"],
            setPreferredHost     : ["PUT"],
            addURI               : ["GET", "PUT"],
            addIdentityToURI     : ["GET", "PUT"],
            removeIdentityFromURI: ["GET", "DELETE"],
            moveIdentity         : ["GET"],
            moveIdentityPost     : ["POST"],
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
    def addIdentifier(String nameSpace, String objectType, Long idNumber, Long versionNumber, String uri) {
        log.debug "Add identifier $nameSpace, $objectType, $idNumber, $versionNumber -> $uri"

        Identifier exists = exists(nameSpace, objectType, idNumber, versionNumber)
        if (exists) {
            render(contentType: 'application/json') {
                [success: 'Identity exists.', identity: exists.id, preferredURI: mappingService.makePrefLink(exists)]
            }
            return
        }

        if (uri) {
            Match match = Match.findByUri(uri)
            if (match) {
                render(contentType: 'application/json') {
                    [success: false, error: "URI $uri already exists and is associated with identifiers: ${match.identifiers}", identifiers: match.identifiers]
                }
                return
            }
        }

        try {
            Identifier identifier = adminService.addIdentifier(nameSpace, objectType, idNumber, versionNumber, uri)
            render(contentType: 'application/json') {
                [success: 'Identity saved with default uri.', identity: identifier.id, preferredURI: mappingService.makePrefLink(identifier.preferredUri)]
            }
        } catch (ServiceException e) {
            render(contentType: 'application/json') {
                [error: 'Identity is not valid, see errors.', errors: e.message]
            }
        }
    }

    @RequiresRoles('admin')
    def bulkAddIdentifiers() {
        log.debug "Bulk add identifiers"
        Map data = jsonObjectToMap(request.JSON as JSONObject)
        if (data && data.containsKey('identifiers')) {
            List<Map> identities = data.identifiers
            try {
                String userName = SecurityUtils.subject.principal
                adminService.bulkAddIdentifiers(identities, userName)
                render(contentType: 'application/json') { [success: "${identities.size()} identities added."] }
            } catch (e) {
                render(contentType: 'application/json') { [success: false, error: e.message] }
            }
        }
    }

    /**
     * This permanently removes identifiers and their exclusive matches, only use for draft identifiers
     * @return
     */
    @RequiresRoles('admin')
    def bulkRemoveIdentifiers() {
        log.debug("Bulk remove identifiers")
        Map data = jsonObjectToMap(request.JSON as JSONObject)
        if (data && data.containsKey('identifiers')) {
            List<Map> identities = data.identifiers
            try {
                adminService.bulkRemoveIdentifiers(identities)
                render(contentType: 'application/json') { [success: "${identities.size()} identities removed."] }
            } catch (e) {
                render(contentType: 'application/json') { [success: false, error: e.message] }
            }
        }
    }

    /**
     * This permanently removes matches and their identifiers, only use for draft identifiers
     * @return
     */
    @RequiresRoles('admin')
    def bulkRemoveUris() {
        log.debug("Bulk remove uris")
        Map data = jsonObjectToMap(request.JSON as JSONObject)
        if (data && data.containsKey('uris')) {
            List<String> uris = data.uris
            try {
                adminService.bulkRemoveByUri(uris)
                render(contentType: 'application/json') { [success: "${uris.size()} uris removed."] }
            } catch (e) {
                render(contentType: 'application/json') { [success: false, error: e.message] }
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
        log.debug "Setting preferred host to $hostname"
        Host host = Host.findByHostName(hostname)

        if (!host) {
            render(contentType: 'application/json') { [error: "Host not found.", params: params] }
            return
        }
        if (!host.preferred) {
            Host prefHost = Host.findByPreferred(true)
            if (prefHost) {
                log.debug "Unsetting current preferred host ${prefHost.hostName}"
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
            match.addToIdentifiers(identifier)
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
                m.addToIdentifiers(identifier)
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
            String muri = mappingService.extractMatchStringFromResolverURI(uri.decodeURL() as String)
            Match m = identifier.identities.find { Match m ->
                m.uri == muri
            }
            if (m) {
                identifier.removeFromIdentities(m)
                m.removeFromIdentifiers(identifier)
                m.save()
                identifier.save(flush: true)
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


    @RequiresRoles('admin')
    def moveIdentity(String fromNameSpace, String fromObjectType, Long fromIdNumber, Long fromVersionNumber,
                     String toNameSpace, String toObjectType, Long toIdNumber, Long toVersionNumber) {
        Identifier from = exists(fromNameSpace, fromObjectType, fromIdNumber, fromVersionNumber)
        Identifier to = exists(toNameSpace, toObjectType, toIdNumber, toVersionNumber)
        moveIdentityFromTo(from, to)
    }

    @RequiresRoles('admin')
    def moveIdentityPost() {
        Map data = jsonObjectToMap(request.JSON as JSONObject)
        Identifier from = exists(data.fromNameSpace as String, data.fromObjectType as String, data.fromIdNumber as Long, data.fromVersionNumber as Long)
        Identifier to = exists(data.toNameSpace as String, data.toObjectType as String, data.toIdNumber as Long, data.toVersionNumber as Long)
        moveIdentityFromTo(from, to)
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
    private void moveIdentityFromTo(Identifier from, Identifier to) {
        log.debug "Moving identities from $from to $to"
        if (to && from) {
            List<Match> fromMatches = from.identities.collect { it }
            fromMatches.each { Match match ->
                log.debug "Moving uri: $match.uri"
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

    private static Identifier exists(String nameSpace, String objectType, Long idNumber, Long versionNumber) {
        Identifier.findByNameSpaceAndObjectTypeAndIdNumberAndVersionNumber(nameSpace, objectType, idNumber, versionNumber)
    }

    private static Map jsonObjectToMap(JSONObject object) {
        Map map = [:]
        object.keySet().each { String key ->
            if (object[key] == JSONObject.NULL) {
                map.put(key, null)
            } else {
                map.put(key, object[key])
            }
        }
        return map
    }

}

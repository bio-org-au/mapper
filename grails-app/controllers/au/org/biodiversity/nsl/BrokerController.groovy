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

import grails.converters.XML
import org.codehaus.groovy.grails.web.util.WebUtils
import org.springframework.http.HttpStatus

import java.util.regex.Matcher

import static org.springframework.http.HttpStatus.GONE
import static org.springframework.http.HttpStatus.NOT_FOUND

class BrokerController {

    def mappingService
    def grailsApplication

    static allowedMethods = ['GET', 'POST']

    def index() {
        response.addHeader('Access-Control-Allow-Origin', '*') // unconditionally allow anything to talk to this service

        String requested = (WebUtils.getForwardURI(request) ?: request.getAttribute('javax.servlet.error.request_uri'))
        requested = requested.decodeURL()

        List<String> parts = requested.split('/api/')
        String matchUri = mappingService.extractMatchStringFromURI(parts[0])

        String api = null
        if (parts.size() == 2) {
            api = "/api/${parts[1]}"
        }

        String formatExtension = null
        if (api) {
            formatExtension = extension(api)
        } else {
            formatExtension = extension(matchUri)
            if (formatExtension) {
                matchUri = matchUri - ".$formatExtension"
            }
        }

        if (formatExtension) {
            params.format = formatExtension
        }

        Match match = Match.findByUri(matchUri)
        if (match) {

            def identifiers = match.identifiers

            if (identifiers.size() == 1) {

                Identifier identifier = identifiers.first()

                if (identifier?.deleted) {
                    response.status = 410 //Gone
                    String message = "$requested no longer exists. Reason: ${identifier.reasonDeleted}."
                    JsonErrorResponse jsonErrorResponse = new JsonErrorResponse(message: message, status: GONE)
                    return respond(jsonErrorResponse, [view: '/common/410', model: [message: message], status: GONE])
                }

                if (match.deprecated) {
                    //do a 301 to the preferred URI which will do a 303 to the resource
                    response.status = 301
                    //the preferred URI will be the default identifier URN e.g. name/apni/12345
                    Match preferred = mappingService.getPreferredLink(identifier)
                    String link = mappingService.makeResolverLink(preferred)
                    if (api) {
                        link += api
                    } else if(formatExtension) {
                        link += ".$formatExtension"
                    }
                    response.setHeader("Location", link)
                    return [links: [link], requested: requested]
                }

                response.status = 303
                response.setHeader("Cache-Control", "no-cache, must-revalidate")
                String format = response.format
                String link = mappingService.makeCurrentLink(identifier, format)
                if (api) {
                    link += api
                } else if(formatExtension) {
                    link += ".$formatExtension"
                }
                response.setHeader("Location", link)
                log.debug "Mapping $requested to $link"
                return [links: [link], requested: requested]
            }

            List<String> links = identifiers.findAll { Identifier identifier -> !identifier.deleted }
                                            .collect { Identifier ident ->
                mappingService.makeCurrentLink(ident)
            }

            log.debug "Mapping $requested -> $links"

            respond(links, [model: [links: links, requested: requested]])

        } else {
            String message = "$requested not found."
            JsonErrorResponse jsonErrorResponse = new JsonErrorResponse(message: message, status: NOT_FOUND)
            return respond(jsonErrorResponse, [view: '/common/404', model: [message: message], status: NOT_FOUND])
        }
    }

    private static String extension(String s) {
        Matcher matcher = (s =~ /(json|xml|rdf|html)?$/)
        ArrayList parts = matcher[0] as ArrayList
        if (parts[1]) {
            return parts[1]
        }
        return null
    }

    def getCurrentIdentity(String uri) {

        String requested = mappingService.extractMatchStringFromURI(uri.decodeURL())

        Match match = Match.findByUri(requested)
        if (match) {
            def currentIdentifiers = match.identifiers
            if (currentIdentifiers.size() == 0) {
                String message = "identifier for $uri not found."
                JsonErrorResponse jsonErrorResponse = new JsonErrorResponse(message: message, status: NOT_FOUND)
                return respond(jsonErrorResponse, [view: '/common/404', model: [message: message], status: NOT_FOUND])
            }
            return respond(currentIdentifiers, [view: 'getCurrentIdentity', model: [identities: currentIdentifiers, uri: uri]])
        }
        String message = "uri $uri not found."
        JsonErrorResponse jsonErrorResponse = new JsonErrorResponse(message: message, status: NOT_FOUND)
        return respond(jsonErrorResponse, [view: '/common/404', model: [message: message], status: NOT_FOUND])
    }

    def links(String nameSpace, String objectType, Long idNumber) {
        response.addHeader('Access-Control-Allow-Origin', '*') // unconditionally allow anything to talk to this service

        Identifier identifier = Identifier.findByNameSpaceAndObjectTypeAndIdNumber(nameSpace, objectType, idNumber)

        if (identifier) {
            List<Map> links = mappingService.findMatchingLinks(identifier)
            log.info "Links for $objectType/$nameSpace/$idNumber -> $links"

            withFormat {
                html links: links
                json {
                    render(contentType: 'application/json') { links }
                }
                xml {
                    render(links as XML)
                }
            }
        } else {
            return notFound([error: "404: $objectType/$nameSpace/$idNumber not found.", links: []])
        }
    }

    def preferredLink(String nameSpace, String objectType, Long idNumber) {
        Identifier identifier = Identifier.findByNameSpaceAndObjectTypeAndIdNumber(nameSpace, objectType, idNumber)
        if (identifier) {
            Match preferred = mappingService.getPreferredLink(identifier)
            if (preferred) {
                String link = mappingService.makeResolverLink(preferred)
                log.info "Preferred link to $objectType/$nameSpace/$idNumber is $link"
                return withFormat {
                    html([link: link])
                    json {
                        render(contentType: 'application/json') { [link: link] }
                    }
                    xml {
                        render([link: link] as XML)
                    }
                }
            }
        }
        return notFound([error: "404: preferred link to $objectType/$nameSpace/$idNumber not found.", link: 'none'])
    }

    private notFound(Map errorResponse) {
        log.info "Not found $errorResponse"
        response.status = NOT_FOUND.value()
        withFormat {
            html errorResponse
            json {
                render(contentType: 'application/json') { errorResponse }
            }
            xml {
                render(errorResponse as XML)
            }
        }
    }

}

class JsonErrorResponse {
    String message
    HttpStatus status
}
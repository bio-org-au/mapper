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

import org.grails.plugins.metrics.groovy.Timed


class MappingService {

    def grailsApplication

    Host preferredHost
    String protocol

    // We cache the host/context prefix length to try and speed up the matching of URLs to identity
    private Integer fullPrefixLength = null
    private Integer relPrefixLength = null

    private getPreferredHost() {
        if(!preferredHost) {
            preferredHost = Host.findByPreferred(true)
        }
        return preferredHost
    }

    private defaultProtocol() {
        if(!protocol){
            protocol  = grailsApplication.config.mapper.defaultProtocol
        }
        return protocol
    }

    @Timed
    String makeCurrentLink(Identifier ident, String format = 'html') {
        String shardHostname = grailsApplication.config.mapper.shards[ident.nameSpace].baseURL

        Closure serviceClosure = grailsApplication.config.mapper.shards[ident.nameSpace].service[format] as Closure
        if (!serviceClosure) {
            serviceClosure = grailsApplication.config.mapper.shards[ident.nameSpace].service['html'] as Closure
        }

        String serviceUri = serviceClosure.call(ident)
        return "$shardHostname/$serviceUri"
    }

    @Timed
    String makePrefLink(Match m) {
        Host host = getPreferredHost()
        if (host) {
            return "${defaultProtocol()}://${host.hostName}/${encodeParts(m.uri)}"
        } else {
            String resolverUrl = grailsApplication.config.mapper.resolverURL
            return "${resolverUrl}/${encodeParts(m.uri)}"
        }
    }

    List<Map> findMatchingLinks(Identifier ident) {
        Match preferred = getPreferredLink(ident)
        List<Map> links = []
        ident.identities.findAll { Match m -> !m.deprecated }.each { Match m ->
            m.hosts.each { Host host ->
                links << [link: "${defaultProtocol()}://${host.hostName}/${encodeParts(m.uri)}", resourceCount: m.identifiers.size(), preferred: host.preferred && m.id == preferred.id]
            }
        }
        links.sort { a, b ->
            if (a.resourceCount == b.resourceCount) {
                a.link <=> b.link
            } else {
                a.resourceCount <=> b.resourceCount
            }
        }
    }

    @Timed
    Match getPreferredLink(Identifier identifier) {
        if (identifier.preferredUri) {
            return identifier.preferredUri
        }
        Identifier.withTransaction {
            String prefUrnStr = identifier.toUrn()
            Match preferred = identifier.identities.find { Match m -> m.uri == prefUrnStr }
            if (!preferred) {
                //we don't have the default so find the shortest non deprecated link to this identifier
                preferred = identifier.identities
                                      .sort { a, b -> a.uri <=> b.uri }
                                      .find { Match m ->
                    !m.deprecated &&
                            m.identifiers.size() == 1 &&
                            m.identifiers.first() == identifier
                }
            }
            identifier.preferredUri = preferred
            identifier.save()
            return preferred.refresh()
        }
    }

    private static String encodeParts(String uri) {
        uri.split('/').collect { it.encodeAsURL() }.join('/').replaceAll(/\+/, '%20')
        //note the + -> %20 replacement is to handle proxies encoding + as '+' not space.
    }

    private String getResolverPrefix() {
        String prefix = grailsApplication.config.mapper.resolverURL + '/'
        String contextExtension = grailsApplication.config.mapper.contextExtension

        if (contextExtension) {
            prefix += "$contextExtension/"
        }
        return prefix
    }

    private int getFullPrefixLength() {
        if (!fullPrefixLength) {
            String prefix = getResolverPrefix()
            fullPrefixLength = prefix.size()
            log.info "full prefix length set to $fullPrefixLength"
        }
        return fullPrefixLength
    }

    private int getRelPrefixLength() {
        if (!relPrefixLength) {
            String prefix = getResolverPrefix()
            prefix = prefix.replaceAll("^https?://[^/]*", '')
            relPrefixLength = prefix.size()
            log.info "relative prefix length set to $relPrefixLength"

        }
        return relPrefixLength
    }

    /**
     * Get the unique match part from the given URI by removing the Resolver + context extension prefix from the uri
     *
     * for example given 'https://id.biodiversity.org.au/nsl/mapper/boa/name/apni/123456' where the resolver URL is
     * 'https://id.biodiversity.org.au' and the contextExtension is 'nsl/mapper/boa' it will return
     * 'name/apni/123456'
     *
     * Note this is intended for the uri supplied behind a proxy redirection, so a proxy might redirect
     * 'http://id.biodiversity.org.au/name/apni/110231' to 'http://id.biodiversity.org.au/nsl/mapper/boa/name/apni/110231'
     *
     * @param uri
     * @return the unique match string to try.
     */
    String extractMatchStringFromURI(String uri) {
        log.info "extracting match from $uri"

        String match
        if (uri.startsWith('http')) {
            match = uri.substring(getFullPrefixLength())
        } else {
            match = uri.substring(getRelPrefixLength())
        }

        log.debug "URI: $uri -> $match"

        return match
    }

    String extractMatchStringFromResolverURI(String uri) {
        if (uri.startsWith('http')) {
            String resolverUrl = grailsApplication.config.mapper.resolverURL + '/'
            return uri - resolverUrl
        } else {
            return uri
        }
    }
}

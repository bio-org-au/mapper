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

@Transactional
class MappingService {

    def grailsApplication

    String makeCurrentLink(Identifier ident, String format = 'html') {
        String shardHostname = grailsApplication.config.mapper.shards[ident.nameSpace].baseURL

        Closure serviceClosure = grailsApplication.config.mapper.shards[ident.nameSpace].service[format] as Closure
        if(!serviceClosure) {
            serviceClosure = grailsApplication.config.mapper.shards[ident.nameSpace].service['html'] as Closure
        }

        String serviceUri = serviceClosure.call(ident)
        return "$shardHostname/$serviceUri"
    }

    String makeResolverLink(Match m) {
        String resolverUrl = grailsApplication.config.mapper.resolverURL
        "${resolverUrl}/${encodeParts(m.uri)}"
    }

    List<Map> findMatchingLinks(Identifier ident) {
        Match preferred = getPreferredLink(ident)
        ident.identities.findAll { Match m -> !m.deprecated }.collect { Match m ->
            [link: makeResolverLink(m), resourceCount: m.identifiers.size(), preferred: m.id == preferred.id]
        }.sort { a,b ->
            if( a.resourceCount == b.resourceCount) {
                a.link <=> b.link
            } else {
                a.resourceCount <=> b.resourceCount
            }
        }
    }

    Match getPreferredLink(Identifier identifier) {
        if(identifier.preferredUri) {
            return identifier.preferredUri
        }
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

    private String encodeParts(String uri) {
        uri.split('/').collect{ it.encodeAsURL() }.join('/').replaceAll(/\+/, '%20')
    }
}

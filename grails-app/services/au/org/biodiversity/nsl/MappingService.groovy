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
            [link: makeResolverLink(m), resourceCount: m.identifiers.size(), preferred: m == preferred]
        }.sort { a,b ->
            if( a.resourceCount == b.resourceCount) {
                a.link <=> b.link
            } else {
                a.resourceCount <=> b.resourceCount
            }
        }
    }

    Match getPreferredLink(Identifier identifier) {
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
        return preferred
    }

    private String encodeParts(String uri) {
        uri.split('/').collect{ it.encodeAsURL() }.join('/').replaceAll(/\+/, '%20')
    }
}

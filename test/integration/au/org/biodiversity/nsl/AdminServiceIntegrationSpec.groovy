package au.org.biodiversity.nsl

import grails.test.mixin.TestFor
import spock.lang.Specification

import javax.sql.DataSource

@TestFor(AdminService)
class AdminServiceIntegrationSpec extends Specification {

    DataSource dataSource

    def setup() {
        service.dataSource = dataSource
    }

    def cleanup() {
    }

    void "test bulk add and remove"() {
        when: 'we add identifiers'
        Integer matchCount = Match.count()
        Integer identifierCount = Identifier.count()
        List<Map> identifiers = [
                [s: 'apni', o: 'treeElement', i: 111, v: 1, u: 'tree/1/111'],
                [s: 'apni', o: 'treeElement', i: 222, v: 1, u: 'tree/1/222'],
                [s: 'apni', o: 'treeElement', i: 333, v: 1, u: 'tree/1/333'],
                [s: 'apni', o: 'treeElement', i: 444, v: 1, u: 'tree/1/444']
        ]
        service.bulkAddIdentifiers(identifiers, 'fred')

        then: 'we have the identifiers and matches'
        Match.count() == matchCount + 4
        Identifier.count() == identifierCount + 4
        identifiers.each {
            Identifier.findByNameSpaceAndObjectTypeAndIdNumber(it.s, it.o, it.i).preferredUri ==
                    Match.findByUri(it.u)
        }

        when: 'we remove identifiers'
        service.bulkRemoveIdentifiers(identifiers)

        then: 'the identifiers have been removed'
        Match.count() == matchCount
        Identifier.count() == identifierCount
        identifiers.each {
            Identifier.findByNameSpaceAndObjectTypeAndIdNumber(it.s, it.o, it.i) == null
            Match.findByUri(it.u) == null
        }

        when: 'we add the identifiers again'
        service.bulkAddIdentifiers(identifiers, 'fred')

        then: 'we have the identifiers and matches again'
        Match.count() == matchCount + 4
        Identifier.count() == identifierCount + 4
        identifiers.each {
            Identifier.findByNameSpaceAndObjectTypeAndIdNumber(it.s, it.o, it.i).preferredUri ==
                    Match.findByUri(it.u)
        }

        when: 'we bulk remove by uri'
        Host prefHost = Host.findByPreferred(true)
        List<String> uris = identifiers.collect { "http://${prefHost.hostName}/${it.u}".toString() }
        Match.withSession { s ->
            s.flush()
            s.clear()
        }
        service.bulkRemoveByUri(uris)

        then: 'they have been removed'
        Match.count() == matchCount
        Identifier.count() == identifierCount
        identifiers.each {
            Identifier.findByNameSpaceAndObjectTypeAndIdNumber(it.s, it.o, it.i) == null
            Match.findByUri(it.u) == null
        }

    }

}

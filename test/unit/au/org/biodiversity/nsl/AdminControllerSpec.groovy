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

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import org.apache.shiro.util.ThreadContext
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(AdminController)
@TestMixin(DomainClassUnitTestMixin)
@Mock([Identifier, Match, Host])
class AdminControllerSpec extends Specification {

    MappingService mappingService
    AdminService adminService

    def setup() {

        def subject = [getPrincipal: { "iamauser" },
                       isAuthenticated: { true }
        ] as Subject

        ThreadContext.put(ThreadContext.SECURITY_MANAGER_KEY,
                [getSubject: { subject }] as SecurityManager)

        SecurityUtils.metaClass.static.getSubject = { subject }

        mappingService = new MappingService()
        mappingService.grailsApplication = [config: makeAConfig()]
        controller.mappingService = mappingService
        adminService = new AdminService()
        adminService.transactionManager = Mock(PlatformTransactionManager) {
            getTransaction(_) >> Mock(TransactionStatus)
        }
        controller.adminService = adminService
    }

    def cleanup() {
    }

    void "test add host"() {
        when: "we try to add a host"
        request.method = 'PUT'
        controller.addHost('id.biodiversity.org.au')
        println response.text

        then: "host should exist"
        response.text.contains('Host saved.')
        Host.count() == 1
        !Host.list().first().preferred

        when: "we set it as preferred"
        response.reset()
        controller.setPreferredHost('id.biodiversity.org.au')
        println response.text

        then: "the host is set as preferred"
        Host.list().first().preferred

        when: "we add a non preferred host it is stored correctly"
        response.reset()
        controller.addHost('biodiversity.org.au')
        println response.text

        then: "host should exist and not be preferred"
        response.text.contains('Host saved.')
        Host.count() == 2
        !Host.findByHostName('biodiversity.org.au')?.preferred
        Host.findAllByPreferred(true).size() == 1

        when: "we set the second host as preferred"
        response.reset()
        controller.setPreferredHost('biodiversity.org.au')
        println response.text

        then: "it is preferred and the only preferred host"
        Host.findByHostName('biodiversity.org.au').preferred
        Host.findAllByPreferred(true).size() == 1


    }

    void "test add Identifier"() {
        when: "we try to add a valid identifier it works"
        request.method = 'PUT'
        controller.addHost('id.biodiversity.org.au')
        response.reset()
        controller.addHost('biodiversity.org.au')
        response.reset()
        controller.setPreferredHost('id.biodiversity.org.au')
        response.reset()
        controller.addIdentifier('apni', 'name', 12345, null, null)

        println response.text

        then:
        response.text.contains('Identity saved with default uri.')
        Identifier.count() == 1
        Match.count() == 1
        Host.count() == 2

        when:
        def i1 = Identifier.get(1)
        def m1 = Match.get(1)
        def h1 = Host.findByHostName('id.biodiversity.org.au')

        then:

        i1.nameSpace == 'apni'
        i1.objectType == 'name'
        i1.idNumber == 12345l
        i1.identities.size() == 1
        i1.preferredUri == m1
        m1.uri == 'name/apni/12345'
        m1.identifiers.size() == 1
        h1.hostName == 'id.biodiversity.org.au'
        m1.hosts
        m1.hosts.size() == 1
        m1.hosts.contains(h1)

        when: "we try to add the same identifier it fails"
        response.reset()
        controller.addIdentifier('apni', 'name', 12345, null, null)

        println response.text

        then:
        response.text.contains('Identity exists.')

        when: "we try to add an invalid identity if fails."
        response.reset()
        controller.addIdentifier('apni', 'name', null, null, null)

        println response.text

        then:
        response.text.contains('Identity is not valid, see errors.')
        response.text.contains('idNumber cannot be null.')

        when: "We can add a second valid Identifier."
        response.reset()
        controller.addIdentifier('apni', 'name', 12346, null, null)

        println response.text

        then:
        response.text.contains('Identity saved with default uri.')
        Identifier.count() == 2
        println Match.list()
        Match.count() == 2
        Identifier.findByIdNumber(12346).preferredUri.hosts.first() == h1

        when:
        def i5 = Identifier.get(2)
        def m4 = Match.get(2)

        then:
        i5.nameSpace == 'apni'
        i5.objectType == 'name'
        i5.idNumber == 12346l
        m4.uri == 'name/apni/12346'

    }

    void "test add URI"() {
        when: "we add a unique uri to an identifier it should work"
        request.method = 'PUT'
        controller.addIdentifier('apni', 'name', 1, null, null)

        then:
        response.text.contains('Identity saved with default uri.')
        Identifier.count() == 1
        Match.count() == 1

        when:
        response.reset()
        controller.addURI('apni', 'name', 1, null, 'fred')

        println response.text

        then:
        response.text.contains('URI saved with identity.')
        Match.count() == 2

        when:
        Match m = Match.findByUriLike('fred')

        then:
        m != null
        m.uri == 'fred'
        m.identifiers.size() == 1
        m.identifiers.first().idNumber == 1l

        when: "we try to add the same uri again it won't work"
        response.reset()
        controller.addURI('apni', 'name', 1, null, 'fred')

        println response.text

        then:
        response.text.contains('URI already exists.')
    }

    void "test add/remove identity to uri"() {
        when:
        Identifier i1 = new Identifier(nameSpace: 'apni', objectType: 'name', idNumber: 23)
        i1.save()
        Match m1 = new Match(uri: 'one')
        m1.save()
        Match m2 = new Match(uri: 'two')
        m2.save()

        then:
        i1.identities == null
        m1.identifiers == null
        m2.identifiers == null

        when:
        request.method = 'PUT'

        controller.addIdentityToURI('apni', 'name', 23, null, 'one')

        println response.text
        m2.refresh() // to fill in the null list

        then:
        response.text.contains('URI linked with identity.')
        i1.identities?.size() == 1
        m1.identifiers?.size() == 1
        i1.identities?.first() == m1
        m1.identifiers?.first() == i1
        m2.identifiers?.empty

        when: "I add m2 'two' to i1"
        response.reset()

        controller.addIdentityToURI('apni', 'name', 23, null, 'two')
        println response.text

        then: 'Then m2 has one identifier and i1 has two matches'
        response.text.contains('URI linked with identity.')
        i1.identities?.size() == 2
        m1.identifiers.size() == 1
        m2.identifiers.size() == 1
        i1.identities?.contains(m1)
        i1.identities?.contains(m2)
        m1.identifiers.first() == i1
        m2.identifiers.first() == i1

        when: "the identity doesn't exist return an error"
        response.reset()

        controller.addIdentityToURI('apni', 'name', 99, null, 'two')
        println response.text

        then:
        response.text.contains("Identifier doesn't exist.")

        when: "the uri doesn't exist return an error"
        response.reset()

        controller.addIdentityToURI('apni', 'name', 23, null, 'blah')
        println response.text

        then:
        response.text.contains("URI doesn't exist.")

        when: 'I remove i1 from m2'
        response.reset()
        request.method = 'DELETE'
        String link = mappingService.makePrefLink(m2).encodeAsURL()
        println "removing $link"
        controller.removeIdentityFromURI('apni', 'name', 23, null, link)

        println response.text

        then:
        response.text.contains('Identity removed from URI.')
        i1.identities?.size() == 1
        m1.identifiers?.size() == 1
        i1.identities?.first() == m1
        m1.identifiers?.first() == i1
        m2.identifiers?.empty

    }

    private static ConfigObject makeAConfig() {
        ConfigSlurper slurper = new ConfigSlurper('test')
        String configString = '''
grails.serverURL = 'http://localhost:7070/nsl-mapper'

mapper {
    resolverURL = 'http://localhost:7070/nsl-mapper/boa'
    contextExtension = 'boa' //extension to the context path (after nsl-mapper).
    defaultProtocol = 'http'
    
    defaultResolver = { ident ->
        Map serviceHosts = [
                apni: 'http://localhost:8080',  // .../au/vascular
                ausmoss: 'http://localhost:8080', // .../au/moss
                lichen: 'http://localhost:8080',  // .../au/lichen
                foa: 'http://biodiversity.org.au/'
        ]
        String host = serviceHosts[ident.nameSpace]
        if (ident.objectType == 'tree') {
            return "${host}/services/${ident.objectType}/${ident.versionNumber}/${ident.idNumber}"
        }
        if(ident.nameSpace == "foa"){
            return "${host}foa/taxa/${ident.idNumber}/summary"
        }
        return "${host}/services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
    }

    format = [
            html: [
                    resolver: defaultResolver
            ],
            json: [
                    resolver: defaultResolver
            ],
            xml: [
                    resolver: defaultResolver
            ],
            rdf: [
                    resolver : { ident ->
                        String url = "DESCRIBE <http://biodiversity.org.au/boa/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}>".encodeAsURL()
                        "sparql/?query=${url}"
                    }
            ]
    ]
}

api.auth = [
        'blah-blah-blah-blah-blah': [
                application: 'apni-services',
                roles      : ['admin'],
                host       : '127.0.0.1\'
        ]
]
'''
        return slurper.parse(configString)
    }
}

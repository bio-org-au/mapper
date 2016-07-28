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

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.gorm.Domain
import grails.test.mixin.hibernate.HibernateTestMixin
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import org.apache.shiro.util.ThreadContext
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(AdminController)
@TestMixin(HibernateTestMixin)
@Domain([Identifier, Match, Host])
class AdminControllerSpec extends Specification {

    def setup() {

        def subject = [getPrincipal   : { "iamauser" },
                       isAuthenticated: { true }
        ] as Subject

        ThreadContext.put(ThreadContext.SECURITY_MANAGER_KEY,
                [getSubject: { subject }] as SecurityManager)

        SecurityUtils.metaClass.static.getSubject = { subject }

        Match.list().each { Match match ->
            List<Host> h = new ArrayList<>(match.hosts)
            h.each { match.removeFromHosts(it) }
        }
        Host.deleteAll(Host.list())
        Identifier.deleteAll((Identifier.list()))
        Match.deleteAll(Match.list())
    }

    def cleanup() {
    }

    void "test add host"() {
        when: "we try to add a host"
        controller.addHost('id.biodiversity.org.au')
        println response.text

        then: "host should exist"
        response.text.contains('Host saved.')
        Host.count() == 1
        !Host.list().first().preferred

        when: "we set it as preferred"
        response.reset()
        controller.setPreferredHost('id.biodiversity.org.au')

        then: "the host is set as preferred"
        Host.list().first().preferred

        when: "we add a non preferred host it is stored correctly"
        response.reset()
        controller.addHost('biodiversity.org.au')
        println response.text

        then: "host should exist and not be preferred"
        response.text.contains('Host saved.')
        Host.count() == 2
        Host.findByHostName('biodiversity.org.au')?.preferred == false
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
        controller.addHost('id.biodiversity.org.au')
        controller.addHost('biodiversity.org.au')
        controller.setPreferredHost('id.biodiversity.org.au')
        response.reset()
        controller.addIdentifier('apni', 'name', 12345)

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
        controller.addIdentifier('apni', 'name', 12345)

        println response.text

        then:
        response.text.contains('Identity already exists.')

        when: "we try to add an invalid identity if fails."
        response.reset()
        controller.addIdentifier('apni', 'name', null)

        println response.text

        then:
        response.text.contains('Identity is not valid, see errors.')
        response.text.contains('idNumber cannot be null.')

        when: "We can add a second valid Identifier."
        response.reset()
        controller.addIdentifier('apni', 'name', 12346)

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
        controller.addIdentifier('apni', 'name', 1)

        then:
        response.text.contains('Identity saved with default uri.')
        Identifier.count() == 1
        Match.count() == 1

        when:
        response.reset()
        controller.addURI('apni', 'name', 1, 'fred')

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
        controller.addURI('apni', 'name', 1, 'fred')

        println response.text

        then:
        response.text.contains('URI already exists.')
    }

    void "test add identity to uri"() {
        when:
        Identifier i1 = new Identifier(nameSpace: 'apni', objectType: 'name', idNumber: 23)
        i1.save()
        Identifier i2 = new Identifier(nameSpace: 'apni', objectType: 'name', idNumber: 32)
        i2.save()
        Match m1 = new Match(uri: 'one')
        m1.save()
        Match m2 = new Match(uri: 'two')
        m2.save()

        then:
        i1.identities == null
        i2.identities == null
        m1.identifiers == null
        m2.identifiers == null

        when:

        controller.addIdentityToURI('apni', 'name', 23, 'one')

        println response.text
        i1.refresh()
        i2.refresh()
        m1.refresh()
        m2.refresh()

        then:
        response.text.contains('URI linked with identity.')
        i1.identities?.size() == 1
        m1.identifiers?.size() == 1
        i1.identities?.first() == m1
        m1.identifiers?.first() == i1
        i2.identities?.empty
        m2.identifiers?.empty

        when:
        response.reset()

        controller.addIdentityToURI('apni', 'name', 23, 'two')
        println response.text
        i1.refresh()
        i2.refresh()
        m1.refresh()
        m2.refresh()

        then:
        response.text.contains('URI linked with identity.')
        i1.identities?.size() == 2
        m1.identifiers.size() == 1
        m2.identifiers.size() == 1
        i1.identities?.contains(m1)
        i1.identities?.contains(m2)
        m1.identifiers.first() == i1
        m2.identifiers.first() == i1
        i2.identities?.empty

        when: "the identity doesn't exist return an error"
        response.reset()

        controller.addIdentityToURI('apni', 'name', 99, 'two')
        println response.text

        then:
        response.text.contains("Identifier doesn't exist.")

        when: "the uri doesn't exist return an error"
        response.reset()

        controller.addIdentityToURI('apni', 'name', 23, 'blah')
        println response.text

        then:
        response.text.contains("URI doesn't exist.")

    }

    void "add NSL Shard should update config"() {
        when: "we have a mapper config and add a shard"
        controller.grailsApplication = [config: makeAConfig()]
        controller.addNslShard('blah', 'http://blahg.org')

        then: "the blah shard is added to the running config"
        println controller.grailsApplication.config
        controller.grailsApplication.config.mapper.shards.containsKey('blah')
        controller.grailsApplication.config.mapper.shards.blah.service.html([objectType: 'name', nameSpace: 'fred', idNumber: 2345]) == 'services/name/fred/2345'

        when: "we re parse the config file"
        ConfigObject config = slurpTest()

        then: "it works and still has the blah config in it"
        config.mapper.shards.containsKey('blah')
        config.mapper.shards.blah.service.html([objectType: 'name', nameSpace: 'fred', idNumber: 2345]) == 'services/name/fred/2345'
    }

    private static ConfigObject slurpTest() {
        ConfigSlurper slurper = new ConfigSlurper('test')
        URL configFileURL = new URL("file:test-nsl-mapper-config.groovy")
        return slurper.parse(configFileURL)
    }

    private static ConfigObject makeAConfig() {
        ConfigSlurper slurper = new ConfigSlurper('test')
        String configString = '''
grails.config.locations = ["file:test-nsl-mapper-config.groovy"]

grails.serverURL = 'http://localhost:7070/nsl-mapper\'

mapper {
    resolverURL = 'http://localhost:7070/nsl-mapper/boa\'
    contextExtension = 'boa' //extension to the context path (after nsl-mapper).

    shards = [
            apni   : [
                    baseURL: 'http://localhost:8080',
                    service: [
                            html: { ident ->
                                "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                            },
                            json: { ident ->
                                "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                            },
                            xml : { ident ->
                                "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                            },
                            rdf : { ident ->
                                String url = "DESCRIBE <http://biodiversity.org.au/boa/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}>".encodeAsURL()
                                "sparql/?query=${url}"
                            }
                    ]
            ],
            ausmoss: [
                    baseURL: 'http://localhost:8080',
                    service: [
                            html: { ident ->
                                "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                            },
                            json: { ident ->
                                "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                            },
                            xml : { ident ->
                                "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                            },
                            rdf : { ident ->
                                String url = "DESCRIBE <http://biodiversity.org.au/boa/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}>".encodeAsURL()
                                "sparql/?query=${url}"
                            }
                    ]
            ],
            foa    : [
                    baseURL: 'http://biodiversity.org.au/',
                    service: [
                            html: { ident ->
                                "foa/taxa/${ident.idNumber}/summary"
                            }
                    ]
            ]]
}

api.auth = [
        'blah-blah-blah-blah-blah': [
                application: 'apni-services',
                roles      : ['admin'],
                host       : '127.0.0.1\'
        ]
]
'''
        File testConfigFile = new File('test-nsl-mapper-config.groovy')
        testConfigFile.write(configString)
        return slurper.parse(configString)
    }
}

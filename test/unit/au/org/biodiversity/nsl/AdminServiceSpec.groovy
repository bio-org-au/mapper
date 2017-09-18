package au.org.biodiversity.nsl

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import org.apache.shiro.util.ThreadContext
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(AdminService)
@TestMixin(DomainClassUnitTestMixin)
@Mock([Identifier, Match, Host])
class AdminServiceSpec extends Specification {

    def setup() {
        def subject = [getPrincipal   : { "iamauser" },
                       isAuthenticated: { true }
        ] as Subject

        ThreadContext.put(ThreadContext.SECURITY_MANAGER_KEY,
                [getSubject: { subject }] as SecurityManager)

        SecurityUtils.metaClass.static.getSubject = { subject }

    }

    def cleanup() {
    }

    void "test add and remove Identifier"() {
        when: 'I add an identifier'
        Identifier identifier = service.addIdentifier('namespace','test', 123 as Long, 456 as Long)

        then: 'It works'
        identifier
        identifier.nameSpace == 'namespace'
        identifier.objectType == 'test'
        identifier.idNumber == 123
        identifier.versionNumber == 456
        identifier.identities
        identifier.identities.size() == 1
        identifier.identities[0].uri == identifier.preferredUri.uri
        identifier.preferredUri.uri == identifier.toUrn()
        identifier.preferredUri.identifiers
        identifier.preferredUri.identifiers.size() == 1
        identifier.preferredUri.identifiers[0] == identifier

        when: 'I remove the the identifier'
        service.removeIdentifier(identifier)

        then: 'It is removed along with the match'
        Identifier.count() == 0
        Match.count() == 0
    }

    void "test add and remove Identifier with two identifiers"() {
        when: 'I add an identifier'
        Identifier identifier1 = service.addIdentifier('namespace','test', 123 as Long, 456 as Long)
        Identifier identifier2 = service.addIdentifier('namespace','test', 789 as Long, 100 as Long)

        then: 'It works'
        identifier1
        identifier1.nameSpace == 'namespace'
        identifier1.objectType == 'test'
        identifier1.idNumber == 123
        identifier1.versionNumber == 456
        identifier1.identities
        identifier1.identities.size() == 1
        identifier1.identities[0].uri == identifier1.preferredUri.uri
        identifier1.preferredUri.uri == identifier1.toUrn()
        identifier1.preferredUri.identifiers
        identifier1.preferredUri.identifiers.size() == 1
        identifier1.preferredUri.identifiers[0] == identifier1

        identifier2
        identifier2.nameSpace == 'namespace'
        identifier2.objectType == 'test'
        identifier2.idNumber == 789
        identifier2.versionNumber == 100
        identifier2.identities
        identifier2.identities.size() == 1
        identifier2.identities[0].uri == identifier2.preferredUri.uri
        identifier2.preferredUri.uri == identifier2.toUrn()
        identifier2.preferredUri.identifiers
        identifier2.preferredUri.identifiers.size() == 1
        identifier2.preferredUri.identifiers[0] == identifier2

        when: 'I add match from identifier2 to identifier1'
        Match match1 = identifier1.preferredUri
        identifier2.addToIdentities(match1)
        match1.addToIdentifiers(identifier2)
        identifier2.save()
        println Match.list()
        println match1.identifiers

        then: 'match1 will have two identifiers'
        match1.identifiers.size() == 2
        match1.identifiers.contains(identifier1)
        match1.identifiers.contains(identifier2)

        when: 'I remove the the identifier'
        service.removeIdentifier(identifier1)
        println Match.list()

        then: 'It is removed but the match1 remains'
        Identifier.count() == 1
        Match.count() == 2
        match1.identifiers.size() == 1
    }
}

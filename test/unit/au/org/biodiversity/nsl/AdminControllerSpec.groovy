package au.org.biodiversity.nsl

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(AdminController)
@Mock([Identifier, Match])
class AdminControllerSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test add Identifier"() {
        when: "we try to add a valid identifier it works"
        controller.addIdentifier('apni', 'name', 12345)

        println response.text

        then:
        response.text.contains('Identity saved with default uri.')
        Identifier.count() == 1
        Match.count() == 1

        when:
        def i1 = Identifier.get(1)
        def m1 = Match.get(1)

        then:

        i1.nameSpace == 'apni'
        i1.objectType == 'name'
        i1.idNumber == 12345
        i1.identities.size() == 1
        m1.uri == 'name/apni/12345'
        m1.identifiers.size() == 1


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
        Match.count() == 2

        when:
        def i5 = Identifier.get(2)
        def m4 = Match.get(2)

        then:
        i5.nameSpace == 'apni'
        i5.objectType == 'name'
        i5.idNumber == 12346
        m4.uri == 'name/apni/12346'

    }

    void "test add URI"() {
        when: "we add a unique uri to an identifier it should work"
        controller.addIdentifier('apni', 'name', 12345)

        then:
        response.text.contains('Identity saved with default uri.')
        Identifier.count() == 1
        Match.count() == 1

        when:
        response.reset()
        controller.addURI('apni', 'name', 12345,'fred')

        println response.text

        then:
        response.text.contains('URI saved with identity.')
        Match.count() == 2

        when:
        Match m = Match.get(2)

        then:
        m.uri == 'fred'
        m.identifiers.size() == 1
        m.identifiers.first().id == 1

        when: "we try to add the same uri again it won't work"
        response.reset()
        controller.addURI('apni', 'name', 12345,'fred')

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

        then:
        response.text.contains('URI linked with identity.')
        i1.identities.size() == 1
        m1.identifiers.size() == 1
        i1.identities.first() == m1
        m1.identifiers.first() == i1
        i2.identities == null
        m2.identifiers == null

        when:
        response.reset()

        controller.addIdentityToURI('apni', 'name', 23, 'two')
        println response.text

        then:
        response.text.contains('URI linked with identity.')
        i1.identities.size() == 2
        m1.identifiers.size() == 1
        m2.identifiers.size() == 1
        i1.identities.contains(m1)
        i1.identities.contains(m2)
        m1.identifiers.first() == i1
        m2.identifiers.first() == i1
        i2.identities == null

        when: "the identity doesn't exist return an error"
        response.reset()

        controller.addIdentityToURI('apni', 'name', 99, 'two')
        println response.text

        then:
        response.text.contains("Identifier doesn\\'t exist.")

        when: "the uri doesn't exist return an error"
        response.reset()

        controller.addIdentityToURI('apni', 'name', 23, 'blah')
        println response.text

        then:
        response.text.contains("URI doesn\\'t exist.")

    }
}

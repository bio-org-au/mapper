package au.org.biodiversity.nsl

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
@TestFor(MappingService)
@TestMixin(DomainClassUnitTestMixin)
class MappingServiceSpec extends Specification {

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

    void "test extracting match string from resolver URI"() {
        when: 'My resolver url is http://blah/blah'
        service.grailsApplication.config = [mapper:[resolverURL: 'http://blah/blah']]
        String extract = service.extractMatchStringFromResolverURI('http://blah/blah/name/space/12345')

        then:
        extract == 'name/space/12345'

        when: 'I try a https address'
        String extract2 = service.extractMatchStringFromResolverURI('https://blah/blah/name/space/12345')

        then: 'I get the same result'
        extract2 == 'name/space/12345'

        when: 'My resolver url is https://blah/blah'
        service.grailsApplication.config = [mapper:[resolverURL: 'https://blah/blah']]
        String extract3 = service.extractMatchStringFromResolverURI('http://blah/blah/name/space/12345')

        then:
        extract3 == 'name/space/12345'

        when: 'I try a https address'
        String extract4 = service.extractMatchStringFromResolverURI('https://blah/blah/name/space/12345')

        then: 'I get the same result'
        extract4 == 'name/space/12345'
    }


}

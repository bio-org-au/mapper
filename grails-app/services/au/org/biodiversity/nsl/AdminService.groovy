package au.org.biodiversity.nsl

import grails.transaction.Transactional
import groovy.transform.Synchronized
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

@Transactional
class AdminService {

    @Synchronized
    Identifier addIdentifier(String nameSpace, String objectType, Long idNumber, Long versionNumber) {
        Identifier identifier = new Identifier(nameSpace: nameSpace, objectType: objectType, idNumber: idNumber, version: versionNumber)
        Match match = new Match(uri: identifier.toUrn())
        identifier.addToIdentities(match)
        identifier.preferredUri = match
        if (identifier.validate() && match.validate()) {
            identifier.save(flush: true)
            Host host = Host.findByPreferred(true)
            if (host) {
                match.addToHosts(host)
                match.save()
            }
            return identifier
        } else {
            List<String> errors = ["Error trying to add identifier nameSpace: $nameSpace, objectType: $objectType, idNumber: $idNumber, version: $versionNumber".toString()]
            if (identifier.errors.hasErrors()) {
                errors.add('Identifier: ' + buildErrorString(identifier.errors))
            }
            if (match.errors.hasErrors()) {
                errors.add('URI: ' + buildErrorString(match.errors))
            }
            throw new ServiceException(errors.join(', \n'))
        }
    }

    private static String buildErrorString(Errors errors) {
        errors.fieldErrors.collect { FieldError e ->
            "${e.field} cannot be ${e.rejectedValue}."
        }.join(', \n')
    }
}

package au.org.biodiversity.nsl

import grails.transaction.Transactional
import groovy.transform.Synchronized
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

@Transactional
class AdminService {

    @Synchronized
    Identifier addIdentifier(String nameSpace, String objectType, Long idNumber, Long versionNumber) {
        Identifier identifier = new Identifier(nameSpace: nameSpace, objectType: objectType, idNumber: idNumber, versionNumber: versionNumber)
        Match match = new Match(uri: identifier.toUrn())
        identifier.addToIdentities(match)
        identifier.preferredUri = match
        match.addToIdentifiers(identifier)
        if (identifier.validate() && match.validate()) {
            identifier.save(flush: true)
            Host host = Host.findByPreferred(true)
            if (host) {
                match.addToHosts(host)
                match.save()
            }
            return identifier
        } else {
            List<String> errors = ["Error trying to add identifier nameSpace: $nameSpace, objectType: $objectType, idNumber: $idNumber, versionNumber: $versionNumber".toString()]
            if (identifier.errors.hasErrors()) {
                errors.add('Identifier: ' + buildErrorString(identifier.errors))
            }
            if (match.errors.hasErrors()) {
                errors.add('URI: ' + buildErrorString(match.errors))
            }
            throw new ServiceException(errors.join(', \n'))
        }
    }

    /**
     * This permanently removes an identifier and it's exclusive Matches. It can't be undone and if someone tries to
     * resolve a match on this identity they will get a 404 - this is meant for draft identities only
     * @param identifier
     */
    @Synchronized
    void removeIdentifier(Identifier identifier) {
        identifier.identities.each { Match match ->
            identifier.removeFromIdentities(match)
            match.removeFromIdentifiers(identifier)
            if (match.identifiers.size() == 0) {
                log.debug "$match has ${match.identifiers.size()} identifiers so deleting"
                match.hosts.each { Host host ->
                    match.removeFromHosts(host)
                }
                match.delete()
            }
        }
        identifier.preferredUri = null
        identifier.delete()
    }

    private static String buildErrorString(Errors errors) {
        errors.fieldErrors.collect { FieldError e ->
            "${e.field} cannot be ${e.rejectedValue}."
        }.join(', \n')
    }
}

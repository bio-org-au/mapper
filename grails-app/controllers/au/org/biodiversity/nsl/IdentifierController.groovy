package au.org.biodiversity.nsl


import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class IdentifierController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Identifier.list(params), model: [identifierInstanceCount: Identifier.count()]
    }

    def show(Identifier identifierInstance) {
        respond identifierInstance
    }

    def create() {
        respond new Identifier(params)
    }

    @Transactional
    def save(Identifier identifierInstance) {
        if (identifierInstance == null) {
            notFound()
            return
        }

        if (identifierInstance.hasErrors()) {
            respond identifierInstance.errors, view: 'create'
            return
        }

        identifierInstance.save flush: true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'identifier.label', default: 'Identifier'), identifierInstance.id])
                redirect identifierInstance
            }
            '*' { respond identifierInstance, [status: CREATED] }
        }
    }

    def edit(Identifier identifierInstance) {
        respond identifierInstance
    }

    @Transactional
    def update(Identifier identifierInstance) {
        if (identifierInstance == null) {
            notFound()
            return
        }

        if (identifierInstance.hasErrors()) {
            respond identifierInstance.errors, view: 'edit'
            return
        }

        identifierInstance.save flush: true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'Identifier.label', default: 'Identifier'), identifierInstance.id])
                redirect identifierInstance
            }
            '*' { respond identifierInstance, [status: OK] }
        }
    }

    @Transactional
    def delete(Identifier identifierInstance) {

        if (identifierInstance == null) {
            notFound()
            return
        }

        identifierInstance.delete flush: true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'Identifier.label', default: 'Identifier'), identifierInstance.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'identifier.label', default: 'Identifier'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}

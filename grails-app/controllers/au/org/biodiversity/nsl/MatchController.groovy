package au.org.biodiversity.nsl


import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class MatchController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Match.list(params), model: [matchInstanceCount: Match.count()]
    }

    def show(Match matchInstance) {
        respond matchInstance
    }

    def create() {
        respond new Match(params)
    }

    @Transactional
    def save(Match matchInstance) {
        if (matchInstance == null) {
            notFound()
            return
        }

        if (matchInstance.hasErrors()) {
            respond matchInstance.errors, view: 'create'
            return
        }

        matchInstance.save flush: true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'match.label', default: 'Match'), matchInstance.id])
                redirect matchInstance
            }
            '*' { respond matchInstance, [status: CREATED] }
        }
    }

    def edit(Match matchInstance) {
        respond matchInstance
    }

    @Transactional
    def update(Match matchInstance) {
        if (matchInstance == null) {
            notFound()
            return
        }

        if (matchInstance.hasErrors()) {
            respond matchInstance.errors, view: 'edit'
            return
        }

        matchInstance.save flush: true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'Match.label', default: 'Match'), matchInstance.id])
                redirect matchInstance
            }
            '*' { respond matchInstance, [status: OK] }
        }
    }

    @Transactional
    def delete(Match matchInstance) {

        if (matchInstance == null) {
            notFound()
            return
        }

        matchInstance.delete flush: true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'Match.label', default: 'Match'), matchInstance.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'match.label', default: 'Match'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}

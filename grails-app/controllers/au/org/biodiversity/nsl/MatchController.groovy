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

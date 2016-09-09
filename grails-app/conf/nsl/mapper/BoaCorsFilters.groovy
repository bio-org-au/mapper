package nsl.mapper

/**
 * For some reason the regular grails plugin isn't being loaded - not sure why.
 *
 * The requirement is simple enough that this filter can do the job.
 */

class BoaCorsFilters {

    def filters = {
        corsFilter(controller:'*', action:'*') {
            before = {
                response.setHeader('Access-Control-Allow-Origin', '*')
                response.setHeader('Access-Control-Allow-Headers', 'authorization, content-type')
            }
        }
    }
}

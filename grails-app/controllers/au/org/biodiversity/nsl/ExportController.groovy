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

import grails.transaction.Transactional
import groovy.sql.Sql
import org.postgresql.PGConnection
import org.postgresql.copy.CopyManager

import java.sql.Connection

@Transactional
class ExportController {

    def grailsApplication

    @SuppressWarnings("GroovyUnusedDeclaration")
    static responseFormats = ['json']

    static allowedMethods = [
            export               : ["GET"]
    ]

    private Sql getNSL() {
        String dbUrl = grailsApplication.config.dataSource.url
        String username = grailsApplication.config.dataSource.username
        String password = grailsApplication.config.dataSource.password
        String driverClassName = grailsApplication.config.dataSource.driverClassName
        Sql.newInstance(dbUrl, username, password, driverClassName)
    }

    def index() {
        List<String> objectTypes = Identifier.executeQuery("select distinct(objectType) from Identifier") as List<String>
        [objectTypes: objectTypes]
    }

    def identifiersByType(String type) {

        log.debug "Export identifier sets for $type"
        List<String> objectTypes = Identifier.executeQuery("select distinct(objectType) from Identifier") as List<String>
        String where = ''
        if(objectTypes.contains(type)){
            where = "where i.object_type = '$type'"
        } else {
            log.debug "Type $type unknown exporting all"
            type = 'All'
        }

        final Date date = new Date()
        final String tempFileDir = grailsApplication.config.mapper.temp.file.directory
        final String fileName = "mapper-export-${type}-${date.format('yyyy-MM-dd-mmss')}.csv"
        final File outputFile = new File(tempFileDir, fileName)

        final String query = """copy (SELECT i.object_type, i.name_space, i.id_number, i.deleted, i.reason_deleted, jsonb_agg(h.host_name || '/' || m.uri) as identifiers
FROM mapper.identifier i
       JOIN mapper.identifier_identities mi ON i.id = mi.identifier_id
       JOIN mapper.match m ON mi.match_id = m.id and m.deprecated = false
       JOIN mapper.match_host hm ON m.id = hm.match_hosts_id
       JOIN mapper.host h ON hm.host_id = h.id
       ${where}
group by i.id
order BY i.name_space, i.object_type, i.id_number) to STDOUT WITH CSV HEADER"""

        log.debug query
        Sql sql = getNSL()
        Connection connection = sql.getConnection()
        connection.setAutoCommit(false)
        CopyManager copyManager = ((PGConnection) connection).getCopyAPI()
        copyManager.copyOut(query, new FileWriter(outputFile))
        sql.close()

        render(file: outputFile, fileName: outputFile.name, contentType: 'text/csv')
    }

}

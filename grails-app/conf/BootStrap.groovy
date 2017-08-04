import au.org.biodiversity.nsl.DbVersion
import groovy.sql.Sql
import groovy.text.SimpleTemplateEngine
import org.hibernate.SessionFactory

import javax.sql.DataSource

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

class BootStrap {
    def shiroSecurityManager
    def shiroSubjectDAO
    SessionFactory sessionFactory
    DataSource dataSource
    private static int CURRENT_DB_VERSION = 5

    def init = { servletContext ->
        if (shiroSecurityManager) {
            shiroSecurityManager.setSubjectDAO(shiroSubjectDAO)
            println "Set subject DAO on security manager."
        }
        if (checkUpToDate()) {
            log.info "Database is up to date."
        } else {
            Sql sql = new Sql(dataSource)
            try {
                if (!updateToCurrentVersion(sql, [:])) {
                    log.error "Database is not up to date. Run update script on the DB before restarting."
                    throw new Exception('Database not at expected version.')
                }
            }
            catch (e) {
                log.error "$e.message : Database is not up to date. Run update script on the DB before restarting."
                throw new Exception('Database not at expected version.')
            }
        }
    }
    def destroy = {
    }

    /**
     * update the database to the current version using update scripts
     * @return true if worked
     */
    private Boolean updateToCurrentVersion(Sql sql, Map params) {
        Integer dbVersion = DbVersion.get(1)?.version
        if (!dbVersion) {
            log.error "Database version not found, not updating."
            return false
        }
        sessionFactory.getCurrentSession().flush()
        sessionFactory.getCurrentSession().clear()
        for (Integer versionNumber in ((dbVersion + 1)..CURRENT_DB_VERSION)) {
            log.info "updating to version $versionNumber"
            File updateFile = getUpdateFile(versionNumber)
            if (updateFile?.exists()) {
                String sqlSource = updateFile.text
                def engine = new SimpleTemplateEngine()
                def template = engine.createTemplate(sqlSource).make(params)
                log.debug template
                sql.execute(template.toString()) { isResultSet, result ->
                    if (isResultSet) log.debug result
                }
            }
        }
        sessionFactory.getCurrentSession().flush()
        sessionFactory.getCurrentSession().clear()
        return checkUpToDate()
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private Boolean checkUpToDate() {
        try {
            DbVersion.get(1)?.version == CURRENT_DB_VERSION
        } catch (e) {
            log.error e.message
            return false
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private File getUpdateFile(Integer versionNumber) {
        File file = new File("web-app/sql/update-${versionNumber}.sql")
        log.info "mapper-ddl.sql file path $file.absolutePath"
        return file
    }

}

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

import static java.sql.Connection.*

hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
    singleSession = true // configure OSIV singleSession mode
    flush.mode = 'manual' // OSIV session flush mode outside of transactional context
}

// environment specific settings
//The configuration below is a default for testing and should be overridden in the ~/.nsl/nsl-mapper-config.groovy file

environments {
    development {
        hibernate {
            default_schema = 'mapper'
        }
        dataSource {

            pooled = true
            driverClassName = "org.postgresql.Driver"
            username = "nsldev"
            password = "nsldev"
            dialect = "org.hibernate.dialect.PostgreSQLDialect"
            url = "jdbc:postgresql://localhost:5432/nsl"
            formatSql = false
            logSql = false
            //noinspection GroovyAssignabilityCheck
            properties {
                defaultTransactionIsolation = TRANSACTION_READ_UNCOMMITTED
                initialSize = 2
                maxActive = 5
                minEvictableIdleTimeMillis = 1800000
                timeBetweenEvictionRunsMillis = 1800000
                numTestsPerEvictionRun = 3
                testOnBorrow = true
                testWhileIdle = true
                testOnReturn = true
                validationQuery = "SELECT 1"
            }
        }
    }
    test {
        dataSource {
            pooled = true
            jmxExport = true
            driverClassName = "org.h2.Driver"
            username = "sa"
            password = ""
            dbCreate = "create"
            dialect = "org.hibernate.dialect.H2Dialect"
            url = "jdbc:h2:mem:mapperTestDb"
        }
    }
    production {
        hibernate {
            default_schema = 'mapper'
        }
        dataSource {
            pooled = true
            driverClassName = "org.postgresql.Driver"
            username = "nsldev"
            password = "nsldev"
            dialect = "org.hibernate.dialect.PostgreSQLDialect"
            url = "jdbc:postgresql://localhost:5432/nsl"
            formatSql = false
            logSql = false
            //noinspection GroovyAssignabilityCheck
            properties {
                defaultTransactionIsolation = TRANSACTION_READ_UNCOMMITTED
                initialSize = 2
                maxActive = 10
                minEvictableIdleTimeMillis = 1800000
                timeBetweenEvictionRunsMillis = 1800000
                numTestsPerEvictionRun = 3
                testOnBorrow = true
                testWhileIdle = true
                testOnReturn = true
                validationQuery = "SELECT 1"
            }
        }
    }
}

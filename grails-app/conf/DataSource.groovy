dataSource {
    pooled = true
    jmxExport = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
    singleSession = true // configure OSIV singleSession mode
    flush.mode = 'manual' // OSIV session flush mode outside of transactional context
    default_schema = 'mapper'
}

// environment specific settings
//The configuration below is a default for testing and should be overridden in the ~/.nsl/nsl-mapper-config.groovy file

environments {
    development {
        dataSource {
            dbCreate = "update"

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
                defaultTransactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED
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
            dbCreate = "update"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
        }
    }
    production {
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
                defaultTransactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED
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

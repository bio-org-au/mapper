// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

grails.config.locations = ["file:${userHome}/.nsl/nsl-mapper-config.groovy"]

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination

grails.mime.use.accept.header = true
// The ACCEPT header will not be used for content negotiation for user agents containing the following strings (defaults to the 4 major rendering engines)
grails.mime.disable.accept.header.userAgents = ['blah']//['Gecko', 'WebKit', 'Presto', 'Trident']
grails.mime.types = [ // the first one is the default format
                      all          : '*/*', // 'all' maps to '*' or the first available format in withFormat
                      atom         : 'application/atom+xml',
                      css          : 'text/css',
                      csv          : 'text/csv',
                      form         : 'application/x-www-form-urlencoded',
                      html         : ['text/html', 'application/xhtml+xml'],
                      js           : 'text/javascript',
                      json         : ['application/json', 'text/json'],
                      multipartForm: 'multipart/form-data',
                      rss          : 'application/rss+xml',
                      text         : 'text/plain',
                      hal          : ['application/hal+json', 'application/hal+xml'],
                      xml          : ['text/xml', 'application/xml'],
                      rdf          : ['application/rdf+xml', 'application/x-turtle']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// Legacy setting for codec used to encode data with ${}
grails.views.default.codec = "html"

// The default scope for controllers. May be prototype, session or singleton.
// If unspecified, controllers are prototype scoped.
grails.controllers.defaultScope = 'singleton'

// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside ${}
                scriptlet = 'html' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        // filteringCodecForContentType.'text/html' = 'html'
    }
}


grails.converters.encoding = "UTF-8"
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

// configure passing transaction's read-only attribute to Hibernate session, queries and criterias
// set "singleSession = false" OSIV mode in hibernate configuration after enabling
grails.hibernate.pass.readonly = false
// configure passing read-only to OSIV session by default, requires "singleSession = false" OSIV mode
grails.hibernate.osiv.readonly = false

grails.gorm.failOnError = true

environments {
    development {
        grails.logging.jul.usebridge = true
        grails.serverURL = "http://localhost:7070"
    }
    production {
        grails.logging.jul.usebridge = false
        grails.serverURL = "https://biodiversity.org.au"
    }
}

// log4j configuration
log4j.main = {
    // Example of changing the log pattern for the default console appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error 'org.codehaus.groovy.grails.web.servlet',        // controllers
            'org.codehaus.groovy.grails.web.pages',          // GSP
            'org.codehaus.groovy.grails.web.sitemesh',       // layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping',        // URL mapping
            'org.codehaus.groovy.grails.commons',            // core / classloading
            'org.codehaus.groovy.grails.plugins',            // plugins
            'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate'
    debug 'grails.app'
}

//The configuration below is a default for testing and should be overridden in the ~/.nsl/nsl-mapper-config.groovy file

mapper {
    resolverURL = 'http://localhost:7070/nsl-mapper/boa'
    shards = [apni: [
            baseURL: 'http://localhost:8080',
            service: [
                    html: { ident ->
                        "services/api/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                    },
                    json: { ident ->
                        "services/api/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                    },
                    xml : { ident ->
                        "services/api/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                    },
                    rdf : { ident ->
                        "d2rq/resource?uri=http://biodiversity.org.au/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                    }
            ]
    ],
              foa : [
                      baseURL: 'http://biodiversity.org.au/',
                      service: [
                              html: { ident ->
                                  "foa/taxa/${ident.idNumber}/summary"
                              },
                      ]
              ]
    ]
}

ldap {
    domain = 'domain'
    server.url = 'ldap://localhost:10389'
    search {
        base = 'ou=users,dc=nsl,dc=bio,dc=org,dc=au'
        user = 'uid=admin,ou=system'
        pass = 'secret'

        group {
            name = 'ou=groups,dc=nsl,dc=bio,dc=org,dc=au'
            member {
                element = 'uniqueMember'
                prefix = 'uid='
                postfix = ''
            }
        }
    }
}

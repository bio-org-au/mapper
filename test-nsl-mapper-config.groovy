
grails.config.locations = ["file:test-nsl-mapper-config.groovy"]

grails.serverURL = 'http://localhost:7070/nsl-mapper'

mapper {
    resolverURL = 'http://localhost:7070/nsl-mapper/boa'
    contextExtension = 'boa' //extension to the context path (after nsl-mapper).

    shards = [
            apni   : [
                    baseURL: 'http://localhost:8080',
                    service: [
                            html: { ident ->
                                "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                            },
                            json: { ident ->
                                "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                            },
                            xml : { ident ->
                                "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                            },
                            rdf : { ident ->
                                String url = "DESCRIBE <http://biodiversity.org.au/boa/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}>".encodeAsURL()
                                "sparql/?query=${url}"
                            }
                    ]
            ],
            ausmoss: [
                    baseURL: 'http://localhost:8080',
                    service: [
                            html: { ident ->
                                "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                            },
                            json: { ident ->
                                "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                            },
                            xml : { ident ->
                                "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                            },
                            rdf : { ident ->
                                String url = "DESCRIBE <http://biodiversity.org.au/boa/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}>".encodeAsURL()
                                "sparql/?query=${url}"
                            }
                    ]
            ],
            foa    : [
                    baseURL: 'http://biodiversity.org.au/',
                    service: [
                            html: { ident ->
                                "foa/taxa/${ident.idNumber}/summary"
                            }
                    ]
            ],
blah: [
        baseURL: ('http://blahg.org'),
        service: [
                html: { ident ->
                    "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                },
                json: { ident ->
                    "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                },
                xml : { ident ->
                    "services/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
                },
                rdf : { ident ->
                    String url = "DESCRIBE <http://biodiversity.org.au/boa/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}>".encodeAsURL()
                    "sparql/?query=${url}"
                }
        ]
]]
}

api.auth = [
        'blah-blah-blah-blah-blah': [
                application: 'apni-services',
                roles      : ['admin'],
                host       : '127.0.0.1'
        ]
]

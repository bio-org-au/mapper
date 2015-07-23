class UrlMappings {

	static mappings = {

        "/boa/**"(controller: 'broker', action: 'index', method: "GET")

        "/links/$nameSpace/$objectType/$idNumber"(controller: 'broker', action: 'links', method: "GET")

        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }


        "/"(view:"/index")
        "500"(controller: 'error', action: 'index')

	}
}

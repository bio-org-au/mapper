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

class UrlMappings {

    static excludes = ["/metrics/*"]
	static mappings = {

        "/**"(controller: 'broker', action: 'index')

        "/links/$nameSpace/$objectType/$idNumber"(controller: 'broker', action: 'links')

        "/admin/moveIdentity"(controller: 'admin', action: 'moveIdentityPost')

        "/robots.txt" (view: "/robots")

        "/**/robots.txt" (view: "/robots")

        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(controller: 'error', action: 'index')

	}
}

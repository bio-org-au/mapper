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

import org.apache.shiro.authc.UnknownAccountException

/**
 * User: pmcneil
 * Date: 3/06/15
 *
 */
class ApiRealm {

    static authTokenClass = ApiKeyToken
    def grailsApplication

    def authenticate(ApiKeyToken authToken) {
        log.info "trying API Realm login for ${authToken}"
        Map details = getDetails(authToken.key)
        if (details) {
            if(details.host) { //if host is set then ensure it matches
                if(details.host == authToken.host){
                    return [details.application, 'api']
                }
                throw new UnknownAccountException("No account found for api user [${authToken.key}]")
            }
            return [details.application, 'api']
        }
        throw new UnknownAccountException("No account found for api user [${authToken.key}]")
    }

    private Map getDetails(String key) {
        if (grailsApplication.config.api?.auth instanceof Map) {
            Map apiAuth = (grailsApplication.config.api.auth as Map)
            return apiAuth[key] as Map
        }
        return null
    }

    private Map getDetailByPrincipal(String principal) {
        if (grailsApplication.config.api?.auth instanceof Map) {
            Map apiAuth = (grailsApplication.config.api.auth as Map)
            Map.Entry entry = apiAuth.find { k, v -> v.application == principal }
            return entry ? entry.value as Map : null
        }
        return null
    }

    Boolean hasRole(principal, String roleName) {
        Map details = getDetailByPrincipal(principal)
        details?.roles && details.roles.contains(roleName)
    }

    Boolean hasAllRoles(principal, roles) {
        Map details = getDetailByPrincipal(principal)
        if (details?.roles) {
            details.roles.containsAll(roles)
        }
    }

}
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

import org.apache.shiro.authc.*
import org.apache.shiro.authz.permission.WildcardPermission

import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.NamingException
import javax.naming.directory.*

/**
 * Simple realm that authenticates users against an LDAP server.
 */
class LdapRealm {
    static authTokenClass = UsernamePasswordToken

    def grailsApplication

    String ldapURL = null

    String authenticate(UsernamePasswordToken authToken) {
        log.info "Attempting to authenticate ${authToken.username} in LDAP realm..."
        String username = authToken.username
        String password = new String(authToken.password)

        // Get LDAP config for application. Use defaults when no config
        // is provided.
        ConfigObject appConfig = grailsApplication.config
        String searchBase = appConfig.ldap.search.base ?: ""
        String usernameAttribute = appConfig.ldap.username.attribute ?: "uid"
        Boolean skipAuthc = appConfig.ldap.skip.authentication ?: false
        Boolean skipCredChk = appConfig.ldap.skip.credentialsCheck ?: false
        Boolean allowEmptyPass = appConfig.ldap.allowEmptyPasswords != [:] ? appConfig.ldap.allowEmptyPasswords : true

        // Skip authentication ?
        if (skipAuthc) {
            log.info "Skipping authentication in development mode."
            return username
        }

        // Null username is invalid
        if (username == null) {
            throw new AccountException("Null usernames are not allowed by this realm.")
        }

        // Empty username is invalid
        if (username == "") {
            throw new AccountException("Empty usernames are not allowed by this realm.")
        }

        // Allow empty passwords ?
        if (!allowEmptyPass) {
            // Null password is invalid
            if (password == null) {
                throw new CredentialsException("Null password are not allowed by this realm.")
            }

            // empty password is invalid
            if (password == "") {
                throw new CredentialsException("Empty passwords are not allowed by this realm.")
            }
        }

        ldapSearch { InitialDirContext ctx, String ldapUrl ->

            // Look up the DN for the LDAP entry that has a 'uid' value
            // matching the given username.
            def matchAttrs = new BasicAttributes(true)
            matchAttrs.put(new BasicAttribute(usernameAttribute, username))

            NamingEnumeration<SearchResult> result = ctx.search(searchBase, matchAttrs)
            if (!result.hasMore()) {
                throw new UnknownAccountException("No account found for user [${username}]")
            }

            // Skip credentials check ?
            if (skipCredChk) {
                log.info "Skipping credentials check in development mode."
                return username
            }

            //check we can log in as the user we just found using the password supplied
            SearchResult searchResult = result.next()
            try {
                getLDAPContext(searchResult.nameInNamespace, password, ldapUrl)
                //we don't care about the context, just that we don't get an exception from logging in
                return username
            } catch (AuthenticationException ex) {
                log.info "Invalid password $ex.message"
                throw new IncorrectCredentialsException("Invalid password for user '${username}'")
            }
        }
    }

    private String getCNForUserName(String userName) {
        String searchBase = grailsApplication.config.ldap.search.base ?: ""
        String usernameAttribute = grailsApplication.config.ldap.username.attribute ?: "uid"

        ldapSearch { InitialDirContext ctx, String ldapUrl ->

            // Look up the DN for the LDAP entry that has a 'uid' value
            // matching the given username.
            def matchAttrs = new BasicAttributes(true)
            matchAttrs.put(new BasicAttribute(usernameAttribute, userName))

            NamingEnumeration<SearchResult> result = ctx.search(searchBase, matchAttrs)
            if (!result.hasMore()) {
                return null
            }

            SearchResult searchResult = result.next()
            return searchResult.nameInNamespace
        }
    }


    private String findLDAPServerUrlToUse(String user, String password) {
        if (ldapURL) {
            return ldapURL
        }

        def configLdapUrls = grailsApplication.config.ldap.server.url ?: ["ldap://localhost:389/"]

        // Accept strings and GStrings for convenience, but convert to list
        List<String> ldapUrls = []
        if (configLdapUrls && (configLdapUrls instanceof String)) {
            ldapUrls = [(configLdapUrls as String)]
        } else if (configLdapUrls instanceof Collection) {
            ldapUrls = configLdapUrls as List<String>
        }

        // Set up the configuration for the LDAP search we are about to do.
        Hashtable env = getBaseLDAPEnvironment(user, password)

        // Find an LDAP server that we can connect to.
        InitialDirContext ctx = null
        String urlUsed = ldapUrls.find { url ->
            log.info "Trying LDAP server ${url} ..."
            env[Context.PROVIDER_URL] = url

            // If an exception occurs, log it.
            try {
                ctx = new InitialDirContext(env)
                return true
            }
            catch (NamingException e) {
                log.error "Could not connect to ${url}: ${e}"
                return false
            }
        }
        ldapURL = urlUsed
        return urlUsed
    }

    private static InitialDirContext getLDAPContext(String user, String password, String ldapUrl) {
        // Set up the configuration for the LDAP search we are about to do.
        Hashtable env = getBaseLDAPEnvironment(user, password)
        env[Context.PROVIDER_URL] = ldapUrl
        return new InitialDirContext(env)
    }

    private static Hashtable getBaseLDAPEnvironment(String user, String password) {
        def env = new Hashtable()
        env[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
        if (user) {
            // Non-anonymous access for the search.
            env[Context.SECURITY_AUTHENTICATION] = "simple"
            env[Context.SECURITY_PRINCIPAL] = user
            env[Context.SECURITY_CREDENTIALS] = password
        }
        return env
    }

    private def ldapSearch(Closure work) {
        String searchUser = grailsApplication.config.ldap.search.user ?: ""
        String searchPass = grailsApplication.config.ldap.search.pass ?: ""

        String ldapUrl = findLDAPServerUrlToUse(searchUser, searchPass)
        if (ldapUrl) {
            InitialDirContext ctx = getLDAPContext(searchUser, searchPass, ldapUrl)
            return work(ctx, ldapUrl)
        } else {
            throw new AuthenticationException("No LDAP server available.")
        }
    }

    Boolean hasRole(principal, roleName) {
        Map searchGroup = grailsApplication.config.ldap.search.group
        if(!searchGroup) {
            log.error "No LDAP search group in config."
            return false
        }
        String searchGroupName = searchGroup.name ?: ''
        String searchGroupMember = searchGroup.member.element ?: ''
        String memberString = "${searchGroup.member.prefix}$principal${searchGroup.member.postfix}"

        return ldapSearch { InitialDirContext ctx, String ldapUrl ->
            def matchAttrs = new BasicAttributes(true)
            matchAttrs.put(new BasicAttribute('cn', roleName))

            NamingEnumeration<SearchResult> result = ctx.search(searchGroupName, matchAttrs)
            if (!result.hasMore()) {
                log.error "Unknown role $roleName"
                return false
            }
            SearchResult group = result.next()
            Attribute uniqueMember = group.getAttributes().all.find { attribute ->
                attribute.getID() == searchGroupMember
            } as Attribute

            if (uniqueMember) {
                String foundPrincipalInGroup = uniqueMember.all.find { String val ->
                    val == memberString
                }
                if (foundPrincipalInGroup) {
                    return true
                }
            }
            return false
        }
    }

    Boolean hasAllRoles(principal, roles) {
        def role = roles.find { role ->
            !hasRole(principal, role)
        }
        return role == null
    }

//    def isPermitted(principal, WildcardPermission requiredPermission) {
//        return requiredPermission.parts.size() > 0 && hasRole(principal, requiredPermission.parts[0])

        // Does the user have the given permission directly associated
        // with himself?
        //
        // First find all the permissions that the user has that match
        // the required permission's type and project code.
//    }

}

class BootStrap {
    def shiroSecurityManager
    def shiroSubjectDAO

    def init = { servletContext ->
        if(shiroSecurityManager) {
            shiroSecurityManager.setSubjectDAO(shiroSubjectDAO)
            println "Set subject DAO on security manager."
        }
    }
    def destroy = {
    }
}

import au.org.biodiversity.nsl.ApiSessionStorageEvaluator
import org.apache.shiro.mgt.DefaultSubjectDAO

// Place your Spring DSL code here
beans = {
    shiroSessionStorageEvaluator(ApiSessionStorageEvaluator)

    shiroSubjectDAO(DefaultSubjectDAO) { bean ->
        sessionStorageEvaluator = ref("shiroSessionStorageEvaluator")
    }
}

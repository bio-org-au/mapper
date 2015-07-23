package au.org.biodiversity.nsl

import org.apache.shiro.mgt.SessionStorageEvaluator
import org.apache.shiro.subject.Subject

/**
 * User: pmcneil
 * Date: 3/06/15
 *
 */
class ApiSessionStorageEvaluator implements SessionStorageEvaluator {

    /**
     * if the subject has a principal called 'api' then don't create or enable a session
     * @param subject
     * @return true if a session is required
     */
    @Override
    boolean isSessionStorageEnabled(Subject subject) {
        String api = subject.principals.find { it == 'api' }
        if (api) {
            return false
        }
        return true
    }
}

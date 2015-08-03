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

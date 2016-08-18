/*
    Copyright 2015 Australian National Botanic Gardens

    This file is part of NSL-domain-plugin.

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

/**
 * There should only ever be a single db_version record in the database.
 * This record is updated to the current version when an update script is run to update the data model in the database.
 * Versions are simple serial Integers e.g 1,2,3,4... etc
 */
class DbVersion {

    Integer version

    static mapping = {
    }

    static constraints = {
    }
}

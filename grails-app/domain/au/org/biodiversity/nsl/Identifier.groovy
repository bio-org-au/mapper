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

import grails.util.Environment
import org.apache.shiro.SecurityUtils

import java.sql.Timestamp

class Identifier {

    String nameSpace
    String objectType
    Long idNumber
    Long versionNumber
    Boolean deleted = false
    String reasonDeleted
    Timestamp updatedAt
    String updatedBy
    Match preferredUri

    static hasMany = [identities: Match]

    static mapping = {
        version false
        id generator: 'native', params: [sequence: 'mapper_sequence'], defaultValue: "nextval('mapper.mapper_sequence')"

        deleted defaultvalue: "false"

        nameSpace index: 'identifier_index'
        objectType index: 'identifier_index'
        idNumber index: 'identifier_index'
        if (Environment.current != Environment.TEST) { // test uses H2 so it doesn't understand this
            updatedAt sqlType: 'timestamp with time zone'
        }
    }

    static constraints = {
        nameSpace unique: ['objectType', 'idNumber', 'versionNumber']
        reasonDeleted nullable: true
        updatedAt nullable: true
        updatedBy nullable: true
        preferredUri nullable: true
        versionNumber nullable: true
    }

    def beforeInsert() {
        updatedAt = new Timestamp(System.currentTimeMillis())
        updatedBy = SecurityUtils.subject?.getPrincipal()?.toString()
    }

    def beforeUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis())
        updatedBy = SecurityUtils.subject?.getPrincipal()?.toString()
    }

    @Override
    String toString() {
        "$id: $nameSpace, $objectType, $idNumber, $versionNumber"
    }

    String toUrn() {
        if(versionNumber) {
            return "$objectType/$versionNumber/$idNumber"
        }
        return "$objectType/$nameSpace/$idNumber"
    }

}

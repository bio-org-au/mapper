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

import org.apache.shiro.SecurityUtils

import java.sql.Timestamp

class Match {

    String uri
    Boolean deprecated = false
    Timestamp updatedAt
    String updatedBy

    static belongsTo = Identifier
    static hasMany = [identifiers: Identifier]

    static mapping = {
        version false
        id generator: 'native', params: [sequence: 'mapper_sequence'], defaultValue: "nextval('mapper.mapper_sequence')"
        uri index: 'identity_uri_index', unique: true
        deprecated defaultvalue: "false"
        updatedAt sqlType: 'timestamp with time zone'
    }

    static constraints = {
        uri unique: true
        updatedAt nullable: true
        updatedBy nullable: true
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
    public String toString() {
        "$id: $uri"
    }
}

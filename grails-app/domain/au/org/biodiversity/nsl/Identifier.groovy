package au.org.biodiversity.nsl

import java.sql.Timestamp

class Identifier {

    String nameSpace
    String objectType
    Long idNumber
    Boolean deleted = false
    String reasonDeleted
    Timestamp updatedAt
    String updatedBy

    static hasMany = [identities: Match]

    static mapping = {
        version false
        id generator: 'native', params: [sequence: 'mapper_sequence'], defaultValue: "nextval('mapper.mapper_sequence')"

        deleted defaultvalue: "false"

        nameSpace index: 'identifier_index'
        objectType  index: 'identifier_index'
        idNumber index: 'identifier_index'
        updatedAt sqlType: 'timestamp with time zone'

    }

    static constraints = {
        nameSpace unique: ['objectType','idNumber']
        reasonDeleted nullable: true
        updatedAt nullable: true
        updatedBy nullable: true
    }

    @Override
    String toString() {
        "$id: $nameSpace, $objectType, $idNumber"
    }

    String toUrn() {
        "$objectType/$nameSpace/$idNumber"
    }
}

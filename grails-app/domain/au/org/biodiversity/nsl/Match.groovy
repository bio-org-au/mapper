package au.org.biodiversity.nsl

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

    @Override
    public String toString() {
        "$id: $uri"
    }
}

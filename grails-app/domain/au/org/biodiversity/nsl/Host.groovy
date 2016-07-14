package au.org.biodiversity.nsl

class Host {

    String hostName //e.g. https://id.biodiversity.org.au

    static hasMany = [matches: Match]

    static mapping = {
        version false
        id generator: 'native', params: [sequence: 'mapper_sequence'], defaultValue: "nextval('mapper.mapper_sequence')"
    }

    static constraints = {
        hostName nullable: false, maxSize: 512
    }
}

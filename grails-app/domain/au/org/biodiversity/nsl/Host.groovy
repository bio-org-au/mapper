package au.org.biodiversity.nsl

class Host {

    String hostName //e.g. https://id.biodiversity.org.au
    Boolean preferred

    static mapping = {
        version false
        id generator: 'native', params: [sequence: 'mapper_sequence'], defaultValue: "nextval('mapper.mapper_sequence')"
        preferred defaultvalue: "false"
    }

    static constraints = {
        hostName nullable: false, maxSize: 512
    }
}

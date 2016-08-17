File links = new File("/home/pmcneil/tmp/test-urls.txt")
links.eachLine { String line ->

    try {
        line.toURL().getText(requestProperties: [Accept: 'application/json'])
    } catch (e) {
        println e.message
    }

}
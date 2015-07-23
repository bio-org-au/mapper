includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsArgParsing")

target(setWarPath: "set the context path of the war file for Tomcat deploys.") {

    depends(parseArguments)

    if (argsMap["params"].size() != 1) {
        event("StatusError", [statusError])
        return
    }

    File targetDir = new File('target')
    String warPath = argsMap.params[0].toString()

    if(targetDir.exists() && targetDir.isDirectory()) {
        File[] warFiles = targetDir.listFiles(new FilenameFilter() {
            @Override
            boolean accept(File dir, String name) {
                return name.endsWith('.war')
            }
        })
        warFiles.each { File warFile ->
            String path = warFile.canonicalPath - warFile.name
            warFile.renameTo("${path}/${warPath}#${warFile.name - 'nsl-'}")
        }
    }
}

setDefaultTarget(setWarPath)

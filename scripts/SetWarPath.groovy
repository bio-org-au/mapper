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

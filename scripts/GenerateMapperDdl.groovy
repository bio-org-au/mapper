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
includeTargets << grailsScript("_GrailsInit")
includeTargets << new File("${grailsSettings.projectPluginsDir}/hibernate4-4.3.6.1/scripts/SchemaExport.groovy")

target(main: "Generate the Mapper ddl sql from the current schema including views") {
    schemaExport()
    File ddl = new File("${grailsSettings.projectTargetDir}/ddl.sql")
    String text = ddl.text
                     .replaceAll(/alter table/, 'alter table if exists')
                     .replaceAll(/drop constraint/, 'drop constraint if exists')
                     .replaceAll(/boolean not null/, 'boolean default false not null')
                     .replaceAll(/create sequence mapper.mapper_sequence;/, '')
                     .replaceAll(/drop sequence mapper.mapper_sequence;/, 'drop sequence mapper.mapper_sequence;\n    create sequence mapper.mapper_sequence;')
                     .replaceAll(/create sequence mapper.hibernate_sequence;/, '')
                     .replaceAll(/drop sequence mapper.hibernate_sequence;/, 'drop sequence mapper.hibernate_sequence;\n    create sequence mapper.hibernate_sequence;')

    File dataDir = new File("${grailsSettings.baseDir}/web-app/sql")
    File viewsDir = new File(dataDir, 'views')
    File mapperDDL = new File(dataDir, "mapper-ddl.sql")
    mapperDDL.write(text)
    viewsDir.listFiles().each{ File view ->
        mapperDDL.append("\n-- ${view.name}\n")
        mapperDDL.append(view.text)
    }
}

setDefaultTarget(main)

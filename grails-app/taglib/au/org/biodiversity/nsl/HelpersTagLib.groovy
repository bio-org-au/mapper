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

class HelpersTagLib {

    def linkedData = { attr ->
        if (attr.val != null) {
            String description = ''
            def data = null
            if (attr.val instanceof Map) {
                description = attr.val.desc
                data = attr.val.data
            } else {
                data = attr.val
            }

            if (data instanceof String) {
                if (description) {
                    out << "&nbsp;<strong>${data}</strong>"
                    out << "&nbsp;<span class='text text-muted'>$description</span>"
                } else {
                    out << "<strong>${attr.val}</strong>"
                }
                return
            }

            if (data instanceof Collection) {
                out << "<a href='#' data-toggle='collapse' data-target='#${data.hashCode()}'>(${data.size()})</a>"
                out << "&nbsp;<span class='text text-muted'>$description</span>"
                out << "<ol id='${data.hashCode()}' class='collapse'>"
                Integer top = Math.min(data.size(), 99)
                (0..top).each { idx ->
                    Object obj = data[idx]
                    if (obj) {
                        if (obj instanceof Collection) {
                            out << "<li>&nbsp;"
                            out << '['
                            obj.eachWithIndex { Object subObj, i ->
                                if (i) {
                                    out << ', '
                                }
                                printObject(subObj)
                            }
                            out << ']</li>'
                        } else {
                            out << '<li>'
                            printObject(obj)
                            out << '</li>'
                        }
                    }
                }
                if(top < data.size()) {
                    out << '<li>...</li>'
                }
                out << '</ol>'

            } else {
                out << "<strong>${attr.val}</strong>"
            }

        }
    }

    private void printObject(obj) {
        if (obj.properties.containsKey('id')) {
            String cont = obj.class.canonicalName.split(/\./).last()
            String link = g.createLink(controller: cont, id: obj.id, action: 'show')
            out << "<a href='$link'>${obj.toString()}</a>"
        } else {
            out << "<strong>$obj</strong>"
        }
    }

    def camelToLabel = { attrs ->
        String label = attrs.camel
        if (label) {
            label = label.replaceAll(/([a-z]+)([A-Z])/, '$1 $2').toLowerCase()
            out << label.capitalize()
        }
    }

    def trucate = { attrs ->
        Integer max = attrs.max as Integer
        String line = attrs.line
        if (line.size() > max) {
            out << line[0..max] + '...'
        } else {
            out << attrs.line
        }

    }

    def sourceDataLink = { attrs ->
        def source = attrs.source
        if (source.properties.containsKey('sourceId')) {
            String sourceSystem = toCamelCase(source.sourceSystem)
            if(sourceSystem == 'reference') {
                sourceSystem = 'apniReference'
            } else if(sourceSystem == 'author') {
                sourceSystem = 'apniAuthor'
            }
            String link = g.createLink(controller: sourceSystem, action: 'show', id: source.sourceId)
            out << "<a href='$link' target='source'><span class='fa fa-link'></span>&nbsp;go to source record</a>"
        }
        return 'no source link'
    }

    static String toCamelCase(String text) {
        return text.toLowerCase().replaceAll("(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() })
    }
}

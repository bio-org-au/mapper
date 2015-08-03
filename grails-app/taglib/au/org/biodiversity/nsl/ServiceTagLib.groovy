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

class ServiceTagLib {

    def grailsApplication

//    static defaultEncodeAs = 'html'
    static encodeAsForTags = [tagName: 'raw']
    static namespace = "st"

    def displayMap = { attrs ->

        def map = attrs.map

        out << '<ul>'
        map.each { k, v ->
            out << "<li>"
            out << "<b>${k.encodeAsHTML()}:</b>&nbsp;"
            if (v instanceof Map) {
                out << displayMap(map: v)
            } else {
                if (v.toString().startsWith('http://')) {
                    out << "<a href='$v'>${v.encodeAsHTML()}</a>"
                } else {
                    out << v.encodeAsHTML()
                }
            }
            out << '</li>'
        }
        out << '</ul>'
    }

    def systemNotification = { attrs ->
        String messageFileName = grailsApplication?.config?.nslServices?.system?.message?.file
        if (messageFileName) {
            File message = new File(messageFileName)
            if (message.exists()) {
                String text = message.text
                if (text) {
                    out << """<div class="alert alert-danger" role="alert">
  <span class="fa fa-warning" aria-hidden="true"></span>
  $text</div>"""
                }
            }
        } else {
            out << 'configure message filename.'
        }
    }
}

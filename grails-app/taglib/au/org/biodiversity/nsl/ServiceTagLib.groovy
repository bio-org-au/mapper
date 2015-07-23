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

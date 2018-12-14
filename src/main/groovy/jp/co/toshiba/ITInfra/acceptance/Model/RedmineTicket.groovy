package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString(includePackage = false)
class RedmineTicket extends SpecModel {
    // tracker -> subject -> custom_fileds の辞書
    def ticket_dict = [:].withDefault{[:].withDefault{[:]}}
    def port_list_dict = [:].withDefault{[]}

    def regist(String tracker, String subject, String field_name, String value) {
        ticket_dict[tracker][subject][field_name] = value
    }

    def regist_port_list(String target, LinkedHashMap<String, PortList> port_lists) {
        println "REGIST_PORT_LIST:$target, ${port_lists}"
        port_lists.each { address, port_list ->
            port_list_dict[target] << address
        }
    }

    def get_ticket_dict() {
        return this.ticket_dict
    }

    def get_port_list_dict() {
        return this.port_list_dict
    }

    def count() { return 1 }
}


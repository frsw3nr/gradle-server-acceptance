package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString(includePackage = false)
class RedmineTicket extends SpecModel {
    // tracker -> subject -> custom_fileds の辞書
    def dict = [:].withDefault{[:].withDefault{[:]}}

    def regist(String tracker, String subject, String field_name, String value) {
        dict[tracker][subject][field_name] = value
    }

    def count() { return 1 }
}


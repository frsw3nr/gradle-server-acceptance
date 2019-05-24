package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import groovy.transform.InheritConstructors
import org.apache.commons.lang.math.NumberUtils
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.TestItem.*

@Slf4j
@ToString(includePackage = false)
@InheritConstructors
class TestItem {

    String platform
    String test_id
    Boolean verify_test
    def server_info = [:]
    LinkedHashMap<String,TestResult> test_results = [:]
    LinkedHashMap<String,PortList> port_lists = [:]

    TestResultRegister test_register    = new TestResultRegister(this)
    PortListRegister port_list_register = new PortListRegister(this)
    ResultVerifier result_verifier      = new ResultVerifier(this)

    @ToString(includePackage = false)
    class CheckResult {
        Boolean check
        String error_msg
    }


    TestResult make_verify(String metric_name, Boolean verify_ok, String error_msg = null) {
        test_register.make_verify(metric_name, verify_ok, error_msg)
    }

    def results(Object value = null) {
        test_register.results(value)
    }

    def results(Map values) {
        test_register.results(values)
    }

    def status(Boolean status_ok) {
        test_register.status(status_ok)
    }

    def status(Map status_oks) {
        test_register.status(status_oks)
    }

    def lookuped_port_list(String ip, 
                         String description = null,
                         String mac         = null,
                         String vendor      = null,
                         String switch_name = null,
                         String netmask     = null,
                         String subnet      = null,
                         String port_no     = null,
                         String device_type = null) {
        port_list_register.lookuped_port_list(ip, description, mac, vendor, switch_name, 
            netmask, subnet, port_no, device_type)
    }

    def admin_port_list(String ip, 
                        String description = null,
                        String mac         = null,
                        String vendor      = null,
                        String switch_name = null,
                        String netmask     = null,
                        String subnet      = null,
                        String port_no     = null,
                        String device_type = null) {
        port_list_register.admin_port_list(ip, description, mac, vendor, switch_name, 
            netmask, subnet, port_no, device_type)
    }

    def error_msg(String error_msg) {
        test_register.error_msg(error_msg)
    }

    def verify(Boolean status_ok) {
        test_register.verify(status_ok)
    }

    def verify(Map status_oks) {
        test_register.verify(status_oks)
    }

    def devices(List csv, List header) {
        test_register.devices(csv, header)
    }

    def target_info(String item, String platform = null) {
        result_verifier.target_info(item, platform)
    }

    def verify_text_search(String item_name, String value) {
        result_verifier.verify_text_search(item_name, value)
    }

    def verify_number_equal(String item_name, Object value, double err_range = 0) {
        result_verifier.verify_number_equal(item_name, value, err_range)
    }

    def verify_number_lower(String item_name, Object value) {
        result_verifier.verify_number_lower(item_name, value)
    }

    def verify_number_higher(String item_name, Object value) {
        result_verifier.verify_number_higher(item_name, value)
    }

    def verify_text_search_map(String item_name, Map values) {
        result_verifier.verify_text_search_map(item_name, values)
    }

    def verify_number_equal_map(String item_name, Map values, double err_range = 0) {
        result_verifier.verify_number_equal_map(item_name, values, err_range)
    }

    def verify_text_search_list(String item_name, Object values) {
        result_verifier.verify_text_search_list(item_name, values)
    }

    def sql_rows_to_csv(List rows, List header = null) {
        def header_keys = [:]
        def csv = []
        rows.each { row ->
            def list = []
            row.each { column_name, value ->
                list << value
                if (!header_keys.containsKey(column_name))
                    header_keys[column_name] = true
            }
            csv << list
        }
        def headers = header_keys.keySet()
        if (header)
            headers = header
        def text = "${headers.join('\t')}\n"
        csv.each { line ->
            text += "${line.join('\t')}\n"
        }
        return text
    }

    def parse_csv(String lines) {
        def header = []
        def csv = []
        def rownum = 1
        lines.eachLine { line ->
            def arr = line.split('\t')
            if (rownum == 1)
                header = arr
            else
                csv << arr
            rownum ++
        }
        return [header, csv]
    }

    def make_abbreviation(String value) {
        if (value == 'False') {
            return '0'
        } else if (value == 'True') {
            return '1'
        } else {
            return value
        }
    }

    def make_summary_text(Map result_labels) {
        def result_summarys = [:]
        result_labels.each { label_key, result_label ->
            [label_key, "${this.test_id}.${label_key}"].find { result_label_key ->
                def found = this.test_results?."${result_label_key}"
                // println "KEY:${result_label_key}, FOUND:${found}"
                if (found != null) {
                    def value = make_abbreviation(found.value)
                    result_summarys[result_label] = value
                    return true
                }
            }
        }
        def summary_text = (result_summarys) ? "${result_summarys}" : "Not Found"
        this.results(summary_text)
    }
}

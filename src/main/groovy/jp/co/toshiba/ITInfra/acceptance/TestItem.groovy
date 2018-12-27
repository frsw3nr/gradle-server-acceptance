package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import org.apache.commons.lang.math.NumberUtils
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
class TestItem {

    String platform
    String test_id
    Boolean verify_test
    def server_info = [:]
    LinkedHashMap<String,TestResult> test_results = [:]
    LinkedHashMap<String,PortList> port_lists = [:]

    @ToString(includePackage = false)
    class CheckResult {
        Boolean check
        String error_msg
    }

    TestResult make_test_result(String metric_name, Object value) {
        def test_result = this.test_results?."${metric_name}" ?:
                          new TestResult(name: metric_name)
        def value_str = "$value"
        test_result.value = value_str
        test_result.status = ResultStatus.OK
        if (value == null || value_str == '[:]' || value_str == '[]' || value_str == '') {
            test_result.status = ResultStatus.WARNING
            test_result.error_msg = 'Not found'
        }
        this.test_results[metric_name] = test_result
    }

    TestResult make_status(String metric_name, Boolean status_ok) {
        def test_result = this.test_results?."${metric_name}" ?:
                          new TestResult(name: metric_name)
        test_result.status = (status_ok) ? ResultStatus.OK : ResultStatus.NG
        this.test_results[metric_name] = test_result
    }

    def set_base_verify(Boolean verify_ok) {
        def base_result = this.test_results?."${this.test_id}"
        if (!base_result) {
            base_result = new TestResult(name: this.test_id, verify: ResultStatus.OK)
            this.test_results."${this.test_id}" = base_result
        }
        if (!verify_ok) {
            base_result.verify = ResultStatus.NG
        }
    } 

    TestResult make_verify(String metric_name, Boolean verify_ok, String error_msg = null) {
        this.set_base_verify(verify_ok)
        def test_result = this.test_results?."${metric_name}" ?:
                          new TestResult(name: metric_name)
        test_result.verify = (verify_ok) ? ResultStatus.OK : ResultStatus.NG
        test_result.error_msg = error_msg
        this.test_results[metric_name] = test_result
    }

    def results(Object value = null) {
        this.make_test_result(this.test_id, value)
    }

    def results(Map values) {
        values.each { metric_name, value ->
            if (value == '[:]')     // Aboid withDefault{[:]} null object check
                return
            this.make_test_result(metric_name, value)
        }
    }

    def status(Boolean status_ok) {
        this.make_status(this.test_id, status_ok)
    }

    def status(Map status_oks) {
        status_oks.each { metric_name, status_ok ->
            this.make_status(metric_name, status_ok)
        }
    }

    def port_list(String ip, 
                  String description = null,
                  String mac         = null,
                  String vendor      = null,
                  String switch_name = null,
                  String netmask     = null,
                  String device_type = null,
                  Boolean online     = null,
                  PortType port_type = null) {
        def _port_list = this.port_lists?."${ip}" ?:
                         new PortList(ip : ip, 
                                      description : description, 
                                      mac :         mac, 
                                      vendor :      vendor, 
                                      switch_name : switch_name, 
                                      netmask :     netmask,
                                      device_type : device_type,
                                      online :      online,
                                      port_type :   port_type, 
                                     )
        this.port_lists[ip] = _port_list
    }

    def error_msg(String error_msg) {
        def metric_name = this.test_id
        def test_result = this.test_results?."${metric_name}" ?:
                          new TestResult(name: metric_name)
        test_result.error_msg = error_msg
        this.test_results[metric_name] = test_result
    }

    def verify(Boolean status_ok) {
        this.make_verify(this.test_id, status_ok)
    }

    def verify(Map status_oks) {
        status_oks.each { metric_name, status_ok ->
            if (status_ok == '[:]')     // Aboid withDefault{[:]} null object check
                return
            this.make_verify(metric_name, status_ok)
        }
    }

    def devices(List csv, List header) {
        def test_result = this.test_results[this.test_id] ?:
                          new TestResult(name: this.test_id)
        def test_result_line = new TestResultLine(csv : csv, header : header)
        test_result.devices = test_result_line
        test_result.status = ResultStatus.OK
        // if (csv == null || csv.size() == 0)
        //     test_result.status = ResultStatus.NG
        this.test_results[this.test_id] = test_result
    }

    def target_info(String item, String platform = null) {
        if (!platform)
            platform = this.platform
        item = item.toLowerCase()
        if (this.server_info.containsKey(item)) {
            def value = this.server_info[item]
            if (value != null && !value.empty)
                return value
        }
        if (!this.server_info.containsKey(platform) ||
            !this.server_info[platform].containsKey(item)) {
            return
        }
        return this.server_info[platform][item]
    }

    def verify_text_search(String item_name, String value) {
        def test_value = this.target_info(item_name)
        if (this.verify_test && test_value) {
            def check = (value =~ /$test_value/) as boolean
            def isok = (check)?'OK':'NG'
            log.info "Check ${item_name}, ${isok}"
            def error_msg
            if (!check) {
                error_msg = "'${value}' !=~ /${test_value}/"
                log.info "Check ${item_name}, ${isok}, ${error_msg}"
            }
            this.make_verify(item_name, check, error_msg)
        }
    }

    def to_number(Object value) {
        def value_double
        if (value instanceof Number)
            value_double = value as Double
        else if (NumberUtils.isNumber(value))
            value_double = NumberUtils.toDouble(value)
        return value_double
    }

    CheckResult is_difference(test_value, value, String item_name, double err_range = 0) {
        def test_value_double = this.to_number(test_value)
        if (!test_value_double) {
            return
        }
        def value_double = this.to_number(value)
        if (value_double == null) {
            log.info "Value '$item_name' is not number : $value"
            return
        }
        def max_value = Math.max(test_value_double, value_double)
        def differ = Math.abs(test_value_double - value_double)
        def check = ((1.0 * differ / max_value) <= err_range) as boolean
        def isok = (check)?'OK':'NG'
        log.info "Check ${item_name}, ${isok}"
        def error_msg
        def err = (err_range == 0) ? '' : "(error range=${err_range})"
        if (!check) {
            error_msg = "${value} != ${test_value}${err}"
        }
        return new CheckResult(check: check, error_msg: error_msg)
    }

    def verify_number_equal(String item_name, Object value, double err_range = 0) {
        if (!this.verify_test)
            return
        def test_value = this.target_info(item_name)
        def check_result = this.is_difference(test_value, value, item_name, err_range)
        if (check_result != null) {
            if (!check_result.check) {
                log.info "Check ${item_name}, NG, ${check_result.error_msg}"
            }
            this.make_verify(item_name, check_result.check, check_result.error_msg)
        }
    }

    def verify_text_search_map(String item_name, Map values) {
        if (!this.verify_test)
            return
        def test_values = this.target_info(item_name)
        if (test_values) {
            if (!test_values instanceof Map) {
                log.info "Test value '$item_name' is not Map : $test_value"
                return
            }
            def error_msg
            def check = true
            test_values.find { test_key, test_value ->
                if (!values.containsKey(test_key)) {
                    check = false
                    error_msg = "'${test_key}' not in '${trim_values_text(values)}'"
                    return true
                }
                if (!(values[test_key] =~ /$test_value/)) {
                    check = false
                    error_msg = "'${values[test_key]}'(${test_key}) !=~ /${test_value}/ in '${trim_values_text(values)}'"
                    return true
                }
            }
            def isok = (check)?'OK':'NG'
            log.info "Check ${item_name}, ${isok}"
            if (!check) {
                log.info "Check ${item_name}, ${isok}, ${error_msg}"
            }
            this.make_verify(item_name, check, error_msg)
        }
    }

    def verify_number_equal_map(String item_name, Map values, double err_range = 0) {
        if (!this.verify_test)
            return
        def test_values = this.target_info(item_name)
        if (test_values) {
            if (!test_values instanceof Map) {
                log.info "Test value '$item_name' is not Map : $test_value"
                return
            }
            def ng_msg
            def check = true
            test_values.find { test_key, test_value ->
                if (!values.containsKey(test_key)) {
                    check = false
                    ng_msg = "'${test_key}' not in '${values}'"
                    return true
                }
                def value = values[test_key]
                def check_result = this.is_difference(test_value, value, item_name,
                                                      err_range)
                if (check_result && check_result.check == false) {
                    check = false
                    ng_msg = check_result.error_msg
                    return true
                }
            }
            def isok = (check)?'OK':'NG'
            log.info "Check ${item_name}, ${isok}"
            if (!check) {
                log.info "Check ${item_name}, NG, ${ng_msg}"
            }
            this.make_verify(item_name, check, ng_msg)
        }
    }

    def trim_values_text(Object values) {
        def values_text = values.toString()
        if (values_text.size() > 100)
            values_text = values_text.substring(0, 100) + ' ...'
        return values_text
    }

    def verify_text_search_list(String item_name, Object values) {
        if (!this.verify_test)
            return
        def test_values = this.target_info(item_name)
        if (test_values) {
            if (!test_values instanceof Map) {
                log.info "Test value '$item_name' is not Map : $test_value"
                return
            }
            def ng_msg
            def check = true
            def text_search_values = values.toString()
            test_values.find { test_key, test_value ->
                def included = (text_search_values =~ /$test_key/) as boolean
                if (!included) {
                    check = false
                    ng_msg = "'${test_key}' not in '${trim_values_text(values)}'"
                    return true
                }
            }
            def isok = (check)?'OK':'NG'
            log.info "Check ${item_name}, ${isok}"
            def error_msg
            if (!check) {
                error_msg = "Check ${item_name}, ${isok}, ${ng_msg}"
                log.info error_msg
                this.make_verify(this.test_id, check)
            }
            this.make_verify(item_name, check, ng_msg)
        }
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

}

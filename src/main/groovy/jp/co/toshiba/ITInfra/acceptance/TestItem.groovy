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
    def server_info = [:]
    LinkedHashMap<String,TestResult> test_results = [:]

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

    TestResult make_verify(String metric_name, Boolean verify_ok) {
        def test_result = this.test_results?."${metric_name}" ?:
                          new TestResult(name: metric_name)
        test_result.verify = (verify_ok) ? ResultStatus.OK : ResultStatus.NG
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
        if (this.server_info.containsKey(item)) {
            def value = this.server_info[item]
            if (value != null && !nullList.empty)
                return this.server_info[item]
        }
        if (!this.server_info.containsKey(platform) ||
            !this.server_info[platform].containsKey(item)) {
            return
        }
        return this.server_info[platform][item]
    }

    def verify_text_search(String item_name, String value) {
        def test_value = this.target_info(item_name)
        if (test_value) {
            def check = (value =~ /$test_value/) as boolean
            log.info "Check ${item_name}, '${value}' =~ /${test_value}/, OK : ${check}"
            this.make_verify(item_name, check)
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

    def is_difference(test_value, value, String item_name, double err_range = 0) {
        def test_value_double = this.to_number(test_value)
        if (!test_value_double) {
            return
        }
        def value_double = this.to_number(value)
        if (!value_double) {
            log.warn "Value '$item_name' is not number : $value"
            return
        }
        def max_value = Math.max(test_value_double, value_double)
        def differ = Math.abs(test_value_double - value_double)
        def check = ((1.0 * differ / max_value) <= err_range) as boolean
        def err = (err_range == 0) ? '' : "(error range=${err_range})"
        log.info "Check ${item_name}, '${value}' == '${test_value}'${err}, OK : ${check}"
        return check
    }

    def verify_number_equal(String item_name, Object value, double err_range = 0) {
        def test_value = this.target_info(item_name)
        def check = this.is_difference(test_value, value, item_name, err_range)
        if (check != null)
            this.make_verify(item_name, check)
    }

    def verify_text_search_map(String item_name, Map values) {
        def test_values = this.target_info(item_name)
        if (test_values) {
            if (!test_values instanceof Map) {
                log.warn "Test value '$item_name' is not Map : $test_value"
                return
            }
            def check = true
            test_values.find { test_key, test_value ->
                if (!values.containsKey(test_key)) {
                    check = false
                    return true
                }
                if (!(values[test_key] =~ /$test_value/)) {
                    check = false
                    return true
                }
            }
            log.info "Check ${item_name}, ${values} in ${test_values}, OK : ${check}"
            this.make_verify(item_name, check)
        }
    }

    def verify_number_equal_map(String item_name, Map values, double err_range = 0) {
        def test_values = this.target_info(item_name)
        if (test_values) {
            if (!test_values instanceof Map) {
                log.warn "Test value '$item_name' is not Map : $test_value"
                return
            }
            def check = true
            test_values.find { test_key, test_value ->
                if (!values.containsKey(test_key)) {
                    check = false
                    return true
                }
                def value = values[test_key]
                def is_equal = this.is_difference(test_value, value, item_name, err_range)
                if (is_equal == false) {
                    check = false
                    return true
                }
            }
            log.info "Check ${item_name}, ${values} = ${test_values}, OK : ${check}"
            this.make_verify(item_name, check)
        }
    }

    def verify_text_search_list(String item_name, Map values) {
        def test_values = this.target_info(item_name)
        if (test_values) {
            if (!test_values instanceof Map) {
                log.warn "Test value '$item_name' is not Map : $test_value"
                return
            }
            def check = true
            test_values.find { test_key, test_value ->
                if (!values.containsKey(test_key)) {
                    check = false
                    return true
                }
            }
            log.info "Check ${item_name}, ${values} in ${test_values}, OK : ${check}"
            this.make_verify(item_name, check)
        }
    }
}

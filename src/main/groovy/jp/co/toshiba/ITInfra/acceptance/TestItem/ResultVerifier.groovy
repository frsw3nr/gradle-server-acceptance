package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import org.apache.commons.lang.math.NumberUtils
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.TestItem
import jp.co.toshiba.ITInfra.acceptance.TestItem.*

@Slf4j
@ToString(includePackage = false)
@Singleton
class ResultVerifier {

    TestItem test_item

    @ToString(includePackage = false)
    class CheckResult {
        Boolean check
        String error_msg
    }

    ResultVerifier test_item(TestItem test_item) {
        this.test_item = test_item
        return this
    }

    // ResultVerifier(TestItem test_item) {
    //     this.test_item = test_item
    // }

    def target_info(String item, String platform = null) {
        if (!platform)
            platform = test_item.platform
        item = item.toLowerCase()
        if (test_item.server_info.containsKey(item)) {
            def value = test_item.server_info[item]
            if (value != null && !value.empty)
                return value
        }
        if (!test_item.server_info.containsKey(platform) ||
            !test_item.server_info[platform].containsKey(item)) {
            return
        }
        return test_item.server_info[platform][item]
    }

    def verify_text_search(String item_name, String value) {
        def test_value = test_item.target_info(item_name)
        if (test_item.verify_test && test_value) {
            def check = (value =~ /$test_value/) as boolean
            def isok = (check)?'OK':'NG'
            log.info "Check ${item_name}, ${isok}"
            def error_msg
            if (!check) {
                error_msg = "'${value}' !=~ /${test_value}/"
                log.info "Check ${item_name}, ${isok}, ${error_msg}"
            }
            test_item.make_verify(item_name, check, error_msg)
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
        if (!test_item.verify_test)
            return
        def test_value = test_item.target_info(item_name)
        def check_result = this.is_difference(test_value, value, item_name, err_range)
        if (check_result != null) {
            if (!check_result.check) {
                log.info "Check ${item_name}, NG, ${check_result.error_msg}"
            }
            test_item.make_verify(item_name, check_result.check, check_result.error_msg)
        }
    }

    def verify_number_lower(String item_name, Object value) {
        if (!test_item.verify_test)
            return
        def test_value = test_item.target_info(item_name)
        if (!test_value)
            return
        def test_value_double = this.to_number(test_value)
        def value_double = this.to_number(value)
        if (value_double == null) {
            log.info "Value '$item_name' is not number : $value"
            return
        }
        def check = (value_double <= test_value_double) as boolean
        def isok = (check)?'OK':'NG'
        log.info "Check ${item_name}, ${isok}"
        def error_msg
        if (!check) {
            error_msg = "'${value}' <= '${test_value}'"
            log.info "Check ${item_name}, ${isok}, ${error_msg}"
        }
        test_item.make_verify(item_name, check, error_msg)
    }

    def verify_number_higher(String item_name, Object value) {
        if (!test_item.verify_test)
            return
        def test_value = test_item.target_info(item_name)
        if (!test_value)
            return
        def test_value_double = this.to_number(test_value)
        def value_double = this.to_number(value)
        if (value_double == null) {
            log.info "Value '$item_name' is not number : $value"
            return
        }
        def check = (value_double >= test_value_double) as boolean
        def isok = (check)?'OK':'NG'
        log.info "Check ${item_name}, ${isok}"
        def error_msg
        if (!check) {
            error_msg = "'${value}' >= '${test_value}'"
            log.info "Check ${item_name}, ${isok}, ${error_msg}"
        }
        test_item.make_verify(item_name, check, error_msg)
    }

    def verify_text_search_map(String item_name, Map values) {
        if (!test_item.verify_test)
            return
        def test_values = test_item.target_info(item_name)
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
            test_item.make_verify(item_name, check, error_msg)
        }
    }

    def verify_number_equal_map(String item_name, Map values, double err_range = 0) {
        if (!test_item.verify_test)
            return
        def test_values = test_item.target_info(item_name)
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
            test_item.make_verify(item_name, check, ng_msg)
        }
    }

    def trim_values_text(Object values) {
        def values_text = values.toString()
        if (values_text.size() > 100)
            values_text = values_text.substring(0, 100) + ' ...'
        return values_text
    }

    def verify_text_search_list(String item_name, Object values) {
        if (!test_item.verify_test)
            return
        def test_values = test_item.target_info(item_name)
        if (test_values) {
            if (!(test_values instanceof Map)) {
                log.info "Test value '$item_name' is not Map : $test_values"
                return
            }
            def ng_msg
            def check = true
            def text_search_values = values.toString()
            test_values.find { test_key, test_value ->
                def included = (text_search_values.contains(test_key)) as boolean
                // def included = (text_search_values =~ /$test_key/) as boolean
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
                test_item.make_verify(test_item.test_id, check)
            }
            test_item.make_verify(item_name, check, ng_msg)
        }
    }

}

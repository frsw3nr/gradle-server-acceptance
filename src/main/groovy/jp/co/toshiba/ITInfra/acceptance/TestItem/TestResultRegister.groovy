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
class TestResultRegister {

    TestItem test_item

    TestResultRegister test_item(TestItem test_item) {
        this.test_item = test_item
        return this
    }

    // TestResultRegister(TestItem test_item) {
    //     this.test_item = test_item
    // }

    TestResult make_test_result(String metric_name, Object value) {
        def test_result = test_item.test_results?."${metric_name}" ?:
                          new TestResult(name: metric_name)
        def value_str = "$value"
        test_result.value = value_str
        test_result.status = ResultStatus.OK
        if (value == null || value_str == '[:]' || value_str == '[]' || value_str == '') {
            test_result.status = ResultStatus.WARNING
            test_result.error_msg = 'Not found'
        }
        test_item.test_results[metric_name] = test_result
    }

    TestResult make_status(String metric_name, Boolean status_ok) {
        def test_result = test_item.test_results?."${metric_name}" ?:
                          new TestResult(name: metric_name)
        test_result.status = (status_ok) ? ResultStatus.OK : ResultStatus.NG
        test_item.test_results[metric_name] = test_result
    }

    def set_base_verify(Boolean verify_ok) {
        def base_result = test_item.test_results?."${test_item.test_id}"
        if (!base_result) {
            base_result = new TestResult(name: test_item.test_id, verify: ResultStatus.OK)
            test_item.test_results."${test_item.test_id}" = base_result
        }
        if (!verify_ok) {
            base_result.verify = ResultStatus.NG
        }
    } 

    TestResult make_verify(String metric_name, Boolean verify_ok, String error_msg = null) {
        this.set_base_verify(verify_ok)
        def test_result = test_item.test_results?."${metric_name}" ?:
                          new TestResult(name: metric_name)
        test_result.verify = (verify_ok) ? ResultStatus.OK : ResultStatus.NG
        test_result.error_msg = error_msg
        test_item.test_results[metric_name] = test_result
    }

    def results(Object value = null) {
        this.make_test_result(test_item.test_id, value)
    }

    def results(Map values) {
        values.each { metric_name, value ->
            if (value == '[:]')     // Aboid withDefault{[:]} null object check
                return
            this.make_test_result(metric_name, value)
        }
    }

    def status(Boolean status_ok) {
        this.make_status(test_item.test_id, status_ok)
    }

    def status(Map status_oks) {
        status_oks.each { metric_name, status_ok ->
            this.make_status(metric_name, status_ok)
        }
    }

    def error_msg(String error_msg) {
        def metric_name = test_item.test_id
        def test_result = test_item.test_results?."${metric_name}" ?:
                          new TestResult(name: metric_name)
        test_result.error_msg = error_msg
        test_item.test_results[metric_name] = test_result
    }

    def verify(Boolean status_ok) {
        this.make_verify(test_item.test_id, status_ok)
    }

    def verify(Map status_oks) {
        status_oks.each { metric_name, status_ok ->
            if (status_ok == '[:]')     // Aboid withDefault{[:]} null object check
                return
            this.make_verify(metric_name, status_ok)
        }
    }

    def devices(List csv, List header) {
        def test_result = test_item.test_results[test_item.test_id] ?:
                          new TestResult(name: test_item.test_id)
        def test_result_line = new TestResultLine(csv : csv, header : header)
        test_result.devices = test_result_line
        test_result.status = ResultStatus.OK
        // if (csv == null || csv.size() == 0)
        //     test_result.status = ResultStatus.NG
        test_item.test_results[test_item.test_id] = test_result
    }

}

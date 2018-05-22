package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
class TestItem {

    String test_id
    LinkedHashMap<String,TestResult> test_results = [:]

    TestResult make_test_result(String metric_name, Object value) {
        def test_result = this.test_results?."${metric_name}" ?:
                          new TestResult(name: metric_name)
        def value_str = "$value"
        test_result.value = value_str
        test_result.status = ResultStatus.OK
        if (value == null || value_str == '[:]' || value_str == '[]' || value_str == '')
            test_result.status = ResultStatus.WARNING
            test_result.error_msg = 'Not found'
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

}

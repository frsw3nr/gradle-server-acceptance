package jp.co.toshiba.ITInfra.acceptance.Document;

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
public class ReportMaker {

    ConfigObject item_map
    String result_dir
    SheetSummary report_sheet
    SheetDeviceResult error_report_sheet
    def metrics = [:].withDefault{[:]}
    def platform_metrics = [:].withDefault{[:]}

    def set_environment(ConfigTestEnvironment env) {
        this.item_map = env.get_item_map()
        this.result_dir = env.get_node_dir()
    }

    def convert_test_item() {
        item_map.each { report_type, result_names ->
            if (report_type == 'target') {
                result_names.each { metric_name, result_name ->
                    this.metrics[metric_name]['report_type'] = 'target'
                    this.metrics[metric_name]['result_name'] = result_name
                }
            } else if (report_type == 'platform') {
                result_names.each { platform_name, platform_metrics ->
                    platform_metrics.each { metric_name, result_name ->
                        this.metrics[metric_name]['report_type'] = 'platform'
                        this.platform_metrics[metric_name][platform_name] = result_name
                    }
                }
            }
        }
    }

    def add_summary_result(String target, String metric, TestResult test_result) {
        def sheet = this.report_sheet ?: new SheetSummary()
        sheet.rows[target] = sheet.rows[target] ?: sheet.rows.size() + 1
        sheet.cols[metric] = sheet.cols[metric] ?: sheet.cols.size() + 1
        sheet.results[target][metric] = test_result
        this.report_sheet = sheet
    }

    def add_test_error_result(target, platform, metric, test_result) {
        def sheet_key = ['target': target, 'platform': platform, 'id': metric]
        def sheet = this.error_report_sheet ?: new SheetDeviceResult()
        sheet.rows[sheet_key] = sheet.rows[sheet_key] ?: sheet.rows.size() + 1
        sheet.results[sheet_key] = test_result
        this.error_report_sheet = sheet
    }

    TestResult get_test_result_from_json(TestReport test_report, TestTarget test_target) {
        def result_reader = new TestResultReader('result_dir': this.result_dir)
        def target_name = test_target?.name
        TestResult test_result
        test_report?.platform_metrics.find { platform, metric ->
            def test_platform = result_reader.read_test_platform_result(target_name,
                                                                        platform)
            if (test_platform) {
                def test_results = test_platform?.test_results
                if (test_results.containsKey(metric)) {
                    test_result = test_results[metric]
                    return true
                }
            }
        }
        return (test_result) ? test_result : 
                               new TestResult(value: "Need json",
                                              status : ResultStatus.UNKOWN)
    }

    TestResult get_test_result(TestReport test_report, TestTarget test_target) {
        def report_name = test_report?.name
        TestResult test_result
        if (!report_name)
            return test_result
        if (test_report?.metric_type == 'target') {
            def result_name = test_report?.default_name
            if (result_name) {
                def value = test_target?."${result_name}"
                test_result = new TestResult(name: result_name, value: value)
            }
        } else if (test_report.metric_type == 'platform') {
            def test_platforms = test_target?.test_platforms
            if (test_platforms) {
                // println "GET_TEST_RESULT:${test_platforms}"
                def platform_metrics = test_report.platform_metrics
                test_platforms.find { platform_name, test_platform ->
                    if (platform_metrics.containsKey(platform_name)) {
                        def result_name = platform_metrics[platform_name]
                        if (test_platform.test_results.containsKey(result_name)) {
                            test_result = test_platform.test_results[result_name] as TestResult
                            return true
                        }
                    }
                }
                if (!test_result) {
                    test_result = get_test_result_from_json(test_report, test_target)
                }
            }
        }
        log.debug "TEST_RESULT:${test_target.name}, ${report_name}, ${test_result}"
        return test_result ?: new TestResult(value: "", status : ResultStatus.UNKOWN)
    }

    def extract_error_test(TestScenario test_scenario) {
        def domain_metrics = test_scenario.test_metrics.get_all()
        def domain_targets = test_scenario.get_domain_targets()
        def test_error_reports = test_scenario.test_error_reports.get_all()
        domain_targets.each { domain, domain_target ->
            domain_target.each { target, test_target ->
                if (test_target.target_status == RunStatus.INIT ||
                    test_target.target_status == RunStatus.READY)
                    return
                def metric_sets = domain_metrics[domain].get_all()
                metric_sets.each { platform, metric_set ->
                    def test_platform = test_target.test_platforms[platform]
                    def test_results = test_platform?.test_results
                    if (test_platform?.platform_status == RunStatus.ERROR)
                        run_status = RunStatus.ERROR
                    if (!test_results)
                        return
                    test_results.each { metric, test_result ->
                        if (test_result && test_result?.verify == ResultStatus.NG) {
                            this.add_test_error_result(target, platform, metric, test_result)
                        }
                    }
                }
            }
        }
    }

    // def aggrigate_test_results(TestScenario test_scenario) {
    //     def domain_targets = test_scenario.get_domain_targets()
    //     def domain_metrics = test_scenario.test_metrics.get_all()

    //     domain_targets.each { domain, domain_target ->
    //         domain_target.each { target, test_target ->
    //         def metric_sets = domain_metrics[domain].get_all()
    //         def run_status = RunStatus.FINISH
    //         metric_sets.each { platform, metric_set ->

    //     domain_targets.each { domain, domain_target ->
    //         domain_target.each { target, test_target ->
    //             // if (test_target.target_status == RunStatus.INIT ||
    //             //     test_target.target_status == RunStatus.READY)
    //             //     return
    //             test_reports.each {report_name, test_report ->
    //                 def test_result = get_test_result(test_report, test_target)
    //                 add_summary_result(target, report_name, test_result)
    //             }
    //         }
    //     }
    // }

    def visit_test_scenario(TestScenario test_scenario) {
        long start = System.currentTimeMillis()

        // this.convert_test_item()
        def test_reports = test_scenario.test_reports.get_all()
        def domain_targets = test_scenario.get_domain_targets()

        domain_targets.each { domain, domain_target ->
            domain_target.each { target, test_target ->
                // if (test_target.target_status == RunStatus.INIT ||
                //     test_target.target_status == RunStatus.READY)
                //     return
                test_reports.each {report_name, test_report ->
                    def test_result = get_test_result(test_report, test_target)
                    add_summary_result(target, report_name, test_result)
                }
            }
        }
        this.extract_error_test(test_scenario)
        long elapse = System.currentTimeMillis() - start
        log.info "Finish report maker, Elapse : ${elapse} ms"
    }
}

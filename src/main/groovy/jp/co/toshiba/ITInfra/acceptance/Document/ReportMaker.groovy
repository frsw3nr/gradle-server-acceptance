package jp.co.toshiba.ITInfra.acceptance.Document;

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
public class ReportMaker {

    ConfigObject item_map
    SheetSummary report_sheet
    def metrics = [:].withDefault{[:]}
    def platform_metrics = [:].withDefault{[:]}

    def set_environment(ConfigTestEnvironment env) {
        this.item_map = env.get_item_map()
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

    TestResult get_test_result(TestReport test_report, TestTarget test_target) {
        def report_name = test_report.name
        TestResult test_result = new TestResult(name: report_name, value: "TEST")
        def metric = this.metrics?."${report_name}"
        if (!metric) {
            println "NOTFOUND METRIC: $metric"
            return
        }
        if (metric.report_type == 'target') {
            def result_name = metric.result_name
            test_result = new TestResult(name: result_name, 
                                         value: test_target?."${result_name}")
        } else if (metric.report_type == 'platform') {
            def test_platforms = test_target?.test_platforms
            if (test_platforms) {
                def platform_metric = this.platform_metrics?."${report_name}"
                test_platforms.find { platform_name, test_platform ->
                    if (platform_metric.containsKey(platform_name)) {
                        def result_name = platform_metric[platform_name]
                        if (test_platform.test_results.containsKey(result_name)) {
                            test_result = test_platform.test_results[result_name] as TestResult
                            return true
                        }
                    }
                }
            }
        }
        log.debug "TEST_RESULT:${test_target.name}, ${report_name}, ${test_result}"
        return test_result
    }

    def visit_test_scenario(TestScenario test_scenario) {
        long start = System.currentTimeMillis()

        this.convert_test_item()
        println "METRICS: ${this.metrics}"
        def test_reports = test_scenario.test_reports.get_all()
        def domain_targets = test_scenario.get_domain_targets()

        domain_targets.each { domain, domain_target ->
            domain_target.each { target, test_target ->
                println "TEST_TARGET: ${test_target.name}, ${test_target.target_status}"
                // if (test_target.target_status == RunStatus.INIT ||
                //     test_target.target_status == RunStatus.READY)
                //     return
                test_reports.each {report_name, test_report ->
                    def test_result = get_test_result(test_report, test_target)
                    add_summary_result(target, report_name, test_result)
                }
            }
        }
        println "ROWS:${report_sheet.rows}"
        long elapse = System.currentTimeMillis() - start
        log.info "Finish report maker, Elapse : ${elapse} ms"
    }

}

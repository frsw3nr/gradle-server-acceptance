package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
class SheetSummary {
    def rows = [:]
    def added_rows = [:]
    def cols = [:]
    def results = [:].withDefault{[:].withDefault{[:]}}
}

@Slf4j
@ToString(includePackage = false)
class SheetDeviceResult {
    def rows = [:]
    def results = [:]
}

// @Slf4j
// @ToString(includePackage = false)
// class TicketInfo {
//     String tracker
//     String subject
//     String status
//     LinkedHashMap<String,String> custom_fields = [:]
//     LinkedHashMap<String,PortList> port_lists = [:]
// }

@Slf4j
@ToString(includePackage = false)
class EvidenceMaker {
    LinkedHashMap<String,SheetSummary> summary_sheets = [:]
    LinkedHashMap<String,SheetSummary> summary_tickets = [:]
    LinkedHashMap<String,SheetDeviceResult> device_result_sheets = [:]

    def add_added_test_metric(domain, platform, metric, test_metric) {
        println "ADD: $domain, $platform, $metric, $test_metric"
        def sheet = this.summary_sheets[domain] ?: new SheetSummary()
        sheet.added_rows[[platform, metric]] = test_metric
        this.summary_sheets[domain] = sheet
    }

    def add_summary_result(domain, target, platform, metric, test_result) {
        def sheet = this.summary_sheets[domain] ?: new SheetSummary()
        sheet.rows[[platform, metric]] = 1
        if (sheet.cols.containsKey(target)) {
            sheet.cols[target] = sheet.cols.size()
        } else {
            sheet.cols[target] = sheet.cols.size() + 1
        }
        sheet.results[platform][metric][target] = test_result
        this.summary_sheets[domain] = sheet
    }

    def add_device_result(target, platform, metric, test_result) {
        def sheet_key = [platform, metric]
        def sheet = this.device_result_sheets[sheet_key] ?: new SheetDeviceResult()
        sheet.rows[target] = 1
        sheet.results[target] = test_result
        this.device_result_sheets[sheet_key] = sheet
    }

    def make_aggrigate_result(TestTarget test_target, Map verify_summaries,
                              List failed_metrics) {
        def test_ok  = verify_summaries[ResultStatus.OK] ?: 0
        def test_ng  = verify_summaries[ResultStatus.NG] ?: 0
        def test_all = test_ok + test_ng
        def success_rate = 'No test'
        def comment = ''
        if (test_all > 0) {
            success_rate = sprintf('%.1f %%', (double) 100 * test_ok / test_all)
            if (test_ng > 0) {
                comment += "${test_ng} / ${test_all} Failed : ${failed_metrics}"
                log.info "Finish verify : ${test_target.name}, ${comment}"
            } else {
                log.info "Finish verify : ${test_target.name}, OK"
            }
        } else {
            success_rate = 'Not test'
        }
        test_target.success_rate = success_rate
        test_target.verify_comment = comment
    }

    def aggrigate_test_result(test_scenario) {
        def domain_metrics = test_scenario.test_metrics.get_all()
        def domain_targets = test_scenario.get_domain_targets()

        domain_targets.each { domain, domain_target ->
            domain_target.each { target, test_target ->
                test_target.success_rate = "No test"
                def verify_summaries = [:].withDefault{0}
                def failed_metrics = []
                if (test_target.target_status == RunStatus.INIT ||
                    test_target.target_status == RunStatus.READY)
                    return
                def metric_sets = domain_metrics[domain].get_all()
                metric_sets.each { platform, metric_set ->
                    def test_platform = test_target.test_platforms[platform]
                    def test_results = test_platform?.test_results
                    if (!test_results)
                        return
                    metric_set.get_all().each { metric, test_metric ->
                        def test_result = test_results[metric]
                        if (test_result) {
                            verify_summaries[test_result.verify] ++
                            if (test_result.verify == ResultStatus.NG) {
                                failed_metrics << metric
                                test_scenario.exit_code = 1
                            }
                        }
                    }
                }
                this.make_aggrigate_result(test_target, verify_summaries, failed_metrics)
            }
        }
    }

    def extract_added_test_metric(TestScenario test_scenario) {
        def domain_metrics = test_scenario.test_metrics
        // println "EXTRACT_ADDED_TEST_METRIC:${domain_metrics.count()}"
        def domain_targets = test_scenario.get_domain_targets()
        domain_targets.each { domain, test_targets ->
            test_targets.each { target, test_target ->
                if (test_target.target_status == RunStatus.INIT ||
                    test_target.target_status == RunStatus.READY)
                    return
                test_target.test_platforms.each { platform, test_platform ->
                    def metric_set = domain_metrics[domain][platform]
                    if (metric_set.count() > 0) {
                        test_platform?.added_test_metrics.each { metric, test_metric ->
                            metric_set.add(test_metric)
                            add_added_test_metric(domain, platform, metric, test_metric)
                        }
                    }
                    // println "METRIC_SET: ${metric_set.get_all()}"
                }
            }
        }
    }

    def visit_test_scenario(test_scenario) {
        long start = System.currentTimeMillis()

        this.extract_added_test_metric(test_scenario)
        this.aggrigate_test_result(test_scenario)
        def domain_metrics = test_scenario.test_metrics.get_all()
        def domain_targets = test_scenario.get_domain_targets()
        def comparision_sequences = [true, false]
        comparision_sequences.each { comparision_sequence ->
            domain_targets.each { domain, domain_target ->
                domain_target.each { target, test_target ->
                    if (test_target.target_status == RunStatus.INIT ||
                        test_target.target_status == RunStatus.READY)
                        return
                    def comparision = test_target.comparision
                    def metric_sets = domain_metrics[domain].get_all()
                    def run_status = RunStatus.FINISH
                    metric_sets.each { platform, metric_set ->
                        def test_platform = test_target.test_platforms[platform]
                        def test_results = test_platform?.test_results
                        if (test_platform?.platform_status == RunStatus.ERROR)
                            run_status = RunStatus.ERROR
                        if (!test_results)
                            return
                        metric_set.get_all().each { metric, test_metric ->
                            def test_result = test_results[metric]
                            if (test_result) {
                                if (comparision == comparision_sequence) {
                                    add_summary_result(domain, target, platform, metric,
                                                       test_result)
                                    // println "METRIC: $domain, $target, $platform, $metric"
                                    if (test_metric.device_enabled) {
                                        add_device_result(target, platform, metric, test_result)
                                    }
                                }
                            }
                        }
                    }
                    if (test_target.target_status == RunStatus.RUN)
                        test_target.target_status = run_status
                }
            }
        }
        long elapse = System.currentTimeMillis() - start
        log.debug "Finish evidence maker, Elapse : ${elapse} ms"
    }
}

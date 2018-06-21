package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
class SheetSummary {
    def rows = [:]
    def cols = [:]
    def results = [:].withDefault{[:].withDefault{[:]}}
}

@Slf4j
@ToString(includePackage = false)
class SheetDeviceResult {
    def rows = [:]
    def results = [:]
}

@Slf4j
@ToString(includePackage = false)
class EvidenceMaker {
    LinkedHashMap<String,SheetSummary> summary_sheets = [:]
    LinkedHashMap<String,SheetDeviceResult> device_result_sheets = [:]

    def add_summary_result(domain, target, platform, metric, test_result) {
        def sheet = this.summary_sheets[domain] ?: new SheetSummary()
        sheet.rows[[platform, metric]] = 1
        sheet.cols[target] = sheet.cols.size()
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

    def visit_test_scenario(test_scenario) {
        long start = System.currentTimeMillis()

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
                            if (test_result && comparision == comparision_sequence) {
                                add_summary_result(domain, target, platform, metric,
                                                   test_result)
                                if (test_metric.device_enabled) {
                                    add_device_result(target, platform, metric, test_result)
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

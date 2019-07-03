package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.Model.ResultStatus
import jp.co.toshiba.ITInfra.acceptance.Model.RunStatus
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
import jp.co.toshiba.ITInfra.acceptance.Model.TestResult
import jp.co.toshiba.ITInfra.acceptance.Model.ColumnType
import jp.co.toshiba.ITInfra.acceptance.Model.TestMetric
import jp.co.toshiba.ITInfra.acceptance.Model.TestMetricSet

@Slf4j
@ToString(includePackage = false)
class CompareCounter {

    // Count comparison results with target, platform, metric as key
    def metric_counters = [:].withDefault { [:].withDefault { [:].withDefault {
        new LinkedHashMap<ResultStatus,Integer>()
    }}}

    def count_up(String server, String platform, String metric,
                 ResultStatus result_status) {
        Map metric_counter = this.metric_counters?.get(server)?.get(platform)?.get(metric)
        int result_counter = metric_counter?.get(result_status) ?: 0
        this.metric_counters[server][platform][metric][result_status] = result_counter + 1
    }

    Boolean is_empty() {
        return (this.metric_counters.size() == 0)
    }

    Map get_all() {
        return this.metric_counters
    }

    // Map get_target_counter(String target) {
    //     return metric_counters?.get(target)
    // }

    Map get_platform_counter(String target, String platform) {
        return metric_counters?.get(target)?.get(platform)
    }

    TestResult create_tag_test_result(String name, Map comparisions) {
        def total_count = 0
        comparisions.each { ResultStatus comparision, count ->
            total_count += count
        }
        if (total_count == 0) {
            return new TestResult(name : name, value : '', verify : ResultStatus.UNKOWN)
        }
        double match_rate = 100.0 * (comparisions[ResultStatus.MATCH] ?: 0) / total_count
        String value = String.format("%1\$.0f %%", match_rate);
        ResultStatus verify = ResultStatus.NG
        if (match_rate == 100.0) {
            verify = ResultStatus.OK
        } else if (match_rate > 0) {
            verify = ResultStatus.WARNING
        } else {
            verify = ResultStatus.NG
        }
        return new TestResult(name : name, value : value, status: verify, 
                              verify : ResultStatus.OK, column_type: ColumnType.TAGGING)
    }

    Map create_tag_test_results(String target, String platform) {
        LinkedHashMap<String,TestResult> test_results = new LinkedHashMap<String,TestResult>()
        Map metric_counters = this.metric_counters?.get(target)?.get(platform)
        metric_counters.each { metric, metric_counter ->
            // println "CREATE DETAIL: $metric, $metric_counter"
            TestResult test_result = this.create_tag_test_result(metric, metric_counter)
            test_results[metric] = test_result
        }
        return test_results
    }
}

@Slf4j
@ToString(includePackage = false)
class DataComparator {
    // ターゲット,プラットフォーム,メトリックをキーにした比較結果カウンター
    CompareCounter compare_counter = new CompareCounter()

    def count_compare_result(String target, String platform, String metric, ResultStatus comparision) {
        this.compare_counter.count_up(target, platform, metric, comparision)
    }

    def sumup_compare_counter(TestScenario test_scenario) {
        // 結果カウンターを集計する、集計結果をタグ用のtest_resultに格納する
        // テストシナリオからタグ名（ターゲット名）、プラットフォーム名をキーに、
        // テストプラットフォームを検索する。
        // テストプラットフォームのtest_resultに集計結果をセットする
        
        Map target_counters = this.compare_counter.get_all()
        target_counters.each { target, platform_counters ->
            platform_counters.each { platform, metric_counters ->
                Map test_results = this.compare_counter.create_tag_test_results(target, platform)
                def tag_name = "TAG:$target"
                TestPlatform test_platform = test_scenario.get_test_platform(tag_name, platform)
                if (test_platform) {
                    test_platform.test_results = test_results
                }
            }
        }
    }

    def compare_server(TestTarget test_target, TestTarget compare_target) {
        def checked = [:]
        def category_ids = [:]
        test_target.test_platforms.each { platform_name, test_platform ->
            def compare_platform = compare_target?.test_platforms[platform_name]
            if (!compare_platform?.test_results)
                return
            compare_target.target_status = RunStatus.COMPARED
            test_platform?.test_results.each { metric_name, test_result ->
                def compare_result = compare_platform.test_results[metric_name]
                test_result.compare_server = compare_target.name
                if (test_result?.value == compare_result?.value) {
                    test_result.comparision = ResultStatus.MATCH
                } else {
                    test_result.comparision = ResultStatus.UNMATCH
                }
                checked[platform_name, metric_name] = true
                count_compare_result(test_result.compare_server, platform_name,
                                     metric_name, test_result.comparision)
            }
        }
        compare_target.test_platforms.each { platform_name, test_platform ->
            test_platform?.test_results.each { metric_name, test_result ->
                if (!(checked[platform_name, metric_name])){
                    count_compare_result(compare_target.name, platform_name, 
                                         metric_name, ResultStatus.UNMATCH)
                }
            }
        }
    }

    def visit_test_scenario(TestScenario test_scenario) {
        def targets = test_scenario.test_targets.get_all()
        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                def compare_server = test_target.compare_server
                if (!compare_server)
                    return
                def compare_target = test_scenario.test_targets.get(compare_server,
                                                                    domain)
                if (!compare_target) {
                    def msg = "Compare server not found : ${compare_server}"
                    throw new IllegalArgumentException(msg)
                }
                this.compare_server(test_target, compare_target)
            }
        }
        this.sumup_compare_counter(test_scenario)
    }
}

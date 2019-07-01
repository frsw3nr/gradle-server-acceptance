package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.Model.ResultStatus
import jp.co.toshiba.ITInfra.acceptance.Model.RunStatus
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import jp.co.toshiba.ITInfra.acceptance.Model.TestResult
import jp.co.toshiba.ITInfra.acceptance.Model.TestMetric

@Slf4j
@ToString(includePackage = false)
class DataComparator {
    def tag_compare_counts = [:].withDefault{[:].withDefault{0}}

    def count_compare_result(test_result) {
        test_result.with {
            println "DataComparator COUNT:${it}"
            this.tag_compare_counts[compare_server, name][comparision] ++
        }
    }

    TestResult create_test_result(String name, double match_rate) {
        String value = String.format("%1\$.1f %%", 100 * match_rate);
        ResultStatus verify = (match_rate == 1.0) ? ResultStatus.OK : ResultStatus.NG
        return new TestResult(name:name, value:value, verify:verify)
    }

    def sumup_compare_count(TestScenario test_scenario) {
        // def target_domains = test_scenario.test_targets.get(target_name)
        def tag_test_results = [:].withDefault {
            new LinkedHashMap<String,TestResult>()
        }
        this.tag_compare_counts.each { tag_metric_key, comparisions ->
            def tag_name    = tag_metric_key[0]
            def metric_name = tag_metric_key[1]
            def total_count = 0
            comparisions.each { ResultStatus comparision, count ->
                total_count += count
            }
            double match_rate = comparisions[ResultStatus.MATCH] ?: 0 / total_count
            // println "$tag_name, $metric_name, $match_rate"
            def test_result = create_test_result(metric_name, match_rate)
            tag_test_results[tag_name][metric_name] = test_result
        }
        // TODO:
        // target,platform,metric 毎に結果を集計するように変更
        // test_scenario から　test_platform を検索するように変更
        

        // def test_platform = test_scenario.get_test_platform()
        def domain_metrics = test_scenario.test_metrics.get_all()
        tag_test_results.each { tag_name, test_results ->
            def target_name = "TAG:$tag_name"
            def target_domains = test_scenario.test_targets.get(target_name)
            target_domains.each { domain_name, test_target ->
                def platform_metrics = domain_metrics[domain_name].get_all()
                platform_metrics.each { platform_name, platform_metric ->
                    // TODO : TestResultReader クラスread()メソッドを適用
                    println "Sumup : ${tag_name}, ${platform_name}, ${platform_metric}"
                    // test_target.test_platforms[platform_name].test_results = tast 
                    // def test_platform = this.read_test_platform_result(target_name,
                    //                                                    platform_name)
                    // if (test_platform) {
                    //     test_platform.test_target = test_target
                    //     test_target.test_platforms[platform_name] = test_platform
                    // }
                }
            }

        }
    }

    def compare_server(ConfigObject platform_metrics, TestTarget test_target, TestTarget compare_target) {
        test_target.test_platforms.each { platform_name, test_platform ->
            def test_metrics = platform_metrics[platform_name]
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
                count_compare_result(test_result)
            }
        }
    }

    def visit_test_scenario(TestScenario test_scenario) {
        def targets = test_scenario.test_targets.get_all()
        def domain_metrics = test_scenario.test_metrics.get_all()
        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                def platform_metrics = domain_metrics[domain].get_all()
                def compare_server = test_target.compare_server
                if (!compare_server)
                    return
                def compare_target = test_scenario.test_targets.get(compare_server,
                                                                    domain)
                if (!compare_target) {
                    def msg = "Compare server not found : ${compare_server}"
                    throw new IllegalArgumentException(msg)
                }
                this.compare_server(platform_metrics, test_target, compare_target)
            }
        }
        this.sumup_compare_count(test_scenario)
    }
}

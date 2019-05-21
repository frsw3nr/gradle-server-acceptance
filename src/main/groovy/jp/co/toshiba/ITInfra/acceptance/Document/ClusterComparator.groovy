package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import static groovy.json.JsonOutput.*
import groovy.json.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
class ClusterComparator {
    def surrogate_keys = [:].withDefault{[:]}
    def dummy_results = [:].withDefault{[:]}

    def make_surrogate_keys(TestTarget test_target) {
        test_target.test_platforms.each { platform_name, test_platform ->
            // def compare_platform = compare_target?.test_platforms[platform_name]
            // if (!compare_platform?.test_results)
            //     return
            // compare_target.target_status = RunStatus.COMPARED
            test_platform?.test_results.each { metric_name, test_result ->
                // def compare_result = compare_platform.test_results[metric_name]
                // test_result.compare_server = compare_target.name
                // if (test_result?.value == compare_result?.value) {
                //     test_result.comparision = ResultStatus.MATCH
                // } else {
                //     test_result.comparision = ResultStatus.UNMATCH
                // }
                // println "COMPARE:$platform_name, $metric_name, $test_result.comparision"
                def index = "${platform_name}|${metric_name}"
                def value = test_result?.value
                def id_max = surrogate_keys[index].size()
                if (!surrogate_keys[index].containsKey(value)) {
                    surrogate_keys[index][value] = id_max + 1
                }
                dummy_results[test_target.name][index] = surrogate_keys[index][value]
            }
        }
        def json = new groovy.json.JsonBuilder()
        json(dummy_results)
        println json.toPrettyString()
    }

    def visit_test_scenario(TestScenario test_scenario) {
        def targets = test_scenario.test_targets.get_all()
        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                // def compare_server = test_target.compare_server
                // println "VISIT:$target_name, $domain, $compare_server"
                // if (!compare_server)
                //     return
                // def compare_target = test_scenario.test_targets.get(compare_server,
                //                                                     domain)
                // if (!compare_target) {
                //     def msg = "Compare server not found : ${compare_server}"
                //     throw new IllegalArgumentException(msg)
                // }
                this.make_surrogate_keys(test_target)
            }
        }

    }
}

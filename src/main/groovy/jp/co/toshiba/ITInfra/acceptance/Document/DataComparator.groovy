package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import static groovy.json.JsonOutput.*
import groovy.json.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
class DataComparator {

    def compare_server(TestTarget test_target, TestTarget compare_target) {
        test_target.test_platforms.each { platform_name, test_platform ->
            def compare_platform = compare_target?.test_platforms[platform_name]
            println "COMPARE2: platform_name:${platform_name}"
            if (!compare_platform?.test_results)
                return
            test_platform?.test_results.each { metric_name, test_result ->
                def compare_result = compare_platform.test_results[metric_name]
                if (test_result?.value == compare_result?.value) {
                    test_result.comparision = ResultStatus.MATCH
                } else {
                    test_result.comparision = ResultStatus.UNMATCH
                }
                println "COMAPRE ${metric_name} : ${test_result.comparision}"
            }
        }
    }

    def visit_test_scenario(TestScenario test_scenario) {
        def targets = test_scenario.test_targets.get_all()
        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                def compare_server = test_target.compare_server
                println "COMPARE: ${target_name}, ${domain}, ${compare_server}"
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

    }
}

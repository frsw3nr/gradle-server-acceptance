package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import static groovy.json.JsonOutput.*
import groovy.json.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
class TestResultReader {
    String json_dir
    def status_hash = [
        'OK'      : ResultStatus.OK,
        'NG'      : ResultStatus.NG,
        'WARNING' : ResultStatus.WARNING,
        'MATCH'   : ResultStatus.MATCH,
        'UNMATCH' : ResultStatus.UNMATCH,
        'UNKOWN'  : ResultStatus.UNKOWN,
    ]

    def convert_to_result_status(String status) {
        return(this.status_hash[status])
    }

    TestPlatform read_test_platform_result(String target_name, String platform_name) 
                                       throws IOException {
        def json_file = new File("${json_dir}/${target_name}/${platform_name}.json")
        if(!json_file.exists())
            return
        def results_json = new JsonSlurper().parseText(json_file.text)
        def test_platform = new TestPlatform(name: platform_name,
                                             test_results: results_json)
        test_platform.test_results.each { metric_name, test_result ->
            test_result.status = convert_to_result_status(test_result.status)
            test_result.verify = convert_to_result_status(test_result.verify)
        }
        return test_platform
    }

    def read_test_target_result(TestScenario test_scenario, String target) {
        def domain_metrics = test_scenario.test_metrics.get_all()
        println "domain_metrics:$domain_metrics"
        def targets = test_scenario.test_targets.get_all()
        println "targets:$targets"
        def test_target2 = test_scenario.test_targets.get(target)
        if (test_target2) {
            def domain = test_target2.domain
            def target_name = test_target2.name
            println "platform_metrics: ${domain_metrics[domain]}"
            // def platform_metrics = domain_metrics[domain].get_all()
            // platform_metrics.each { platform_name, platform_metric ->
            //     def test_platform = this.read_test_platform_result(target_name,
            //                                                 platform_name)
            //     if (test_platform) {
            //         test_platform.test_target = test_target
            //         test_target.test_platforms[platform_name] = test_platform
            //     }
            // }

        }
        println "test_target2:$test_target2"

        // targets.each { target_name, domain_targets ->
        //     domain_targets.each { domain, test_target ->
        //         def platform_metrics = domain_metrics[domain].get_all()
        //         platform_metrics.each { platform_name, platform_metric ->
        //             def test_platform = this.read_test_platform_result(target_name,
        //                                                         platform_name)
        //             if (test_platform) {
        //                 test_platform.test_target = test_target
        //                 test_target.test_platforms[platform_name] = test_platform
        //             }
        //         }
        //     }
        // }
    }

    def read_entire_scenario(TestScenario test_scenario) {
        def domain_metrics = test_scenario.test_metrics.get_all()
        def targets = test_scenario.test_targets.get_all()

        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                def platform_metrics = domain_metrics[domain].get_all()
                platform_metrics.each { platform_name, platform_metric ->
                    def test_platform = this.read_test_platform_result(target_name,
                                                                platform_name)
                    if (test_platform) {
                        test_platform.test_target = test_target
                        test_target.test_platforms[platform_name] = test_platform
                    }
                }
            }
        }
    }
}

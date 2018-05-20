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

    def read_test_platform(String target_name, String platform_name) 
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

    def read_entire_scenario(test_scenario) {
        def domain_metrics = test_scenario.test_metrics.get_all()
        def targets = test_scenario.test_targets.get_all()

        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                def platform_metrics = domain_metrics[domain].get_all()
                platform_metrics.each { platform_name, platform_metric ->
                    def test_platform = this.read_test_platform(target_name,
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

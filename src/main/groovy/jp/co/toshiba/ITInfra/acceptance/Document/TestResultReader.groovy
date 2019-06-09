package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.json.JsonSlurper
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
class TestResultReader {
    String result_dir
    def status_hash = [
        'OK'      : ResultStatus.OK,
        'NG'      : ResultStatus.NG,
        'WARNING' : ResultStatus.WARNING,
        'MATCH'   : ResultStatus.MATCH,
        'UNMATCH' : ResultStatus.UNMATCH,
        'UNKOWN'  : ResultStatus.UNKOWN,
    ]

    TestResultReader(Map params) {
        this.result_dir = params['result_dir']
        if(this.result_dir && !(new File(this.result_dir)).exists()){
            throw new IOException("JSON results directory not found : ${this.result_dir}")
        }
    }

    def set_environment(ConfigTestEnvironment env) {
        this.result_dir = env.get_result_dir()
    }

    def convert_to_result_status(String status) {
        return(this.status_hash[status])
    }

    TestPlatform read_test_platform_result(String target_name, String platform_name) 
                                       throws IOException {
        def json_file = new File("${result_dir}/${target_name}/${platform_name}.json")
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

    def read_port_lists(String target_name) throws IOException {
        LinkedHashMap<String, PortList> port_lists = [:]
        new File(result_dir).eachFile { 
            ( it.name =~ /${target_name}__(.+).json/ ).each { json_file, domain ->
                def results_json = new JsonSlurper().parseText(it.text)
                def port_list = results_json?.port_list
                if (port_list) {
                    port_lists << port_list
                }
            }
        }
        // println "RESULT: ${target_name}, ${port_lists}"
        return port_lists
    }

    def read_port_list(String target_name, String domain_name) 
                       throws IOException {
        def json_file = new File("${result_dir}/${target_name}__${domain_name}.json")
        if(!json_file.exists())
            return
        def results_json = new JsonSlurper().parseText(json_file.text)
        return results_json?.port_list
    }

    def read_test_target_result(TestScenario test_scenario, String target_name) {
        def domain_metrics = test_scenario.test_metrics.get_all()
        def target_domains = test_scenario.test_targets.get(target_name)
        target_domains.each { domain_name, test_target ->
            def platform_metrics = domain_metrics[domain_name].get_all()
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

    def read_compare_target_result(TestScenario test_scenario) {
        def domain_metrics = test_scenario.test_metrics.get_all()
        def targets = test_scenario.test_targets.get_all()
        targets.each { target_name, domain_targets ->
            def port_lists = this.read_port_lists(target_name)
            domain_targets.each { domain, test_target ->
                if (test_target.target_status == RunStatus.INIT &&
                    test_target.comparision == true) {
                    test_target.port_list = port_lists
                    // this.read_port_list(target_name, domain)
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

    def read_entire_result(TestScenario test_scenario) {
        def domain_metrics = test_scenario.test_metrics.get_all()
        def targets = test_scenario.test_targets.get_all()

        targets.each { target_name, domain_targets ->
            def port_lists = this.read_port_lists(target_name)
            domain_targets.each { domain, test_target ->
                test_target.port_list = port_lists
                // test_target.port_list = this.read_port_list(target_name, domain)
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

    def visit_test_scenario(test_scenario) {
        this.read_compare_target_result(test_scenario)
    }
}

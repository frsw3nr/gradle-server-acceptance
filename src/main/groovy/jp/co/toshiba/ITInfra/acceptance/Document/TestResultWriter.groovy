package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import static groovy.json.JsonOutput.*
import groovy.json.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
class TestResultWriter {
    String result_dir

    def write_test_platform(String target_name, String platform_name, 
                            TestPlatform test_platform) throws IOException {
        def output_dir = "${result_dir}/${target_name}"
        new File(output_dir).mkdirs()
        new File("${output_dir}/${platform_name}.json").with {
            def json = JsonOutput.toJson(test_platform.test_results)
            it.text = JsonOutput.prettyPrint(json)
        }
    }

    def write_entire_scenario(test_scenario) {
        def targets = test_scenario.test_targets.get_all()

        targets.each { target, domain_targets ->
            domain_targets.each { domain, test_target ->
                test_target.test_platforms.each { platform, test_platform ->
                    this.write_test_platform(target, platform, test_platform)
                }
            }
        }
    }

    def write(test_scenario) {
        def targets = test_scenario.test_targets.get_all()

        targets.each { target, domain_targets ->
            domain_targets.each { domain, test_target ->
                test_target.test_platforms.each { platform, test_platform ->
                    this.write_test_platform(target, platform, test_platform)
                }
            }
        }
    }


    def visit_test_scenario(test_scenario) {
        println "result_dir : $result_dir"
        def resultDir = new File(this.result_dir)
        if (resultDir.exists())
            resultDir.deleteDir()
        this.write_entire_scenario(test_scenario)
    }
}

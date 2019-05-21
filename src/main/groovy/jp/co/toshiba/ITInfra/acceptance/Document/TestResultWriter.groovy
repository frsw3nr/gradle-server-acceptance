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

        // ワークアラウンド対応:
        // ネストしたオブジェクトをJSON変換すると、スタックオーバフロー
        // が発生する。
        // 回避策として、 TestResultクラスの devices メンバーを除外して
        // 変換するように変更
        // 関連記事：
        // https://stackoverflow.com/questions/26664953/caught-java-lang-stackoverflowerror-jsonbuilder-closure
        // https://issues-test.apache.org/jira/browse/GROOVY-7682
        def result_info = [:]
        test_platform?.test_results.each { metric, test_result ->
            if (test_result.getClass() == TestResult) {
                result_info[metric] = test_result.asMap()
            }
        }

        if (result_info) {
            new File("${output_dir}/${platform_name}.json").with {
                def json = JsonOutput.toJson(result_info)
                it.text = JsonOutput.prettyPrint(json)
                // def json = new JsonBuilder(test_platform.test_results)
                // it.text = json.toString()
            }
        }
    }

    def get_port_lists(TestTarget test_target) {
        def port_list_info = [:]
        test_target.test_platforms.each { platform, test_platform ->
            test_platform?.port_lists.each { address, port_list ->
                port_list_info[address] = port_list.asMap()
            }
        }
        return port_list_info
    }

    def write_test_target(String target_name, TestTarget test_target)
        throws IOException {
        new File(result_dir).mkdirs()
        def domain = test_target.domain
        def target_info = [:]
        test_target.with {
            target_info['name']           = it.name
            target_info['ip']             = it.ip
            target_info['template_id']    = it.template_id
            target_info['compare_server'] = it.compare_server
            target_info['target_status']  = it.target_status
            target_info['platforms']      = it.test_platforms.keySet()
            target_info['port_list']      = get_port_lists(it)
        }
        def json = JsonOutput.toJson(target_info)
        new File("${result_dir}/${target_name}__${domain}.json").with {
            it.text = JsonOutput.prettyPrint(json)
            // println "TARGET JSON OUTPUT: ${JsonOutput.prettyPrint(json)}"
        }
    }

    def write_entire_scenario(test_scenario) {
        def targets = test_scenario.test_targets.get_all()

        targets.each { target, domain_targets ->
            domain_targets.each { domain, test_target ->
                this.write_test_target(target, test_target)
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
        def resultDir = new File(this.result_dir)
        if (resultDir.exists())
            resultDir.deleteDir()
        this.write_entire_scenario(test_scenario)
    }
}

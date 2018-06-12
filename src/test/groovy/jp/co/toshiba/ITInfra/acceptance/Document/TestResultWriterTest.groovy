import spock.lang.Specification
import static groovy.json.JsonOutput.*
import groovy.json.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "TestResultWriterTest.全体の JSON 実行結果読み込み"

class TestResultWriterTest extends Specification {

    def config_file = 'src/test/resources/config.groovy'
    def excel_parser
    def test_scenario
    def evidence_maker

    def setup() {
        excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
    }

    def "DryRun 実行結果JSON保存"() {
        when:
        def test_scheduler = new TestScheduler()
        test_scenario.accept(test_scheduler)
        def test_result_writer = new TestResultWriter(json_dir: 'build/evidence')
        test_result_writer.write_entire_scenario(test_scenario)

        then:
        1 == 1
    }

    def "比較対象の DryRun 実行とJSON結果保存"() {
        setup:
        String[] args = [
            '--dry-run',
            '-c', './src/test/resources/config.groovy',
            '-excel', 'src/test/resources/check_sheet_target.xlsx'
        ]

        when:
        def test_runner = new TestRunner()
        test_runner.parse(args)
        def test_env = ConfigTestEnvironment.instance
        test_env.read_from_test_runner(test_runner)
        def test_scheduler = new TestScheduler()
        test_env.set_test_schedule_environment(test_scheduler)
        test_scheduler.init()
        test_scheduler.run()
        def test_result_writer = new TestResultWriter(json_dir: 'build/evidence')
        test_result_writer.write_entire_scenario(test_scheduler.test_scenario)

        then:
        1 == 1
    }

    // def "比較対象の DryRun 実行とJSON結果保存"() {
    //     when:
    //     excel_parser = new ExcelParser('src/test/resources/check_sheet_target.xlsx')
    //     excel_parser.scan_sheet()
    //     test_scenario = new TestScenario(name: 'root')
    //     test_scenario.accept(excel_parser)
    //     def test_scheduler = new TestScheduler()
    //     test_scenario.accept(test_scheduler)
    //     def test_result_writer = new TestResultWriter(json_dir: 'build/evidence')
    //     test_result_writer.write_entire_scenario(test_scenario)

    //     then:
    //     1 == 1
    // }

    def "特定ターゲットのJSON 実行結果読み込み"() {
        when:
        def test_result_reader = new TestResultReader(json_dir: 'src/test/resources/json')
        test_result_reader.read_test_target_result(test_scenario, 'cent7')

        then:
        def targets = test_scenario.test_targets.get_all()
        targets.each { target_name, domain_targets ->
            println "TARGET_NAME: $target_name"
            domain_targets.each { domain, test_target ->
                println " DOMAIN_NAME: $domain"
                println " STATUS: ${test_target.target_status}, COMPARE: ${test_target.comparision}"
                test_target.test_platforms.each { platform_name, test_platform ->
                    println "  PLATFORM_NAME: $platform_name"
                    // println "TARGET : $target_name, $domain, $platform_name"
                    test_platform.test_results.size() > 0
                }
            }
        }
    }

    def "JSON 実行結果読み込み"() {
        when:
        def test_result_reader = new TestResultReader(json_dir: 'src/test/resources/json')
        test_result_reader.read_entire_result(test_scenario)

        then:
        def targets = test_scenario.test_targets.get_all()
        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                test_target.test_platforms.each { platform_name, test_platform ->
                    println "$target_name, $domain, $platform_name"
                    test_platform.test_results.size() > 0
                }
            }
        }
    }

    def "比較対象の JSON 実行結果読み込み"() {
        when:
        def test_result_reader = new TestResultReader(json_dir: 'src/test/resources/json')
        test_result_reader.read_compare_target_result(test_scenario)

        then:
        1 == 1
        def targets = test_scenario.test_targets.get_all()
        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                println " TARGET: $target_name, $domain"
                println " STATUS: ${test_target.target_status}, COMPARE: ${test_target.comparision}"
                test_target.test_platforms.each { platform_name, test_platform ->
                    println "  PLATFORM_NAME: $platform_name"
                    println "  RESULTS: ${test_platform.test_results}"
                }
            }
        }
    }

    def "JSON 実行結果単体読み込み"() {
        setup:
        def json_text = """\
        |{
        |    "hostname": {
        |        "devices": null,
        |        "value": "ostrich",
        |        "status": "OK",
        |        "custom_fields": {
        |            
        |        },
        |        "verify": null,
        |        "name": "hostname"
        |    },
        |    "network": {
        |        "devices": {
        |            "header": [
        |                "device",
        |                "ip",
        |                "mtu",
        |                "state",
        |                "mac",
        |                "subnet"
        |            ],
        |            "csv": [
        |                [
        |                    "eth0",
        |                    "NaN",
        |                    "1500",
        |                    "UP",
        |                    "00:0c:29:ca:44:db",
        |                    "NaN"
        |                ]
        |            ],
        |            "custom_fields": {
        |                
        |            }
        |        },
        |        "value": "[eth0:null]",
        |        "status": "OK",
        |        "custom_fields": {
        |            
        |        },
        |        "verify": null,
        |        "name": "network"
        |    }
        |}
        """.stripMargin()

        when:
        def slurper = new groovy.json.JsonSlurper()
        def result = slurper.parseText(json_text)
        def result_hostname = new TestResult(result['hostname'])
        println result_hostname
        def result_network = new TestResult(result['network'])
        println result_network

        then:
        result_hostname != null
        result_network != null
    }

    def "JSON 実行結果ファイル読み込み"() {
        when:
        def results_text = new File("src/test/resources/json/ostrich/Linux.json").text
        def results_json = new JsonSlurper().parseText(results_text)
        def test_platform = new TestPlatform(name: 'Linux', test_results: results_json)
        def status_hash = [
            'OK'      : ResultStatus.OK,
            'NG'      : ResultStatus.NG,
            'WARNING' : ResultStatus.WARNING,
            'MATCH'   : ResultStatus.MATCH,
            'UNMATCH' : ResultStatus.UNMATCH,
            'UNKOWN'  : ResultStatus.UNKOWN,
        ]
        test_platform.test_results.each { metric_name, test_result ->
            test_result.status = status_hash[test_result.status]
            test_result.verify = status_hash[test_result.verify]
        }

        then:
        println test_platform.test_results['hostname']
        println test_platform.test_results['network']
        test_platform.test_results['hostname'].status == ResultStatus.OK
        test_platform.test_results['network'].status  == ResultStatus.OK
        test_platform.test_results['hostname'].status != 'OK'
        test_platform.test_results['network'].status  != 'OK'
    }

    // def "実行結果Excel書き込み"() {
    //     when:
    //     println test_scenario.test_targets.get_all()
    //     evidence_maker.command = EvidenceMakerCommand.READ_JSON
    //     test_scenario.accept(evidence_maker)
    //     evidence_maker.command = EvidenceMakerCommand.OUTPUT_EXCEL
    //     test_scenario.accept(evidence_maker)

    //     then:
    //     1 == 1
    //     // def targets = test_scenario.test_targets.get_all()
    //     // targets.each { target_name, domain_targets ->
    //     //     domain_targets.each { domain, test_target ->
    //     //         test_target.test_platforms.each { platform_name, test_platform ->
    //     //             println "$target_name, $domain, $platform_name"
    //     //             test_platform.test_results.size() > 0
    //     //         }
    //     //     }
    //     // }
    // }

}


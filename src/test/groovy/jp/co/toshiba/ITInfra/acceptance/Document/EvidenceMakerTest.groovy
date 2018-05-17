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

// gradle --daemon test --tests "EvidenceMakerTest.DryRun シナリオ実行"

class EvidenceMakerTest extends Specification {

    def config_file = 'src/test/resources/config.groovy'
    def excel_parser
    def test_scenario
    def evidence_maker

    def setup() {
        excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        evidence_maker = new EvidenceMaker(excel_parser: excel_parser)
        def test_env = ConfigTestEnvironment.instance
        // 'src/test/resources/json/'
        test_env.read_config(config_file)
        test_env.set_evidence_environment(evidence_maker)
    }

    def "DryRun シナリオ実行"() {
        when:
        def test_scheduler = new TestScheduler()
        test_scenario.accept(test_scheduler)
        evidence_maker = new EvidenceMaker(excel_parser: excel_parser)
        // test_scenario.accept(evidence_maker)

        then:
        1 == 1
    }

    def "DryRun 実行結果JSON保存"() {
        when:
        def test_scheduler = new TestScheduler()
        test_scenario.accept(test_scheduler)
        evidence_maker.command = EvidenceMakerCommand.OUTPUT_JSON
        test_scenario.accept(evidence_maker)

        then:
        1 == 1
    }

    def "JSON 実行結果読み込み"() {
        when:
        println test_scenario.test_targets.get_all()
        evidence_maker.command = EvidenceMakerCommand.READ_JSON
        test_scenario.accept(evidence_maker)

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

    def "実行結果Excel書き込み"() {
        when:
        println test_scenario.test_targets.get_all()
        evidence_maker.command = EvidenceMakerCommand.READ_JSON
        test_scenario.accept(evidence_maker)
        evidence_maker.command = EvidenceMakerCommand.OUTPUT_EXCEL
        test_scenario.accept(evidence_maker)

        then:
        1 == 1
        // def targets = test_scenario.test_targets.get_all()
        // targets.each { target_name, domain_targets ->
        //     domain_targets.each { domain, test_target ->
        //         test_target.test_platforms.each { platform_name, test_platform ->
        //             println "$target_name, $domain, $platform_name"
        //             test_platform.test_results.size() > 0
        //         }
        //     }
        // }
    }

}


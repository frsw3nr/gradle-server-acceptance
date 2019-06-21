import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import spock.lang.Specification

// gradle --daemon test --tests "TagGeneratorTest2.結果の比較"

class TagGeneratorTest2 extends Specification {
    TestScenario test_scenario
    ConfigTestEnvironment test_env
    ExcelParser excel_parser

    def setup() {
        excel_parser = new ExcelParser('src/test/resources/サーバチェックシート3.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        test_env = ConfigTestEnvironment.instance
        test_env.read_config('src/test/resources/config.groovy')
        test_env.config.dry_run = true
    }

    def JSON結果読み込みエラー() {
        when:
        def test_result_reader = new TestResultReader(node_dir: 'src/test/resources/hoge')
        test_result_reader.read_entire_result(test_scenario)

        then:
        thrown(IOException)
        1 == 1
    }

    def 結果の比較() {
        when:
        def test_result_reader = new TestResultReader(node_dir: 'src/test/resources/json')
        test_result_reader.read_entire_result(test_scenario)
        def tag_generator2 = new TagGenerator2()
        test_scenario.accept(tag_generator2)

        then:
        def comparitions = [:].withDefault{0}
        def targets = test_scenario.test_targets.get_all()
        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                println "TAG:$domain, $target_name, ${test_target.tag}"
                test_target.test_platforms.each { platform_name, test_platform ->
                    test_platform.test_results.each { metric_name, test_result ->
                        comparitions[test_result.comparision] ++
                    }
                }
            }
        }
        1 == 1
        println "comparitions: ${comparitions}"
        // comparitions[ResultStatus.MATCH] > 0
        // comparitions[ResultStatus.UNMATCH] > 0
    }

    def 比較結果のExcel更新() {
        when:
        def test_result_reader = new TestResultReader(node_dir: 'src/test/resources/json')
        test_result_reader.read_entire_result(test_scenario)
        def tag_generator2 = new TagGenerator2()
        test_scenario.accept(tag_generator2)
        println test_scenario.test_targets.get_keys()
        def evidence_maker = new EvidenceMaker()
        test_scenario.accept(evidence_maker)
        println evidence_maker
        def excel_sheet_maker = new ExcelSheetMaker(
                                    excel_parser: excel_parser,
                                    evidence_maker: evidence_maker)
        excel_sheet_maker.output('build/check_sheet2.xlsx')

        then:
        1 == 1
    }
}

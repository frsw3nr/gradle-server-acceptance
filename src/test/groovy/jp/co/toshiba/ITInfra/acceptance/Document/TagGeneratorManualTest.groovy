import groovy.json.JsonOutput
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import spock.lang.Specification

// gradle --daemon test --tests "TagGeneratorManualTest.比較集計"

class TagGeneratorManualTest extends Specification {
    TestScenario test_scenario
    ConfigTestEnvironment test_env
    ExcelParser excel_parser

    def setup() {
        excel_parser = new ExcelParser('サーバチェックシート.xlsx')
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
        def tag_generator = new TagGeneratorManual2()
        test_scenario.accept(tag_generator)

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

    def カウンタ集計() {
        when:
        def compare_counter = new CompareCounter()

        compare_counter.count_up('cent7', 'Linux', 'uname', ResultStatus.MATCH)
        compare_counter.count_up('cent7', 'Linux', 'uname', ResultStatus.MATCH)
        compare_counter.count_up('cent7', 'Linux', 'uname', ResultStatus.UNMATCH)
        compare_counter.count_up('cent7', 'Linux', 'uname', ResultStatus.MATCH)
        compare_counter.count_up('cent7', 'Linux', 'kernel', ResultStatus.MATCH)
        compare_counter.count_up('cent6', 'Linux', 'uname', ResultStatus.MATCH)

        then:
        println compare_counter
        1 == 1
    }

    def 比較集計() {
        when:
        def test_result_reader = new TestResultReader(node_dir: 'src/test/resources/json')
        test_result_reader.read_entire_result(test_scenario)
        def tag_generator = new TagGeneratorManual2()
        test_scenario.accept(tag_generator)
        def data_comparator = new DataComparator()
        test_scenario.accept(data_comparator)

        then:
        def compare_counter = data_comparator.compare_counter
        compare_counter.is_empty() == false
        def platform_counter = compare_counter.get_platform_counter('cent7', 'Linux')
        // def json = JsonOutput.toJson(platform_counter)
        // println JsonOutput.prettyPrint(json)
        def test_platform = test_scenario.get_test_platform('TAG:cent7', 'Linux')
        println test_platform.test_results
        1 == 1
    }

    def 比較結果のExcel更新() {
        when:
        def test_result_reader = new TestResultReader(node_dir: 'src/test/resources/json')
        test_result_reader.read_entire_result(test_scenario)
        def tag_generator = new TagGeneratorManual2()
        test_scenario.accept(tag_generator)
        def cent7g = test_scenario?.test_targets?.get('cent7g', 'Linux')
        cent7g.target_status = RunStatus.FINISH
        println "CENT7G :${cent7g}"
        def data_comparator = new DataComparator()
        test_scenario.accept(data_comparator)
        println test_scenario.test_targets.get_keys()
        def evidence_maker = new EvidenceMaker()
        test_scenario.accept(evidence_maker)
        def summary_sheet = evidence_maker.summary_sheets['Linux']
        println "SUMMARY_SHEET : ${summary_sheet}"
        println "SUMMARY_SHEET COLS : ${summary_sheet.cols}"

        // println evidence_maker
        def excel_sheet_maker = new ExcelSheetMaker(
                                    excel_parser: excel_parser,
                                    evidence_maker: evidence_maker)
        excel_sheet_maker.output('build/check_sheet2.xlsx')

        then:
        1 == 1
    }
}

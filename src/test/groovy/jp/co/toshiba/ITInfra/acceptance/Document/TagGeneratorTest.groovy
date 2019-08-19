import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import spock.lang.Specification

// gradle --daemon test --tests "TagGeneratorTest.結果の比較"

class TagGeneratorTest extends Specification {
    TestScenario test_scenario
    ConfigTestEnvironment test_env
    ExcelParser excel_parser

    def setup() {
        excel_parser = new ExcelParser('src/test/resources/サーバチェックシート.xlsx')
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
        def test_result_reader = new TestResultReader(node_dir: 'src/test/resources/node')
        test_result_reader.read_entire_result(test_scenario)
        def tag_generator = new TagGenerator()
        tag_generator.cluster_size = 2
        test_scenario.accept(tag_generator)

        then:
        tag_generator.domain_clusters['Linux'].index_rows.size() > 0
        def count = 0
        test_scenario.test_targets.get_keys().each {
            (it =~ /TAG:/).each {
                count ++
            }
        }
        count > 0
    }

    def 比較結果のExcel更新() {
        when:
        def test_result_reader = new TestResultReader(node_dir: 'src/test/resources/node')
        test_result_reader.read_entire_result(test_scenario)
        // def cent7g = test_scenario?.test_targets?.get('cent7g', 'Linux')
        // cent7g.target_status = RunStatus.FINISH
        def fin = RunStatus.FINISH
        test_scenario?.test_targets?.get('cent7g',  'Linux').target_status = fin
        test_scenario?.test_targets?.get('cent7',   'Linux').target_status = fin
        test_scenario?.test_targets?.get('ostrich', 'Linux').target_status = fin
        // println "CENT7G :${cent7g}"
        def tag_generator = new TagGenerator(cluster_size: 1)
        tag_generator.cluster_size = 2
        test_scenario.accept(tag_generator)
        def data_comparator = new DataComparator()
        test_scenario.accept(data_comparator)
        def evidence_maker = new EvidenceMaker()
        test_scenario.accept(evidence_maker)
        def excel_sheet_maker = new ExcelSheetMaker(
                                    excel_parser: excel_parser,
                                    evidence_maker: evidence_maker)
        excel_sheet_maker.output('build/check_sheet2.xlsx')

        then:
        1 == 1
    }
}

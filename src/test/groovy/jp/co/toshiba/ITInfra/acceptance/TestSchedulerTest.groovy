import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "TestSchedulerTest.比較対象の DryRun 実行とJSON結果保存"

class TestSchedulerTest extends Specification {

    TestRunner test_runner
    TestScenario test_scenario
    PlatformTester platform_tester
    ExcelParser excel_parser

    def setup() {
        String[] args = [
            '--dry-run',
            '-c', './src/test/resources/config.groovy',
        ]
        test_runner = new TestRunner()
        test_runner.parse(args)

        excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        platform_tester = new PlatformTester()
    }

    def "初期化"() {
        when:
        def excel_file = 'src/test/resources/check_sheet.xlsx'
        def output_evidence = 'build/check_sheet.xlsx'
        def test_scheduler = new TestScheduler(excel_file : excel_file)
        test_scheduler.init()

        then:
        test_scheduler.test_scenario != null
    }

    def "テストタスク作成"() {
        when:
        def test_scheduler = new TestScheduler()
        def tasks = test_scheduler.make_test_platform_tasks(test_scenario)

        then:
        tasks.size() == 3
    }

    def "ターゲット絞り込み"() {
        when:
        def test_scheduler = new TestScheduler(filter_server: 'ostrich')
        def tasks = test_scheduler.make_test_platform_tasks(test_scenario)
        println "TASKS:${tasks['vCenter']['ostrich'].test_metrics}"

        then:
        tasks.size() == 2
    }

    def "メトリック絞り込み"() {
        when:
        def test_scheduler = new TestScheduler(filter_metric: 'uname')
        def tasks = test_scheduler.make_test_platform_tasks(test_scenario)

        then:
        tasks.size() == 1
    }

    def "シナリオ結合 1"() {
        when:
        def excel_file = 'src/test/resources/check_sheet.xlsx'
        def output_evidence = 'build/check_sheet1.xlsx'
        def test_scheduler = new TestScheduler(platform_tester : platform_tester,
                                               excel_parser : excel_parser,
                                               excel_file : excel_file,
                                               output_evidence: output_evidence)
        test_scenario.accept(test_scheduler)
        def evidence_maker = new EvidenceMaker()
        test_scenario.accept(evidence_maker)
        def excel_sheet_maker = new ExcelSheetMaker(
                                    excel_parser: excel_parser,
                                    evidence_maker: evidence_maker)
        excel_sheet_maker.output('build/check_sheet.xlsx')

        then:
        1 == 1
    }

    def "シナリオ結合 2"() {
        when:
        def excel_file = 'src/test/resources/check_sheet.xlsx'
        def output_evidence = 'build/check_sheet2.xlsx'

        def test_scheduler = new TestScheduler(platform_tester : platform_tester,
                                               excel_file : excel_file,
                                               output_evidence: output_evidence)
        test_scheduler.init()
        test_scheduler.run()
        test_scheduler.finish()

        then:
        1 == 1
    }

    def "多重実行"() {
        when:
        def test_scheduler = new TestScheduler(serialize_platforms: ['vCenter': true],
                                               parallel_degree: 3,
                                               platform_tester: platform_tester)
        test_scenario.accept(test_scheduler)

        then:
        1 == 1
    }

}

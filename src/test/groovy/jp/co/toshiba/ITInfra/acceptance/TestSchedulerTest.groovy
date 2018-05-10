import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import static groovy.json.JsonOutput.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "TestSchedulerTest"

class TestSchedulerTest extends Specification {
    TestRunner test_runner
    TestScenario test_scenario
    PlatformTester platform_tester

    def setup() {
        String[] args = [
            '--dry-run',
            '-c', './src/test/resources/config.groovy',
            '-resource', './src/test/resources/log',
            '--parallel', '3',
        ]
        test_runner = new TestRunner()
        test_runner.parse(args)

        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        platform_tester = new PlatformTester()
    }

    def "初期化"() {
        when:
        def test_scheduler = new TestScheduler(test_runner: test_runner)
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

    def "シナリオ実行"() {
        when:
        def test_scheduler = new TestScheduler(platform_tester: platform_tester)
        test_scenario.accept(test_scheduler)

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

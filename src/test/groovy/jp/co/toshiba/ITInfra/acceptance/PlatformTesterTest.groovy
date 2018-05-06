import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import static groovy.json.JsonOutput.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "PlatformTesterTest.初期化"

class PlatformTesterTest extends Specification {
    def test_platform_tasks = [:].withDefault{[]}
    TestRunner test_runner

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

        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
        test_scenario.test_targets.copy('centos7', "ostrich")

        def test_scheduler = new TestScheduler(test_runner: test_runner)
        test_platform_tasks = test_scheduler.make_test_platform_tasks(test_scenario)
    }

    def "初期化"() {
        when:
        def platform_tester = new PlatformTester(test_runner: test_runner)
        // platform_tester.init()

        then:
        platform_tester != null
    }

    def "Linuxテストタスク"() {
        setup:
        def platform_tester = new PlatformTester(test_runner: test_runner)

        when:
        def test_platform_task = test_platform_tasks['Linux']['ostrich']
        platform_tester.run(test_platform_task)

        then:
        1 == 1
    }

    // def "テストタスク絞り込み"() {
    //     when:
    //     def test_scheduler = new PlatformTester(filter_server: 'centos7')
    //     def tasks = test_scheduler.make_test_platform_tasks(test_scenario)

    //     then:
    //     tasks.size() == 2
    // }

    // def "シナリオ読み込み"() {
    //     when:
    //     def test_scheduler = new PlatformTester(platform_tester: platform_tester)
    //     test_scenario.accept(test_scheduler)

    //     then:
    //     1 == 1
    // }

    // def "多重実行"() {
    //     when:
    //     def test_scheduler = new PlatformTester(serialize_platforms: ['vCenter': true],
    //                                            parallel_degree: 3,
    //                                            platform_tester: platform_tester)
    //     test_scenario.accept(test_scheduler)

    //     then:
    //     1 == 1
    // }
}

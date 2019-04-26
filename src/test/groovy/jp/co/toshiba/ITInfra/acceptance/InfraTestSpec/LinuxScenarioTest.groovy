import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "LinuxScenarioTest.シナリオ結合 3"

class LinuxScenarioTest extends Specification {

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
        def test_env = ConfigTestEnvironment.instance
        test_env.read_from_test_runner(test_runner)

        excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        platform_tester = new PlatformTester(config_file: './src/test/resources/config.groovy')
    }

    def "シナリオ結合 3"() {
        when:
        def excel_file = 'src/test/resources/check_sheet.xlsx'
        def output_evidence = 'build/check_sheet2.xlsx'
        println "config_file:${platform_tester.config_file}"
        def test_scheduler = new TestScheduler(platform_tester : platform_tester,
                                               excel_file : excel_file,
                                               output_evidence: output_evidence,
                                               result_dir : 'build/json',
                                               )
        test_scheduler.init()
        test_scheduler.filter_metric = 'filesystem';
        test_scheduler.filter_server = 'ostrich';
        println "FILTER METRIC : ${test_scheduler.filter_metric}"
        println "FILTER SERVER : ${test_scheduler.filter_server}"
        test_scheduler.run()
        test_scheduler.finish()

        then:
        1 == 1
    }

}

import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.Document.*

import org.apache.poi.ss.usermodel.Workbook

// gradle --daemon test --tests "EvidenceManagerTest.エクスポート"

class EvidenceManagerTest extends Specification {
    TestScenario test_scenario
    ConfigTestEnvironment test_env
    PlatformTester platform_tester
    ExcelParser excel_parser

    def setup() {
        String[] args = [
            '--dry-run',
            '-c', './src/test/resources/config.groovy',
            '-excel', 'src/test/resources/check_sheet.xlsx'
        ]

        test_env = ConfigTestEnvironment.instance
        def test_runner = new TestRunner()
        test_runner.parse(args)
        test_env.read_from_test_runner(test_runner)
        test_env.config.evidence.result_dir = './build/json'

        excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
    }

    def 初期化() {
        when:
        def evidence_manager = new EvidenceManager()
        test_env.accept(evidence_manager)

        then:
        1 == 1
    }

    def エクスポート() {
        setup:
        def test_result_reader = new TestResultReader(result_dir: 'src/test/resources/json')
        test_scenario.accept(test_result_reader)

        when:
        def evidence_manager = new EvidenceManager()
        test_env.accept(evidence_manager)
        evidence_manager.export_json(test_scenario)
        evidence_manager.archive_json()
        
        then:
        1 == 1
    }
}

import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.ExcelParser
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import spock.lang.Specification

// gradle --daemon test --tests "EvidenceManagerTest.実行結果の登録"

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

        test_env.get_cmdb_config('src/test/resources/cmdb.groovy')
        test_env.accept(CMDBModel.instance)

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

    def 実行結果の登録() {
        setup:
        def test_scheduler = new TestScheduler()
        test_env.accept(test_scheduler)
        test_scheduler.init()
        test_scheduler.run()
        test_scheduler.finish()

        when:
        def evidence_manager = new EvidenceManager()
        test_env.accept(evidence_manager)
        evidence_manager.export_cmdb()

        then:
        def cmdb_model = CMDBModel.instance
        def node = cmdb_model.cmdb.rows("select * from nodes")
        def result = cmdb_model.cmdb.rows("select * from test_results where node_id = 1")
        def metric = cmdb_model.cmdb.rows("select * from metrics")
        def json = new groovy.json.JsonBuilder()
        json(metric)
        println json.toPrettyString()
        node[0]['node_name'].size() > 0
        node[1]['node_name'].size() > 0
        result.size() > 0
    }

    def 過去の実行結果の登録() {
        when:
        test_env.config.node_dir = './src/test/resources/node'
        def evidence_manager = new EvidenceManager()
        test_env.accept(evidence_manager)
        evidence_manager.export_cmdb_all()

        then:
        def cmdb_model = CMDBModel.instance
        def node = cmdb_model.cmdb.rows("select * from nodes")
        def result = cmdb_model.cmdb.rows("select * from test_results where node_id = 1")
        def metric = cmdb_model.cmdb.rows("select * from metrics")
        def json = new groovy.json.JsonBuilder()
        json(metric)
        println json.toPrettyString()
        node[0]['node_name'].size() > 0
        node[1]['node_name'].size() > 0
        result.size() > 0
    }

    // def エクスポート() {
    //     setup:
    //     def test_result_reader = new TestResultReader(result_dir: 'src/test/resources/json')
    //     test_scenario.accept(test_result_reader)

    //     when:
    //     def evidence_manager = new EvidenceManager()
    //     test_env.accept(evidence_manager)
    //     evidence_manager.export_json(test_scenario)
    //     evidence_manager.archive_json()
        
    //     then:
    //     1 == 1
    // }
}

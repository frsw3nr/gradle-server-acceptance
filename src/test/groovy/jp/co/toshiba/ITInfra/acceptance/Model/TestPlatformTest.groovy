import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.TestScheduler
import jp.co.toshiba.ITInfra.acceptance.Document.ExcelParser
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import spock.lang.Specification

// gradle --daemon test --tests "TestPlatformTest.リスト検索"

class TestPlatformTest extends Specification {
    TestScheduler test_scheduler = new TestScheduler()
    TestScenario test_scenario
    ConfigTestEnvironment test_env

    def setup() {
        test_env = ConfigTestEnvironment.instance
        test_env.read_config('src/test/resources/config.groovy')
    }

    def "初期化"() {
        setup:
        def param1 = ['name': 'Linux', "enabled": false]

        when:
        TestPlatform d1 = new TestPlatform(param1)
        TestPlatform d2 = new TestPlatform(name: 'Linux', enabled: false)
        TestPlatform d3 = new TestPlatform()
        d3.with {name = 'Windows' }

        then:
        [d1.name, d1.enabled]  == ['Linux', false]
        [d2.name, d2.enabled]  == ['Linux', false]
        [d3.name, d3.enabled] == ['Windows', null]
    }

    def "設定ファイル読み込み"() {
        when:
        TestTarget test_target = new TestTarget(name: 'ostrich', domain: 'Linux')
        TestPlatform test_platform = new TestPlatform(name: 'Linux', 
                                                      test_target: test_target,
                                                      enabled: false)
        test_env.accept(test_platform)

        then:
        test_platform.project_test_log_dir   == './src/test/resources/log'
        test_platform.evidence_log_share_dir == './build/log'
        test_platform.current_test_log_dir   == './build/log/ostrich'
        test_platform.local_dir              == './build/log/ostrich/Linux'
    }

    def "テストプラットフォーム検索"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'OS情報採取')
        test_scenario.accept(excel_parser)
        test_scheduler.make_test_platform_tasks(test_scenario)
        def test_platform = test_scenario.get_test_platform('ostrich', 'Linux')

        then:
        test_platform.getDomain()   == 'Linux'
        test_platform.getTarget()   == 'ostrich'
        test_platform.getPlatform() == 'Linux'
    }

    def "検査対象コピー"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'OS情報採取')
        test_scenario.accept(excel_parser)

        when:
        def test_targets = test_scenario.test_targets
        (1..10).each { index ->
            test_targets.copy('centos7', "hoge${index}")
        }

        then:
        test_targets.get_all().size() > 2
    }
}

import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.Document.ExcelParser
import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import spock.lang.Specification

// gradle --daemon test --tests "TestPlatformTest.リスト検索"

class TestPlatformTest extends Specification {
    TestScenario test_scenario

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

    def "チェックシート読み込み"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'OS情報採取')
        test_scenario.accept(excel_parser)

        then:
        1 == 1
    }

    def "リスト検索"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        then:
        def opts = [:]
        def test_platforms = TestPlatform.search(test_scenario)
        println test_platforms
        1 == 1
    }

    def "テストプラットフォーム検索"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'OS情報採取')
        test_scenario.accept(excel_parser)
        then:
        def test_platform = test_scenario.get_test_platform('ostrich', 'Linux')
        println test_platform
        1 == 1
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

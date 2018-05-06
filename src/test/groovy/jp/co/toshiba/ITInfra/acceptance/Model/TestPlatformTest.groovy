import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*

// gradle --daemon test --tests "TestPlatformTest.チェックシート読み込み"

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

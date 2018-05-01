import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*

// gradle --daemon clean test --tests "ExcelParserTest.チェックシートパース"

class ExcelParserTest extends Specification {

    def "初期化"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')

        then:
        1 == 1
    }

    def "シート読み込み"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        excel_parser.sheets.check_sheet.each { domain_name, sheet->
            println "Domain:$domain_name"
        }

        then:
        excel_parser.sheets.keySet() as List == ['target', 'check_sheet', 'check_rule']
        excel_parser.sheets.check_sheet.keySet() as List == ['Linux', 'Windows', 'VMHost']
    }

    def "チェックシートパース"() {
        setup:
        def domains = ['Linux', 'Windows', 'VMHost']
        def templates = [:]

        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        domains.each { domain ->
            def template = new TestDomainTemplate(name: domain)
            template.accept(excel_parser)
            templates[domain] = template
        }

        then:
        domains.each { domain ->
            templates[domain].name == domain
            templates[domain].test_metrics.size() > 0
        }
    }

    def "検査対象パース"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def target_set = new TestTargetSet(name: 'root')
        target_set.accept(excel_parser)
        def test_targets = target_set.get_all()

        then:
        test_targets['centos7'].Linux.verify_id   == 'AP'
        test_targets['win2012'].Windows.verify_id == 'AP'
    }

    def "ルール定義パース"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def rule_set = new TestRuleSet(name: 'root')
        rule_set.accept(excel_parser)
        def test_rules = rule_set.get_all()
        println test_rules

        then:
        1 == 1
        // test_targets['centos7'].Linux.verify_id   == 'AP'
        // test_targets['win2012'].Windows.verify_id == 'AP'
    }

    def "シート全体パース"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'OS情報採取')
        test_scenario.accept(excel_parser)

        then:
        1 == 1
    }

}

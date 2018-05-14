import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*

// gradle --daemon test --tests "EvidenceMakerTest.初期化"

class EvidenceMakerTest extends Specification {

    def excel_parser
    def test_scenario
    def evidence_maker

    def setup() {
        excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
    }

    def "初期化"() {
        when:
        evidence_maker = new EvidenceMaker(excel_parser: excel_parser)

        then:
        1 == 1
    }

    // def "シート読み込み"() {
    //     when:
    //     def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
    //     excel_parser.scan_sheet()
    //     excel_parser.sheet_sources.check_sheet.each { domain_name, sheet->
    //         println "Domain:$domain_name"
    //     }

    //     then:
    //     excel_parser.sheet_sources.keySet() as List == ['target', 'check_sheet', 'check_rule']
    //     excel_parser.sheet_sources.check_sheet.keySet() as List == ['Linux', 'Windows', 'VMHost']
    // }

    // def "チェックシートパース"() {
    //     setup:
    //     def domains = ['Linux', 'Windows', 'VMHost']
    //     def test_metric_sets = [:]

    //     when:
    //     def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
    //     excel_parser.scan_sheet()
    //     domains.each { domain ->
    //         def source = excel_parser.sheet_sources.check_sheet."$domain"
    //         test_metric_sets[domain] = new TestMetricSet(name: domain)
    //         test_metric_sets[domain].accept(excel_parser)
    //     }

    //     then:
    //     domains.each { domain ->
    //         test_metric_sets[domain].name == domain
    //         test_metric_sets[domain].count() > 0
    //     }
    // }

    // def "検査対象パース"() {
    //     when:
    //     def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
    //     excel_parser.scan_sheet()
    //     def target_set = new TestTargetSet(name: 'root')
    //     target_set.accept(excel_parser)
    //     def test_targets = target_set.get_all()

    //     then:
    //     test_targets['centos7'].Linux.verify_id   == 'RuleAP'
    // }

    // def "ルール定義パース"() {
    //     when:
    //     def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
    //     excel_parser.scan_sheet()
    //     def rule_set = new TestRuleSet(name: 'root')
    //     rule_set.accept(excel_parser)
    //     def test_rules = rule_set.get_all()

    //     then:
    //     test_rules.size() == 2
    //     def result_AP = test_rules['RuleAP'].config.vCenter.NumCpu
    //     result_AP == "x == NumberUtils.toDouble(server_info['NumCpu'])"
    //     test_rules['RuleAP'].config.vCenter.Cluster.size() == 0
    // }

    // def "シート全体パース"() {
    //     when:
    //     def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
    //     excel_parser.scan_sheet()
    //     def test_scenario = new TestScenario(name: 'OS情報採取')
    //     test_scenario.accept(excel_parser)
    //     def test_domains = test_scenario.test_metrics.get_all()
    //     def test_targets = test_scenario.test_targets.get_all()
    //     def test_rules   = test_scenario.test_rules.get_all()

    //     def result_platform_keys = [:]
    //     test_domains.each { domain, test_domain ->
    //         def platform_metrics = test_domain.get_all()
    //         platform_metrics.each { platform, platform_metric ->
    //             result_platform_keys[domain, platform] = platform_metric.count()
    //         }
    //     }
    //     println result_platform_keys

    //     then:
    //     test_targets.size() == 2
    //     test_rules.size() == 2
    //     result_platform_keys.size() > 0
    // }

}
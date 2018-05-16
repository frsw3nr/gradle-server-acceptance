import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "TestTargetTest.Excelパース"

class TestTargetTest extends Specification {
    def test_target

    def setup() {
        test_target = new TestTarget(
            name              : 'ostrich',
            ip                : '192.168.10.1',
            domain            : 'Linux',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            remote_alias      : 'ostrich',
        )
    }

    def "初期化"() {
        when:
        def server_info = test_target.asMap()

        then:
        server_info.size() > 0
    }

    def "Excelパース"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def target_set = new TestTargetSet(name: 'root')
        target_set.accept(excel_parser)
        def test_targets = target_set.get_all()

        when:
        def server_info = test_targets['ostrich']['Linux'].asMap()

        then:
        server_info['verify_id'] == 'RuleAP'
        server_info['NumCpu'] == '4.0'
    }

    def "暗黙的なテンプレートセット"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
        def test_targets = test_scenario.test_targets.get_all()

        when:
        def test_target = test_targets['ostrich']['Linux']
        def server_info = test_target.asMap()

        println "SERVER:${test_target}"
        println "INFOS:${server_info}"

        then:
        server_info['verify_id'] == 'RuleAP'
        server_info['NumCpu'] == '4.0'
        server_info['vCenter']['MemoryGB'] == '2.0'
        server_info['Linux']['filesystem'] == ['/:26.5G', '[SWAP]:3G']
    }

    def "明示的なテンプレートセット"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
        def test_targets = test_scenario.test_targets.get_all()
        def test_target = test_targets['ostrich']['Linux']

        when:
        excel_parser.make_template_link(test_target, test_scenario)
        def server_info = test_target.asMap()

        println "SERVER:${test_target}"
        println "INFOS:${server_info}"

        then:
        server_info['verify_id'] == 'RuleAP'
        server_info['NumCpu'] == '4.0'
        server_info['vCenter']['MemoryGB'] == '2.0'
        server_info['Linux']['filesystem'] == ['/:26.5G', '[SWAP]:3G']
    }

    def "Windowsテンプレートセット"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
        def test_targets = test_scenario.test_targets.get_all()

        when:
        def test_target = test_targets['win2012']['Windows']
        def server_info = test_target.asMap()

        println "SERVER:${test_target}"
        println "INFOS:${server_info}"

        then:
        server_info['verify_id'] == 'RuleAP'
        server_info['vCenter']['MemoryGB'] == '2.0'
        server_info['Windows']['net_ip'] == '192.168.0.14,192.168.0.254,255.255.255.0'
    }
}

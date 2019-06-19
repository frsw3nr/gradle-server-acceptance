import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.Document.ExcelParser
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import jp.co.toshiba.ITInfra.acceptance.Model.TestTargetSet
import jp.co.toshiba.ITInfra.acceptance.Model.TestTemplate
import spock.lang.Specification

// gradle --daemon test --tests "TestTargetTest.Excelパース"
// gradle --daemon test --tests "TestTargetTest.テンプレートの遅延評価2"

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
            template_id       : 'Linux',
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
        println "SERVER_INFO:$server_info"
        server_info.containsKey('numcpu')
    }

    def "テンプレートの遅延評価"() {
        setup:
        def template_config = new ConfigObject()
        template_config.mng_ip = "${test_target.ip}"
        def test_template = new TestTemplate(name: 'Linux', values: template_config)

        when:
        test_target.test_templates['Linux'] = test_template
        def server_info = test_target.asMap()

        then:
        println "SERVER_INFO : $server_info"
        server_info['mng_ip'] == '192.168.10.1'
    }

    def "テンプレートの遅延評価2"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet_t1.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        when:
        def test_targets = test_scenario.test_targets.get_all()
        def server_info = test_targets['ostrich']['Linux'].asMap()

        then:
        println "SERVER_INFO 1 : ${server_info}"
        println "SERVER_INFO 2 : ${server_info['Linux']['net_ip']}"
        server_info['Linux']['net_ip'].size() == 3
        // server_info['mng_ip'] == '192.168.10.1'
    }

    def "暗黙的なテンプレートセット"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
        def test_targets = test_scenario.test_targets.get_all()

        when:
        def test_target = test_targets['win2012']['Windows']
        def server_info = test_target.asMap()

        then:
        server_info.containsKey('numcpu')
        // println server_info['Windows']
        server_info['vCenter'].containsKey('memorygb')
        server_info['Windows']['filesystem'].size() > 0
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

        then:
        server_info.containsKey('numcpu')
        // server_info['vCenter'].containsKey('memorygb')
        // server_info['Linux']['filesystem'].size() > 0
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

        def json = new groovy.json.JsonBuilder()
        json(server_info)
        println json.toPrettyString()

        then:
        server_info['vCenter'].containsKey('memorygb')
        server_info['Windows']['network'].size() > 0
    }
}

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

// gradle --daemon test --tests "TestTargetTest.初期化"

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
        println "ROW:${target_set.get_all().size()}"
        def test_targets = target_set.get_all()

        when:
        println "SERVER:${test_targets['ostrich']}"
        def server_info = test_targets['ostrich'].Linux.asMap()

        then:
        server_info['verify_id'] == 'RuleAP'
        server_info['NumCpu'] == '4.0'
    }

}

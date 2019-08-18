import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import spock.lang.Specification

// gradle --daemon test --tests "ReportMakerTest.実行結果変換"

class ReportMakerTest extends Specification {

    def config_file = 'src/test/resources/config.groovy'
    def excel_file = 'src/test/resources/check_sheet.xlsx'
    def result_dir = 'src/test/resources/json'
    def excel_parser
    def test_scenario
    def report_maker

    def setup() {
        excel_parser = new ExcelParser(excel_file)
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
        def test_result_reader = new TestResultReader(node_dir: result_dir)
        test_result_reader.read_entire_result(test_scenario)

        report_maker = new ReportMaker()

        def test_env = ConfigTestEnvironment.instance
        test_env.read_config(config_file)
        test_env.accept(report_maker)
        println "ITEM_MAP:${report_maker.item_map}"
    }

    def "実行結果変換"() {
        when:
        test_scenario.accept(report_maker)

        then:
        // def json = new groovy.json.JsonBuilder()
        // json(report_maker.redmine_ticket)
        // println json.toPrettyString()
        // ToDo : static ゲッターメソッドに変更
        // RedmineTicket.getTicketDict(report_maker)
        report_maker.redmine_ticket.ticket_dict.size() > 0
        report_maker.redmine_ticket.port_list_dict.size() > 0
        report_maker.report_sheet.rows.size() > 0
        report_maker.report_sheet.cols.size() > 0
        report_maker.report_sheet.results.size() > 0
    }

    def "プラットフォーム検索"() {
        when:
        def test_platform = test_scenario.get_test_platform('ostrich', 'Linux')

        then:
        println test_platform
        1 == 1
    }

    def "Excel 出力"() {
        setup:
        def evidence_maker = new EvidenceMaker()
        test_scenario.accept(evidence_maker)

        when:
        test_scenario.accept(report_maker)
        // def json = new groovy.json.JsonBuilder()
        // json(report_maker.report_sheet)
        // println json.toPrettyString()
        def excel_sheet_maker = new ExcelSheetMaker(
                                    excel_parser: excel_parser,
                                    report_maker: report_maker,
                                    evidence_maker: evidence_maker)
        excel_sheet_maker.output('build/check_sheet.xlsx')

        then:
        1 == 1
    }

}


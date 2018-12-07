import spock.lang.Specification
import static groovy.json.JsonOutput.*
import groovy.json.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.IndexedColors
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "TicketMakerTest.実行結果変換"

/*

Redmine チケット登録処理フロー
-------------------------------

* 実行オプション解析追加。regist[-r] {プロジェクト名}
* Excel 検査レポート解析
    * トラッカー、カスタムフィールド抽出し、TicketSummaryに登録
* ポートリストと、サマリ項目のデータ抽出
* Redmineリポジトリ検索
* 抽出データを順に登録
    * 設備チケットRedmine登録
    * ポートリストRedmine登録

ToDo:Linuxプロトタイピング
---------------------------

* ネットワーク検査シナリオからポートリスト(PortList) を登録できるようにする
* Excel検査レポートからチケット情報を解析して、TIcketSummaryに登録できるようにする
* Document/TIcketMaker作成
   * 検査結果から設備チケット登録データを抽出できるようにする
   * 検査結果からポートリストデータを抽出する
* Ticket/RedmineRegistor作成
   * Redmieからプロジェクトを検索できるようにする
   * TicketMaker抽出データを順にチケット登録
       * 設備チケットの登録
       * ポートリストの登録
*/

class TicketMakerTest extends Specification {

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
        def test_result_reader = new TestResultReader(result_dir: result_dir)
        test_result_reader.read_entire_result(test_scenario)

        report_maker = new TicketMaker()

        def test_env = ConfigTestEnvironment.instance
        test_env.read_config(config_file)
        test_env.accept(report_maker)
    }

    def "実行結果変換"() {
        when:
        test_scenario.accept(report_maker)

        then:
        def json = new groovy.json.JsonBuilder()
        json(report_maker.report_sheet)
        println json.toPrettyString()

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


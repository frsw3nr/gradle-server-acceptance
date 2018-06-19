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

// gradle --daemon test --tests "EvidenceMakerTest.セル配色"

class EvidenceMakerTest extends Specification {

    def config_file = 'src/test/resources/config.groovy'
    def excel_file = 'src/test/resources/check_sheet.xlsx'
    def result_dir = 'src/test/resources/json'
    def excel_parser
    def test_scenario
    def evidence_maker

    def setup() {
        excel_parser = new ExcelParser(excel_file)
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        def test_result_reader = new TestResultReader(result_dir: result_dir)
        test_result_reader. read_entire_result(test_scenario)

        // evidence_maker = new EvidenceMaker(excel_parser: excel_parser)
        evidence_maker = new EvidenceMaker()
    }

    def "実行結果変換"() {
        when:
        test_scenario.accept(evidence_maker)

        then:
        def summary_sheet = evidence_maker.summary_sheets['Linux']
        summary_sheet.rows.size() > 0
        summary_sheet.cols.size() > 0
        // summary_sheet.results['vCenter']['NumCpu']['ostrich'] != null
    }

    def "Excel 出力"() {
        when:
        test_scenario.accept(evidence_maker)
        def excel_sheet_maker = new ExcelSheetMaker(
                                    excel_parser: excel_parser,
                                    evidence_maker: evidence_maker)
        excel_sheet_maker.output('build/check_sheet.xlsx')

        then:
        1 == 1
    }

    // Excel シートに以下の行がある場合に、セルスタイルが効かなくなる問題再現テスト
    //  Y   ntp NTPサーバ名 Linux       NTPサーバの検索
    // null_sheet_ng.xlsx が問題のあるシート

    def "セル配色"() {
        setup:
        def sheet_maker = new ExcelSheetMaker()

        when:
        // def inp = new FileInputStream('src/test/resources/test1.xlsx')
        // def inp = new FileInputStream('src/test/resources/check_sheet.xlsx')
        def inp = new FileInputStream('src/test/resources/check_sheet_ng.xlsx')
        // def inp = new FileInputStream('src/test/resources/null_sheet.xlsx')
        // def inp = new FileInputStream('src/test/resources/null_sheet_ng.xlsx')
        def wb  = WorkbookFactory.create(inp)
        // def sheet = wb.getSheet('CheckSheet(Linux)')
        def sheet = wb.getSheetAt(1)

        def rownum = 0
        ResultCellStyle.values().each { cell_style ->
            // Row row = sheet.getRow(3 + rownum)
            Row row = sheet.createRow(3 + rownum)
            def cell = row.createCell(6)
            cell.setCellValue("${cell_style}")
            sheet_maker.set_test_result_cell_style(cell, cell_style)
            rownum ++
        }

        def fos = new FileOutputStream('build/test.xlsx')
        wb.write(fos)
        fos.close()

        then:
        1 == 1
    }

}


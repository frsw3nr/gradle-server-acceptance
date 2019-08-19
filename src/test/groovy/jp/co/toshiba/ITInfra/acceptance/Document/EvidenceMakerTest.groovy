import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import jp.co.toshiba.ITInfra.acceptance.Model.RunStatus
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import spock.lang.Specification

// gradle --daemon test --tests "EvidenceMakerTest.実行結果変換"

class EvidenceMakerTest extends Specification {

    def config_file = 'src/test/resources/config.groovy'
    def excel_file = 'src/test/resources/check_sheet.xlsx'
    def result_dir = 'src/test/resources/node'
    def excel_parser
    def test_scenario
    def evidence_maker

    def setup() {
        excel_parser = new ExcelParser(excel_file)
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        def test_result_reader = new TestResultReader(node_dir: result_dir)
        test_result_reader. read_entire_result(test_scenario)
        println "TEST_SCENARIO:$test_scenario"
        // evidence_maker = new EvidenceMaker(excel_parser: excel_parser)
        evidence_maker = new EvidenceMaker()
        TestTarget.search(test_scenario).each { it.target_status = RunStatus.FINISH }
    }

    def "実行結果変換"() {
        when:
        test_scenario.accept(evidence_maker)

        then:
        1 == 1
        def summary_sheet = evidence_maker.summary_sheets['Linux']
        println "SUMMARY_SHEET ROWS: ${summary_sheet.rows.size()}"
        summary_sheet.rows.size() > 0
        def col_keys = summary_sheet.cols.keySet().collect { "$it" }
        col_keys == ['ostrich', 'cent7', 'cent7g', 'cent65a'] 
        def result = summary_sheet.results['vCenter']['vm.name']['ostrich']
        result.value == 'ostrich'
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
        // def inp = new FileInputStream('src/test/resources/check_sheet_ng.xlsx')
        // def inp = new FileInputStream('src/test/resources/null_sheet.xlsx')
        // def inp = new FileInputStream('src/test/resources/null_sheet_ng.xlsx')
        def inp = new FileInputStream('./サーバチェックシート.xlsx')

        def wb  = WorkbookFactory.create(inp)

        // def sheet = wb.getSheet('CheckSheet(Linux)')
        def sheet = wb.getSheetAt(1)

        def rownum = 0
        ResultCellStyle.values().each { cell_style ->
            // Row row = sheet.getRow(3 + rownum)
            Row row = sheet.createRow(3 + rownum)
            def cell = row.createCell(6)
            cell.setCellValue("日本語${cell_style}")
            sheet_maker.set_test_result_cell_style(cell, cell_style)
            rownum ++
        }

        def fos = new FileOutputStream('build/test.xlsx')
        wb.write(fos)
        fos.close()

        then:
        1 == 1
    }

    def "グループ化"() {
        setup:
        def sheet_maker = new ExcelSheetMaker()

        when:
        def inp = new FileInputStream('./サーバチェックシート.xlsx')
        def wb  = WorkbookFactory.create(inp)

        // def sheet = wb.getSheet('CheckSheet(Linux)')
        def sheet = wb.getSheetAt(4)

        sheet.groupRow(3, 4);
        sheet.groupRow(6, 9);
        sheet.groupRow(3, 10);
        sheet.groupRow(12, 14);
        sheet.groupRow(16, 17);
        sheet.groupRow(12, 18);
        // sheet.groupRow(17, 19);

        def fos = new FileOutputStream('build/test.xlsx')
        wb.write(fos)
        fos.close()

        then:
        1 == 1
    }
}


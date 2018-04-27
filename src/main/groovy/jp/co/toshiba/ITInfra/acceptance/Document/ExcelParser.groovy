package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.xml.MarkupBuilder
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.IndexedColors
// import com.gh.mygreen.xlsmapper.*
// import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

public enum SpecSheetType {
    check_sheet,
    target_sheet,
    rule_sheet,
    unkown,
}

class SheetDefine {
    int[] header_pos
    int header_row    = 0
    int header_column = 0

    SheetDefine(row, column) {
        this.header_row = row
        this.header_column = column
    }
}

class ExcelParser {
    String excel_file
    def sheet_defines = [:]

    ExcelParser(excel_file) {
        this.excel_file = excel_file
        def sheet_defines = [:]
        sheet_defines[SpecSheetType.check_sheet] = new SheetDefine(3, 0)
    }

    def get_sheet_header(Sheet sheet, start_row = 0, start_column = 0) {
        def headers = []
        def header_row = sheet.getRow(start_row)
        if (header_row) {
            (start_column .. header_row.getLastCellNum()).each { column ->
                headers << "${header_row.getCell(column)}"
            }
        }
        return headers
    }

    SpecSheetType get_spec_sheet_type(Sheet sheet) {
        headers = get_sheet_header(sheet, 3, 0)
        println "HEADERS: ${headers}"
        if (headers[0] == 'Test' && headers[1] == 'ID') {
            return SpecSheetType.check_sheet
        }
    }

    def get_scenario_sheet(Sheet sheet) {
        String sheet_name = sheet.getSheetName()
        ( sheet_name =~ /[\(\[](.+)[\)\]]/ ).each { m0, scenario_name ->
            def headers = get_sheet_header(sheet, 3, 0)
            println "HEADERS: ${headers[0..1]}"
            // 4行目のヘッダが Test,ID で始まる行かチェック
            if (headers[0..1] == ['Test', 'ID']) {
                println "READ: ${scenario_name}"
            }
            // if ("${sheet.getRow(3)?.getCell(0)}" == 'Test' &&
            //     "${sheet.getRow(3)?.getCell(1)}" == 'ID') {
            //     def sheet_csv = readTestResult(sheet)
            //     if (sheet_csv)
            //         csv += sheet_csv
            // }
        }
    }

    def visit_test_scenario(test_scenario) throws IOException {
        println "visit_test_scenario"
        // Excel 本体からシートリストを読み込み、シート名からドメイン識別
        // ドメインテンプレートを生成して登録
        new FileInputStream(this.excel_file).withStream { ins ->
            WorkbookFactory.create(ins).with { wb ->
                Iterator<Sheet> sheets = wb.sheetIterator()
                while(sheets.hasNext()) {
                    Sheet sheet = sheets.next()
                    println this.get_scenario_sheet(sheet)
                }
            }
        }
        ['Linux', 'Windows'].each { domain_name ->
            def test_domain = new TestDomainTemplate(name: domain_name)
            test_domain.accept(this)
            test_scenario.test_domain_templates[domain_name] = test_domain
        }
    }

    def visit_check_sheet(check_sheet) {
        println "visit_check_sheet ${check_sheet}"
        // Excle シート名を読み込み
        // 順に検索してシート登録

    }

    def visit_test_target(test_target) {
        println "visit_test_target"
    }

    def visit_test_domain(test_domain) {
        println "visit_test_domain"
    }

    def visit_test_rule(test_rule) {
        println "visit_test_rule"
    }

}

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

class ExcelParser {
    String excel_file

    ExcelParser(excel_file) {
        this.excel_file = excel_file
    }

    def get_sheet_header(Sheet sheet, start_row = 0, start_column = 0) throws IllegalArgumentException {
        def header_row = sheet.getRow(start_row)
        (start_column .. header_row.getLastCellNum()).each { column ->
            println("COLUMN:${header_row.getCell(column)}")
        }
    }

    def get_scenario_sheet(Sheet sheet) {
        String sheet_name = sheet.getSheetName()
        ( sheet.getSheetName() =~ /[\(\[](.+)[\)\]]/ ).each { m0, scenario_name ->
            println scenario_name
            get_sheet_header(sheet, 3, 0)
            // 4行目のヘッダが Test,ID で始まる行かチェック
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

                    // ( sheet.getSheetName() =~ /[\(\[](.+)[\)\]]/ ).each { m0, test_id ->
                    //     def results = new JsonSlurper().parseText(device_config_json.text)
                    //     def row = 1
                    //     results.each { result ->
                    //         this.device_results[server_name][platform][test_id]["row${row}"] = result
                    //         row ++
                    //     }
                    // }
                    
                    // 4行目が Test,ID で始まる行かチェック
                    // if ("${sheet.getRow(3)?.getCell(0)}" == 'Test' &&
                    //     "${sheet.getRow(3)?.getCell(1)}" == 'ID') {
                    //     def sheet_csv = readTestResult(sheet)
                    //     if (sheet_csv)
                    //         csv += sheet_csv
                    // }
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

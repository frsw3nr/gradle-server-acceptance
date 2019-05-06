package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.transform.AutoClone
import groovy.util.logging.Slf4j
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.IndexedColors
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@AutoClone
@Slf4j
class SheetDesign extends SpecModel {
    String name
    String domain_name
    String result_sheet_name_prefix
    ExcelSheetParser sheet_parser
    def column_sizes = []
    def headers = []
    Sheet sheet
    def sheet_row = [:]
    def sheet_metrics = [:]

    def create(Sheet sheet, String domain_name = null) {
        def current_sheet = this.clone()
        current_sheet.domain_name = domain_name
        current_sheet.sheet = sheet

        return current_sheet
    }

    def get_column_sizes() {
        if (!column_sizes) {
            column_sizes = sheet_parser.get_sheet_column_sizes(this.sheet)
        }
        return column_sizes
    }

    def get_headers() {
        if (!headers) {
            headers = sheet_parser.get_sheet_header(this.sheet)
        }
        return headers
    }

    def get() {
        return sheet_parser.get_sheet_body(this.sheet)
    }
}

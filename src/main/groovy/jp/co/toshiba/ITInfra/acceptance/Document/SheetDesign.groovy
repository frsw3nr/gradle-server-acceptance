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
    ExcelSheetParser sheet_parser
    Sheet sheet

    def create(Sheet sheet, String domain_name = null) {
        def current_sheet = this.clone()
        current_sheet.domain_name = domain_name
        current_sheet.sheet = sheet

        return current_sheet
    }

    def get() {
        return sheet_parser.get_sheet_body(this.sheet)
    }
}

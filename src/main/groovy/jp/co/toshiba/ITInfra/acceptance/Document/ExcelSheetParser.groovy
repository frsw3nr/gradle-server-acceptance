package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.util.ConfigObject
import groovy.util.logging.Slf4j
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.IndexedColors
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
abstract class ExcelSheetParser {
    String sheet_prefix    = ''
    int[] header_pos       = [0, 0]
    String[] header_checks = []

    abstract def get_sheet_body(Sheet sheet)

    public static String getStringFormulaValue(Cell cell) {
        assert cell.getCellType() == Cell.CELL_TYPE_FORMULA;

        Workbook book = cell.getSheet().getWorkbook();
        CreationHelper helper = book.getCreationHelper();
        FormulaEvaluator evaluator = helper.createFormulaEvaluator();
        CellValue value = evaluator.evaluate(cell);
        switch (value.getCellType()) {
        case Cell.CELL_TYPE_STRING:
            return value.getStringValue();
        case Cell.CELL_TYPE_NUMERIC:
            return Double.toString(value.getNumberValue());
        case Cell.CELL_TYPE_BOOLEAN:
            return Boolean.toString(value.getBooleanValue());
        default:
            return null;
        }
    }

    public static String getStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
        case Cell.CELL_TYPE_STRING:
            return cell.getStringCellValue();
        case Cell.CELL_TYPE_NUMERIC:
            return Double.toString(cell.getNumericCellValue());
        case Cell.CELL_TYPE_BOOLEAN:
            return Boolean.toString(cell.getBooleanCellValue());
        case Cell.CELL_TYPE_FORMULA:
            // return cell.getCellFormula();
            return getStringFormulaValue(cell);
        case Cell.CELL_TYPE_BLANK:
            return "";
        default:
            return null;
        }
    }
}

@Slf4j
class ExcelSheetParserHorizontal extends ExcelSheetParser {
    private def get_sheet_header(Sheet sheet) {
        def headers = []
        def header_row = sheet.getRow(header_pos[0])
        if (header_row) {
            (header_pos[1] .. header_row.getLastCellNum()).each { column ->
                headers << "${header_row.getCell(column)}"
            }
        }
        def length = header_checks.size()
        if (headers.size() < length || headers[0..length-1] != header_checks) {
            def msg = "Invalid Sheet header '${sheet.getSheetName()}' : ${headers}"
            throw new IllegalArgumentException(msg)
        }
        return headers
    }

    def get_sheet_body(Sheet sheet) {
        def headers = this.get_sheet_header(sheet)
        def lines = []
        (header_pos[0] + 1 .. sheet?.getLastRowNum()).each { rownum ->
            Row row = sheet.getRow(rownum)
            if (row == null)
                return true
            def line = [:]
            (0 .. headers.size()-1).each { colnum_idx ->
                def colnum = header_pos[1] + colnum_idx
                line[headers[colnum_idx]] = "${row.getCell(colnum)}"
            }
            lines << line
        }
        return lines
    }
}

@Slf4j
class ExcelSheetParserVertical extends ExcelSheetParser {
    private def get_sheet_header(Sheet sheet) {
        def headers = []
        (header_pos[0] .. sheet.getLastRowNum()).each { rownum ->
            Row row = sheet.getRow(rownum)
            if (row == null)
                return true
            headers << "${row.getCell(header_pos[1])}"
        }
        def length = header_checks.size()
        if (length == 0)
            return headers

        if (headers.size() < length || headers[0..length-1] != header_checks) {
            def msg = "Invalid Sheet header '${sheet.getSheetName()}' : ${headers}"
            throw new IllegalArgumentException(msg)
        }
        return headers
    }

    def get_sheet_body(Sheet sheet) {
        def headers = this.get_sheet_header(sheet)
        def lines = [].withDefault{[:]}
        (header_pos[0] .. sheet.getLastRowNum()).each { rownum ->
            def header_id   = rownum - header_pos[0]
            def header_name = headers[header_id]
            Row row = sheet.getRow(rownum)
            // println("ROW:$row")
            if (row == null)
                return true
            (header_pos[1] + 2 .. row.getLastCellNum()).each { colnum ->
                def target_id = colnum - (header_pos[1] + 2)
                // lines[target_id][header_name] = "${row.getCell(colnum)}"
                lines[target_id][header_name] = this.getStringValue(row.getCell(colnum))
            }
        }
        return lines
    }
}
package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.xml.MarkupBuilder
import groovy.util.ConfigObject
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import static groovy.json.JsonOutput.*
import groovy.json.*
import org.apache.commons.lang.math.NumberUtils

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.RegionUtil;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import org.apache.poi.ss.usermodel.WorkbookFactory;

import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

public enum ResultCellStyle {
    NORMAL,
    TITLE,
    OK,
    NG,
    SAME,
    WARNING,
    ERROR,
    NOTEST,
}

        // // short black = IndexedColors.BLACK.getIndex();
        // def black = new XSSFColor(new java.awt.Color(0x00, 0x00, 0x00))

// public enum CellColor {
//     BLACK(0),
//     WHITE(1),
//     LIGHT_GREEN(2),
//     LIGHT_TURQUOISE(3),
//     LEMON_CHIFFON(4),
//     ROSE(5),
//     RED(6),
//     GREY_25_PERCENT(7),
// }

@Slf4j
@ToString(includePackage = false)
class ExcelSheetMaker {
    final device_cell_width   = 5760
    final evidence_cell_width = 11520
    final report_cell_height  = 1190

    ExcelParser excel_parser
    EvidenceMaker evidence_maker
    ReportMaker report_maker
    def colors = [:]

    final COLOR_BLACK           = new XSSFColor(new java.awt.Color(0x00, 0x00, 0x00))
    final COLOR_WHITE           = new XSSFColor(new java.awt.Color(0xFF, 0xFF, 0xFF))
    final COLOR_LIGHT_GREEN     = new XSSFColor(new java.awt.Color(0x80, 0xFF, 0x80))
    final COLOR_LIGHT_TURQUOISE = new XSSFColor(new java.awt.Color(0xCC, 0xFF, 0xFF))
    final COLOR_LEMON_CHIFFON   = new XSSFColor(new java.awt.Color(0xFF, 0xFF, 0x99))
    final COLOR_ROSE            = new XSSFColor(new java.awt.Color(0xFF, 0x99, 0xCC))
    final COLOR_RED             = new XSSFColor(new java.awt.Color(0xFF, 0x00, 0x00))
    final COLOR_GREY_25_PERCENT = new XSSFColor(new java.awt.Color(0xC0, 0xC0, 0xC0))

    // def init_cell_colors() {
    //     colors[CellColor.BLACK]           = new XSSFColor(new java.awt.Color(0x00, 0x00, 0x00));
    //     colors[CellColor.WHITE]           = new XSSFColor(new java.awt.Color(0xFF, 0xFF, 0xFF));
    //     colors[CellColor.LIGHT_GREEN]     = new XSSFColor(new java.awt.Color(0x80, 0xFF, 0x80));
    //     colors[CellColor.LIGHT_TURQUOISE] = new XSSFColor(new java.awt.Color(0xCC, 0xFF, 0xFF));
    //     colors[CellColor.LEMON_CHIFFON]   = new XSSFColor(new java.awt.Color(0xFF, 0xFF, 0x99));
    //     colors[CellColor.ROSE]            = new XSSFColor(new java.awt.Color(0xFF, 0x99, 0xCC));
    //     colors[CellColor.RED]             = new XSSFColor(new java.awt.Color(0xFF, 0x00, 0x00));
    //     colors[CellColor.GREY_25_PERCENT] = new XSSFColor(new java.awt.Color(0xC0, 0xC0, 0xC0));
    // }

    def output(String evidence_excel) {
        long start = System.currentTimeMillis()

        log.info "Write evidence : '${evidence_excel}'"
        // this.init_cell_colors()
        excel_parser.sheet_sources['report'].with { sheet_design ->
            def report_sheet = this.report_maker?.report_sheet
            if (report_sheet) {
                write_sheet_report(report_sheet, sheet_design)
            }
        }
        excel_parser.sheet_sources['error_report'].with { sheet_design ->
            def error_report_sheet = this.report_maker?.error_report_sheet
            if (error_report_sheet) {
                write_sheet_error_report(error_report_sheet, sheet_design)
                // println "ERROR_REPORT_SHEET2: ${error_report_sheet?.rows}"
            }
        }
        def count_summary_sheet_update = 0
        excel_parser.sheet_sources['check_sheet'].each { domain, sheet_design ->
            evidence_maker.summary_sheets[domain].each { summary_sheet ->
                write_sheet_summary(summary_sheet, sheet_design)
                count_summary_sheet_update ++
            }
        }
        log.info "Summary sheet updated : ${count_summary_sheet_update}"

        def count_device_sheet_update = 0
        evidence_maker.device_result_sheets.each { sheet_key, device_result_sheet ->
            def platform = sheet_key[0]
            def metric   = sheet_key[1]
            def device_sheet_name = "${platform}_${metric}"
            write_sheet_device_result(device_sheet_name, device_result_sheet)
            count_device_sheet_update ++
        }
        log.info "Device sheet updated : ${count_device_sheet_update}"

        def fos = new FileOutputStream(evidence_excel)
        excel_parser.workbook.write(fos)
        fos.close()

        long elapse = System.currentTimeMillis() - start
        log.info "Finish excel sheet maker, Elapse : ${elapse} ms"
    }

    def setCellValueWithNumericalTest(Cell cell, def value) {
        // println "CELL SET ${value} ${value instanceof Collection} ${value.getClass()}"
        if ((value in String || value in GString) && NumberUtils.isNumber(value)) {
            cell.setCellValue(NumberUtils.toDouble(value))
        } else {
            cell.setCellValue("${value}")
        }
    }

    // def write_cell_summary(Cell cell, TestResult test_result) {
    def write_cell_summary(Cell cell, TestResult test_result,
                           Boolean disable_cell_style = false) {
        if (disable_cell_style) {
            // if (test_result.value != null) {
            //     cell.setCellValue(test_result.value)
            // }
            def value = test_result?.value ?: " "
            // if (NumberUtils.isDigits(value)) {
            //     cell_result.setCellValue(NumberUtils.toDouble(value))
            // } else {
            //     cell_result.setCellValue(value)
            // }
            // cell.setCellValue(value)
            setCellValueWithNumericalTest(cell, value)
            set_test_result_cell_style(cell, ResultCellStyle.NORMAL)
        } else if (test_result) {
            // if (NumberUtils.isDigits(value)) {
            //     cell_result.setCellValue(NumberUtils.toDouble(value))
            // } else {
            //     cell_result.setCellValue(value)
            // }
            // cell.setCellValue(test_result.value)
            setCellValueWithNumericalTest(cell, test_result.value)
            if (test_result.status == null) {
                set_test_result_cell_style(cell, ResultCellStyle.NOTEST)
            } else if (test_result.status == ResultStatus.NG) {
                set_test_result_cell_style(cell, ResultCellStyle.ERROR)
                cell.setCellValue(test_result.error_msg)
            } else if (test_result.comparision == ResultStatus.MATCH) {
                set_test_result_cell_style(cell, ResultCellStyle.SAME)
                def compare_server = test_result?.compare_server ?: 'target'
                cell.setCellValue("Same as '${compare_server}'")
            } else if (test_result.verify == ResultStatus.OK) {
                set_test_result_cell_style(cell, ResultCellStyle.OK)
            } else if (test_result.verify == ResultStatus.NG) {
                set_test_result_cell_style(cell, ResultCellStyle.NG)
            } else if (test_result.status == ResultStatus.WARNING) {
                set_test_result_cell_style(cell, ResultCellStyle.WARNING)
                cell.setCellValue(test_result.error_msg)
            } else {
                set_test_result_cell_style(cell, ResultCellStyle.NORMAL)
            }
        } else {
            set_test_result_cell_style(cell, ResultCellStyle.NOTEST)
        }
    }

    def write_cell_summary_header(Cell cell, String header) {
        cell.setCellValue(header)
        set_test_result_cell_style(cell, ResultCellStyle.TITLE)
    }

    def write_sheet_header(sheet, int[] position, String[] headers) {
        Row header_row = sheet.getRow(position[0])
        def colnum = 0
        headers.each { header ->
            Cell cell = header_row.createCell(colnum + position[1])
            cell.setCellValue(header)
            set_test_result_cell_style(cell, ResultCellStyle.TITLE)
            colnum ++
        }
    }

    def write_sheet_summary(SheetSummary sheet_summary, SheetDesign sheet_design) {
        def workbook = excel_parser.workbook
        // Font font = workbook.createFont();
        // font.setFontName("ＭＳ Ｐゴシック");
        def result_position = sheet_design.sheet_parser.result_pos
        // println "ROW:$sheet_design.sheet_row"
        def row_style  = workbook.createCellStyle().setWrapText(false)
        sheet_design.sheet.with { sheet ->
            def summary_results = sheet_summary.results

            def targets = sheet_summary.cols.keySet() as String[]
            write_sheet_header(sheet, result_position, targets)

            def rownum = 0
            def last_rownum = 0
            sheet_design.sheet_row.each { platform_metric_key, metric_seq ->
                rownum = metric_seq + result_position[0]
                Row row = sheet.getRow(rownum)
                if (row == null)
                    return
                def platform = platform_metric_key[0]
                def metric   = platform_metric_key[1]
                if (!platform && !metric)
                    return
                last_rownum = rownum
                row.setRowStyle(row_style)
                sheet_summary.cols.each { target, column_index ->
                    def colnum = column_index + result_position[1] - 1
                    sheet.setColumnWidth(colnum, evidence_cell_width)
                    Cell cell = row.createCell(colnum)
                    def test_result = summary_results[platform][metric][target] as TestResult
                    try {
                        write_cell_summary(cell, test_result)
                    } catch (NullPointerException e) {
                        log.debug "Not found row ${platform},${metric}"
                    }
                }
            }
            rownum = last_rownum + 1
            sheet_summary.added_rows.each { platform_metric_key, test_metric ->
                def platform = platform_metric_key[0]
                def metric = platform_metric_key[1]
                Row row = sheet.getRow(rownum)
                if (row == null)
                    row = sheet.createRow(rownum)
                def colnum = 0
                ['', metric, '', platform, '', test_metric.description].each { label ->
                    Cell cell_metric = row.createCell(colnum)
                    write_cell_summary(cell_metric, new TestResult(value: label), true)
                    colnum ++
                }
                row.setRowStyle(row_style)
                sheet_summary.cols.each { target, column_index ->
                    colnum = column_index + result_position[1] - 1
                    sheet.setColumnWidth(colnum, evidence_cell_width)
                    Cell cell = row.createCell(colnum)
                    def test_result = summary_results[platform][metric][target] as TestResult
                    try {
                        write_cell_summary(cell, test_result)
                    } catch (NullPointerException e) {
                        log.debug "Not found row ${platform},${metric}"
                    }
                }
                rownum ++
            }
            // sheet_design.sheet_row.each { platform_metric_key, rownum ->
            //     Row row = sheet.getRow(rownum + result_position[0])
            //     if (row == null)
            //         return
            //     row.setRowStyle(row_style)
            //     sheet_summary.cols.each { target, column_index ->
            //         def colnum = column_index + result_position[1] - 1
            //         sheet.setColumnWidth(colnum, evidence_cell_width)
            //         Cell cell = row.createCell(colnum)
            //         def platform = platform_metric_key[0]
            //         def metric = platform_metric_key[1]
            //         def test_result = summary_results[platform][metric][target] as TestResult
            //         try {
            //             write_cell_summary(cell, test_result)
            //         } catch (NullPointerException e) {
            //             log.debug "Not found row ${platform},${metric}"
            //         }
            //     }
            // }
        }
    }

    def write_sheet_report(SheetSummary sheet_summary, SheetDesign sheet_design) {
        def workbook = excel_parser.workbook
        def result_position = sheet_design.sheet_parser.result_pos
        def row_style  = workbook.createCellStyle().setWrapText(false)
        sheet_design.sheet.with { sheet ->
            def summary_results = sheet_summary.results

            sheet_summary.rows.each { target, row_index ->
                def rownum = row_index + result_position[0] - 1
                Row row = sheet.getRow(rownum)
                if (row == null)
                    row = sheet.createRow(rownum)
                row.setRowStyle(row_style)
                row.setHeight((short)report_cell_height)
                Cell cell_no = row.createCell(0)
                write_cell_summary(cell_no, new TestResult(value: row_index), true)
                def colnum
                sheet_summary.cols.each { metric, column_index ->
                    colnum = column_index + result_position[1]
                    // sheet.setColumnWidth(colnum, evidence_cell_width)
                    // if (metric == 'verifycomment')
                    //     sheet.setColumnWidth(colnum, evidence_cell_width)
                    Cell cell = row.createCell(colnum)
                    def test_result = summary_results[target][metric] as TestResult
                    try {
                        write_cell_summary(cell, test_result, true)
                    } catch (NullPointerException e) {
                        log.debug "Not found row ${target},${metric}"
                    }
                }
                Cell cell_misc = row.createCell(colnum + 1)
                write_cell_summary(cell_misc, new TestResult(value: ''), true)
            }
        }
    }

    def write_sheet_error_report(SheetDeviceResult sheet_error_report, 
                                 SheetDesign sheet_design) {
        def workbook = excel_parser.workbook
        def result_position = sheet_design.sheet_parser.result_pos
        def row_style  = workbook.createCellStyle().setWrapText(false)
        sheet_design.sheet.with { sheet ->
            // def error_reports = sheet_error_report.results
            sheet_error_report.rows.each { target, row_index ->
                def rownum = row_index + result_position[0] - 1
                Row row = sheet.getRow(rownum)
                if (row == null)
                    row = sheet.createRow(rownum)
                row.setRowStyle(row_style)
                Cell cell_no = row.createCell(0)
                write_cell_summary(cell_no, new TestResult(value: row_index), true)
                def colnum = 1
                ['target', 'platform', 'id'].each { metric ->
                    def report_key = target?."${metric}" ?: ''
                    Cell cell = row.createCell(colnum)
                    write_cell_summary(cell, new TestResult(value: report_key), true)
                    colnum ++
                }
                Cell cell = row.createCell(colnum)
                def error_msg = sheet_error_report?.results[target]?.error_msg
                write_cell_summary(cell, new TestResult(value: error_msg), true)
            }
        }
    }

    def write_sheet_device_result(String device_sheet_name,
                                  SheetDeviceResult device_result_sheet) {
        def device_sheet = excel_parser.workbook.createSheet(device_sheet_name)

        def rownum = 0
        // println "DEVICE_RESULT_SHEET:${device_result_sheet}"
        device_sheet.with { sheet ->
            device_result_sheet.results.each { target, test_result ->
                // println "target : ${target}"
                def header = test_result?.devices?.header
                def csv    = test_result?.devices?.csv
                // println "header : ${header}"
                // println "csv : ${csv}"
                if (header == null || csv == null)
                    return
                if (rownum == 0) {
                    Row header_row = sheet.createRow(rownum)
                    def colnum = 0
                    header.addAll(0, ['target'])
                    header.each { header_name ->
                        header_row.createCell(colnum).setCellValue(header_name)
                        sheet.setColumnWidth(colnum, device_cell_width)
                        colnum ++
                    }
                    rownum ++
                }
                csv.each { csv_values ->
                    // println "csv_values : ${csv_values}, ${csv_values.size()}, ${target}"
                    Row row = sheet.createRow(rownum)
                    def colnum = 0
                    // csv_values.addAll(0, [target])
                    row.createCell(colnum).setCellValue(target)
                    colnum ++
                    csv_values.each { csv_value ->
                        // println "CSV_VALUE:${rownum},${colnum},${csv_value}"
                        // row.createCell(colnum).setCellValue("${csv_value}")
                        setCellValueWithNumericalTest(row.createCell(colnum), csv_value)
                        colnum ++
                    }
                    rownum ++
                }
            }
        }
    }

    def set_test_result_cell_style(cell, ResultCellStyle result_cell_type) {
        BorderStyle thin = BorderStyle.THIN;
        // short black = IndexedColors.BLACK.getIndex();
        // def black = colors[CellColor.BLACK.value] // new XSSFColor(new java.awt.Color(0x00, 0x00, 0x00))
        def black = COLOR_BLACK
        def wb = cell.getRow().getSheet().getWorkbook();
        CellStyle style = wb.createCellStyle();

        // Set Boder line
        style.setBorderRight(thin);
        style.setRightBorderColor(black);
        style.setBorderBottom(thin);
        style.setBottomBorderColor(black);
        style.setBorderTop(thin);
        style.setTopBorderColor(black);

        // Set Center vertical
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Set Text font and Foreground color
        switch (result_cell_type) {
            // case ResultCellStyle.NORMAL :
            //     style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            //     style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            //     break

            case ResultCellStyle.TITLE :
                // style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                // style.setFillForegroundColor(new XSSFColor(new java.awt.Color(0xFF, 0xFF, 0xFF)));
                style.setFillForegroundColor(COLOR_WHITE)

                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                def font = wb.createFont();
                font.setBold(true);
                // font.setColor(IndexedColors.BLACK.getIndex());
                font.setColor(black);

                style.setFont(font);
                break

            case ResultCellStyle.OK :
                // style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                style.setFillForegroundColor(COLOR_LIGHT_GREEN)
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                def font = wb.createFont();
                font.setBold(true);
                // font.setColor(IndexedColors.BLACK.getIndex());
                font.setColor(black);
                style.setFont(font);
                break

            case ResultCellStyle.SAME :
                // style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
                style.setFillForegroundColor(COLOR_LIGHT_TURQUOISE)
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                def font = wb.createFont();
                // font.setColor(IndexedColors.BLACK.getIndex());
                font.setColor(black);
                style.setFont(font);
                break

            case ResultCellStyle.WARNING :
                // style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
                style.setFillForegroundColor(COLOR_LEMON_CHIFFON)
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                def font = wb.createFont();
                // font.setColor(IndexedColors.BLACK.getIndex());
                font.setColor(black);
                style.setFont(font);
                break

            case ResultCellStyle.NG :
                // style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
                style.setFillForegroundColor(COLOR_ROSE)
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                def font = wb.createFont();
                font.setBold(true);
                // font.setColor(IndexedColors.BLACK.getIndex());
                font.setColor(black);
                style.setFont(font);
                break

            case ResultCellStyle.ERROR :
                def font = wb.createFont();
                // font.setColor(IndexedColors.RED.getIndex());
                style.setFillForegroundColor(COLOR_RED)
                def font_size = font.getFontHeightInPoints()
                font.setFontHeightInPoints((short)(font_size - 2))
                style.setFont(font);
                break

            case ResultCellStyle.NOTEST :
                // style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                style.setFillForegroundColor(COLOR_GREY_25_PERCENT)
                // style.setFillForegroundColor ( HSSFColor.BLACK.index );
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                // style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                break

            default :
                break
        }
        style.setWrapText(true);
        cell.setCellStyle(style);
    }

}

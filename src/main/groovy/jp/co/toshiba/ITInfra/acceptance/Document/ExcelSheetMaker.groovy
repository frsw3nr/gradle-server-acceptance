package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.xml.MarkupBuilder
import groovy.util.ConfigObject
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import static groovy.json.JsonOutput.*
import groovy.json.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.IndexedColors
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

@Slf4j
@ToString(includePackage = false)
class ExcelSheetMaker {
    final device_cell_width   = 5760
    final evidence_cell_width = 11520

    ExcelParser excel_parser
    EvidenceMaker evidence_maker

    def output(String evidence_excel) {
        long start = System.currentTimeMillis()

        log.info "Write evidence : '${evidence_excel}'"
        excel_parser.sheet_sources['check_sheet'].each { domain, sheet_design ->
            evidence_maker.summary_sheets[domain].each { summary_sheet ->
                write_sheet_summary(summary_sheet, sheet_design)
            }
        }
        log.info "Summary sheet updated"
        evidence_maker.device_result_sheets.each { sheet_key, device_result_sheet ->
            def platform = sheet_key[0]
            def metric   = sheet_key[1]
            def device_sheet_name = "${platform}_${metric}"
            write_sheet_device_result(device_sheet_name, device_result_sheet)
            log.info "Device sheet updated : '${device_sheet_name}'"
        }

        def fos = new FileOutputStream(evidence_excel)
        excel_parser.workbook.write(fos)
        fos.close()

        long elapse = System.currentTimeMillis() - start
        log.info "Finish excel sheet maker, Elapse : ${elapse} ms"
    }

    // def write_cell_summary(Cell cell, TestResult test_result) {
    def write_cell_summary(Cell cell, TestResult test_result) {
        if (test_result) {
            cell.setCellValue(test_result.value)
            if (test_result.status == null) {
                set_test_result_cell_style(cell, ResultCellStyle.NOTEST)
            } else if (test_result.status == ResultStatus.NG) {
                set_test_result_cell_style(cell, ResultCellStyle.ERROR)
                cell.setCellValue(test_result.error_msg)
            } else if (test_result.status == ResultStatus.MATCH) {
                set_test_result_cell_style(cell, ResultCellStyle.SAME)
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
        def result_position = sheet_design.sheet_parser.result_pos
        // println "ROW:$sheet_design.sheet_row"
        def row_style  = workbook.createCellStyle().setWrapText(false)
        sheet_design.sheet.with { sheet ->
            def summary_results = sheet_summary.results

            def targets = sheet_summary.cols.keySet() as String[]
            write_sheet_header(sheet, result_position, targets)

            sheet_design.sheet_row.each { platform_metric_key, rownum ->
                Row row = sheet.getRow(rownum + result_position[0])
                if (row == null)
                    return
                row.setRowStyle(row_style)
                sheet_summary.cols.each { target, column_index ->
                    def colnum = column_index + result_position[1] - 1
                    sheet.setColumnWidth(colnum, evidence_cell_width)
                    Cell cell = row.createCell(colnum)
                    def platform = platform_metric_key[0]
                    def metric = platform_metric_key[1]
                    def test_result = summary_results[platform][metric][target] as TestResult
                    try {
                        write_cell_summary(cell, test_result)
                    } catch (NullPointerException e) {
                        log.debug "Not found row ${platform},${metric}"
                    }
                }
            }
        }
    }

    def write_sheet_device_result(String device_sheet_name,
                                  SheetDeviceResult device_result_sheet) {
        def device_sheet = excel_parser.workbook.createSheet(device_sheet_name)

        def rownum = 0
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
                        row.createCell(colnum).setCellValue(csv_value)
                        colnum ++
                    }
                    rownum ++
                }
            }
        }
    }

    def set_test_result_cell_style(cell, ResultCellStyle result_cell_type) {
        BorderStyle thin = BorderStyle.THIN;
        short black = IndexedColors.BLACK.getIndex();
        def wb = cell.getRow().getSheet().getWorkbook();
        CellStyle style = wb.createCellStyle();

        // Set Boder line
        style.setBorderRight(thin);
        style.setRightBorderColor(black);
        style.setBorderBottom(thin);
        style.setBottomBorderColor(black);
        style.setBorderTop(thin);
        style.setTopBorderColor(black);

        // Set Text font and Foreground color
        switch (result_cell_type) {
            case ResultCellStyle.NORMAL :
                style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                break

            case ResultCellStyle.TITLE :
                def font = wb.createFont();
                font.setBold(true);
                font.setColor(IndexedColors.BLACK.getIndex());
                style.setFont(font);
                break

            case ResultCellStyle.OK :
                style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                def font = wb.createFont();
                font.setBold(true);
                font.setColor(IndexedColors.BLACK.getIndex());
                style.setFont(font);
                break

            case ResultCellStyle.SAME :
                style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                def font = wb.createFont();
                font.setColor(IndexedColors.ROYAL_BLUE.getIndex());
                style.setFont(font);
                break

            case ResultCellStyle.WARNING :
                style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                def font = wb.createFont();
                font.setColor(IndexedColors.CORAL.getIndex());
                style.setFont(font);
                break

            case ResultCellStyle.NG :
                style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                def font = wb.createFont();
                font.setBold(true);
                font.setColor(IndexedColors.BLACK.getIndex());
                style.setFont(font);
                break

            case ResultCellStyle.ERROR :
                def font = wb.createFont();
                font.setColor(IndexedColors.RED.getIndex());
                def font_size = font.getFontHeightInPoints()
                println "font_size:$font_size"
                // font.setFontHeightInPoints((short)10)
                font.setFontHeightInPoints((short)(font_size - 2))
                style.setFont(font);
                break

            case ResultCellStyle.NOTEST :
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                break

            default :
                break
        }
        style.setWrapText(true);
        cell.setCellStyle(style);
    }

}

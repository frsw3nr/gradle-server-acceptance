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
    def cell_styles = [:]

    final COLOR_BLACK           = new XSSFColor(new java.awt.Color(0x00, 0x00, 0x00))
    final COLOR_WHITE           = new XSSFColor(new java.awt.Color(0xFF, 0xFF, 0xFF))
    final COLOR_LIGHT_GREEN     = new XSSFColor(new java.awt.Color(0x80, 0xFF, 0x80))
    final COLOR_LIGHT_TURQUOISE = new XSSFColor(new java.awt.Color(0xCC, 0xFF, 0xFF))
    final COLOR_LEMON_CHIFFON   = new XSSFColor(new java.awt.Color(0xFF, 0xFF, 0x99))
    final COLOR_ROSE            = new XSSFColor(new java.awt.Color(0xFF, 0x99, 0xCC))
    final COLOR_RED             = new XSSFColor(new java.awt.Color(0xFF, 0x00, 0x00))
    final COLOR_GREY_25_PERCENT = new XSSFColor(new java.awt.Color(0xC0, 0xC0, 0xC0))

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
            }
        }
        def count_summary_sheet_update = 0
        excel_parser.sheet_sources['check_sheet'].each { domain, sheet_design ->
            evidence_maker.summary_sheets[domain].each { summary_sheet ->
                def sheet = create_sheet_summary(summary_sheet, sheet_design)
                excel_parser.workbook.setSheetOrder(sheet.getSheetName(), 3 + count_summary_sheet_update)
                count_summary_sheet_update ++
            }
            // evidence_maker.summary_sheets[domain].each { summary_sheet ->
            //     write_sheet_summary(summary_sheet, sheet_design)
            //     count_summary_sheet_update ++
            // }
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
            cell.setCellValue("${value ?: ''}")
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
        if (header_row == null)
            header_row = sheet.createRow(position[0])

        def colnum = 0
        headers.each { header ->
            Cell cell = header_row.createCell(colnum + position[1])
            cell.setCellValue(header)
            set_test_result_cell_style(cell, ResultCellStyle.TITLE)
            colnum ++
        }
    }

    def write_sheet_metric_definition(sheet, Row row, TestMetric sheet_metric) {
        def values = sheet_metric.get_definitions()
        // println "VALUES:$values"
        def colnum = 0
        values.each { value ->
            Cell cell = row.createCell(colnum)
            // cell.setEncoding(HSSFCell.ENCODING_UTF_16)
            cell.setCellValue(value)
            set_test_result_cell_style(cell, ResultCellStyle.NORMAL)
            colnum ++
        }
    }

    // def write_sheet_summary(SheetSummary sheet_summary, SheetDesign sheet_design) {
    //     def workbook = excel_parser.workbook
    //     // Font font = workbook.createFont();
    //     // font.setFontName("ＭＳ Ｐゴシック");
    //     def result_position = sheet_design.sheet_parser.result_pos
    //     // println "ROW:$sheet_design.sheet_row"
    //     def row_style  = workbook.createCellStyle().setWrapText(false)
    //     sheet_design.sheet.with { sheet ->
    //         def summary_results = sheet_summary.results

    //         def targets = sheet_summary.cols.keySet() as String[]
    //         write_sheet_header(sheet, result_position, targets)

    //         def rownum = 0
    //         def last_rownum = 0
    //         sheet_design.sheet_row.each { platform_metric_key, metric_seq ->
    //             rownum = metric_seq + result_position[0]
    //             Row row = sheet.getRow(rownum)
    //             if (row == null)
    //                 return
    //             def platform = platform_metric_key[0]
    //             def metric   = platform_metric_key[1]
    //             if (!platform && !metric)
    //                 return
    //             last_rownum = rownum
    //             def added_metrics = sheet_summary.added_rows[platform, metric]
    //             if (added_metrics) {
    //                 println "ADD NEW METRIC: ${metric}, ${added_metrics}"
    //                 added_metrics.each { platform_metric_key2, test_metric ->
    //                     def platform2 = platform_metric_key2[0]
    //                     def metric2 = platform_metric_key2[1]
    //                     row = sheet.getRow(rownum)
    //                     // Row row = sheet.getRow(rownum)
    //                     if (row == null)
    //                         row = sheet.createRow(rownum)
    //                     def colnum = 0
    //                     ['', '', '', metric2, platform2, '', test_metric.description].each { label ->
    //                         Cell cell_metric = row.createCell(colnum)
    //                         write_cell_summary(cell_metric, new TestResult(value: label), true)
    //                         colnum ++
    //                     }
    //                     row.setRowStyle(row_style)
    //                     sheet_summary.cols.each { target, column_index ->
    //                         colnum = column_index + result_position[1] - 1
    //                         sheet.setColumnWidth(colnum, evidence_cell_width)
    //                         Cell cell = row.createCell(colnum)
    //                         def test_result = summary_results[platform2][metric2][target] as TestResult
    //                         println "ADDED SHEET2:$metric,$rownum, $target, $column_index, ${test_result}"
    //                         try {
    //                             write_cell_summary(cell, test_result)
    //                         } catch (NullPointerException e) {
    //                             log.debug "Not found row ${platform2},${metric2}"
    //                         }
    //                     }
    //                     rownum ++
    //                 }
    //             }
    //             row.setRowStyle(row_style)
    //             println "SHEET:$metric,$rownum"
    //             sheet_summary.cols.each { target, column_index ->
    //                 def colnum = column_index + result_position[1] - 1
    //                 sheet.setColumnWidth(colnum, evidence_cell_width)
    //                 Cell cell = row.createCell(colnum)
    //                 def test_result = summary_results[platform][metric][target] as TestResult
    //                 println "SHEET2:$metric,$rownum, $target, $column_index, ${test_result}"
    //                 try {
    //                     write_cell_summary(cell, test_result)
    //                 } catch (NullPointerException e) {
    //                     log.debug "Not found row ${platform},${metric}"
    //                 }
    //             }
    //         }
    //         rownum = last_rownum + 1
    //         // sheet_summary.added_rows.each { platform_metric_key, test_metric ->
    //         //     def platform = platform_metric_key[0]
    //         //     def metric = platform_metric_key[1]
    //         //     Row row = sheet.getRow(rownum)
    //         //     if (row == null)
    //         //         row = sheet.createRow(rownum)
    //         //     def colnum = 0
    //         //     ['', metric, '', platform, '', test_metric.description].each { label ->
    //         //         Cell cell_metric = row.createCell(colnum)
    //         //         write_cell_summary(cell_metric, new TestResult(value: label), true)
    //         //         colnum ++
    //         //     }
    //         //     row.setRowStyle(row_style)
    //         //     sheet_summary.cols.each { target, column_index ->
    //         //         colnum = column_index + result_position[1] - 1
    //         //         sheet.setColumnWidth(colnum, evidence_cell_width)
    //         //         Cell cell = row.createCell(colnum)
    //         //         def test_result = summary_results[platform][metric][target] as TestResult
    //         //         try {
    //         //             write_cell_summary(cell, test_result)
    //         //         } catch (NullPointerException e) {
    //         //             log.debug "Not found row ${platform},${metric}"
    //         //         }
    //         //     }
    //         //     rownum ++
    //         // }
    //         // sheet_design.sheet_row.each { platform_metric_key, rownum ->
    //         //     Row row = sheet.getRow(rownum + result_position[0])
    //         //     if (row == null)
    //         //         return
    //         //     row.setRowStyle(row_style)
    //         //     sheet_summary.cols.each { target, column_index ->
    //         //         def colnum = column_index + result_position[1] - 1
    //         //         sheet.setColumnWidth(colnum, evidence_cell_width)
    //         //         Cell cell = row.createCell(colnum)
    //         //         def platform = platform_metric_key[0]
    //         //         def metric = platform_metric_key[1]
    //         //         def test_result = summary_results[platform][metric][target] as TestResult
    //         //         try {
    //         //             write_cell_summary(cell, test_result)
    //         //         } catch (NullPointerException e) {
    //         //             log.debug "Not found row ${platform},${metric}"
    //         //         }
    //         //     }
    //         // }
    //     }
    // }

    def write_sheet_summary_tag_group(sheet, SheetSummary sheet_summary, SheetDesign sheet_design) {
        def result_position = sheet_design.sheet_parser.result_pos
        def colnum = result_position[1]
        def tag_temp = null
        def group_positions = []
        def targets = sheet_summary.cols.keySet() as String[]
        targets.each { target ->
            def tag = sheet_summary.tags[target]
            if (tag) {
                if (tag_temp != tag) {
                    tag_temp = tag
                    group_positions << colnum
                }
            } else {
                if (tag_temp) {
                    tag_temp = null
                    group_positions << colnum
                }
            }
            colnum ++
        }
        def group_position_strat
        group_positions.each { group_position ->
            if (group_position_strat) {
                def group_position_end = group_position - 1
                if ((group_position_end - group_position_strat) > 1) {
                    sheet.groupColumn(group_position_strat, group_position_end)
                    // sheet.setRowGroupCollapsed(group_position_strat, true)
                }
            }
            group_position_strat = group_position
        }
        println "TAG POSITION : ${group_positions}"
    }

    // Write Summary sheet Header
    def write_sheet_summary_header(sheet, SheetSummary sheet_summary, SheetDesign sheet_design) {
        def result_position = sheet_design.sheet_parser.result_pos
        def header_pos = sheet_design.sheet_parser.header_pos

        def headers = sheet_design.get_headers() as String[]
        write_sheet_header(sheet, header_pos, headers)
        
        def column_sizes = sheet_design.get_column_sizes()
        def colnum = 0
        column_sizes.each { column_size ->
            sheet.setColumnWidth(colnum, column_size)
            colnum ++
        }

        def targets = sheet_summary.cols.keySet() as String[]
        write_sheet_summary_tag_group(sheet, sheet_summary, sheet_design)
        write_sheet_header(sheet, result_position, targets)
        // colnum = result_position[1]
        // def tag_temp = null
        // def group_positions = []
        // targets.each { target ->
        //     sheet.setColumnWidth(colnum, evidence_cell_width)
        //     def tag = sheet_summary.tags[target]
        //     if (tag) {
        //         if (tag_temp != tag) {
        //             tag_temp = tag
        //             group_positions << colnum
        //         }
        //     } else {
        //         if (tag_temp) {
        //             tag_temp = null
        //             group_positions << colnum
        //         }
        //     }
        //     colnum ++
        // }
        // def group_position_strat
        // group_positions.each { group_position ->
        //     if (group_position_strat) {
        //         def group_position_end = group_position - 1
        //         if ((group_position_end - group_position_strat) > 1) {
        //             sheet.groupColumn(group_position_strat, group_position_end + 1)
        //             // sheet.setRowGroupCollapsed(group_position_strat, true)
        //         }
        //     }
        //     group_position_strat = group_position
        // }
        // println "TAG POSITION : ${group_positions}"
    }

    def write_sheet_summary_values_line(Row row, List platform_metric,
                                        SheetSummary sheet_summary,
                                        SheetDesign sheet_design) {
        def platform = platform_metric[0]
        def metric   = platform_metric[1]

        def result_position = sheet_design.sheet_parser.result_pos
        def summary_results = sheet_summary.results
        sheet_summary.cols.each { target, column_index ->
            def colnum = column_index + result_position[1] - 1
            Cell cell = row.createCell(colnum)
            def test_result = summary_results[platform][metric][target] as TestResult
            // println "SHEET2:$metric,$target, $column_index, ${test_result}"
            try {
                write_cell_summary(cell, test_result)
            } catch (NullPointerException e) {
                log.debug "Not found row ${platform},${metric}"
            }
        }
    }

    // Write Summary group
    def write_sheet_summary_group(sheet, List categorys, SheetDesign sheet_design) {
        def result_position = sheet_design.sheet_parser.result_pos
        def category_temp
        def group_positions = []
        def rownum = result_position[0] + 1
        categorys.each { category ->
            if (category_temp != category) {
                category_temp = category
                group_positions << rownum
            }
            rownum ++
        }
        group_positions << rownum
        def group_position_strat
        group_positions.each { group_position ->
            if (group_position_strat) {
                def group_position_end = group_position - 2
                if ((group_position - group_position_strat) > 0) {
                    sheet.groupRow(group_position_strat, group_position_end)
                    // sheet.setRowGroupCollapsed(group_position_strat, true)
                }
            }
            group_position_strat = group_position
        }
    }

    def get_sheet_summary_name(SheetSummary sheet_summary, SheetDesign sheet_design) {
        return "${sheet_design.result_sheet_name_prefix}(${sheet_summary.sheet_name})"
    }

    def create_sheet_summary(SheetSummary sheet_summary, SheetDesign sheet_design) {
        long start  = System.currentTimeMillis()
        def workbook = excel_parser.workbook
        // Font font = workbook.createFont();
        // font.setFontName("ＭＳ Ｐゴシック");
        def row_style  = workbook.createCellStyle().setWrapText(false)

        def sheet_name = get_sheet_summary_name(sheet_summary, sheet_design)
        def sheet = workbook.createSheet(sheet_name)
        sheet.groupColumn(3, 5)
        println "TAGS:${sheet_summary.tags}"
        write_sheet_summary_header(sheet, sheet_summary, sheet_design)

        def rownum = sheet_design.sheet_parser.result_pos[0] + 1
        def last_rownum = 0

        def categorys = []
        long elapse_write_definition = 0
        long elapse_write_cell       = 0
        sheet_design.sheet_metrics.each { platform_metric, test_metric ->
            def added_metrics = sheet_summary.added_rows[platform_metric]
            if (added_metrics) {
                // println "ADD NEW METRIC: ${platform_metric}, ${added_metrics}"
                added_metrics.each { platform_metric2, test_metric2 ->
                    categorys << test_metric2.category
                    Row row = sheet.getRow(rownum)
                    if (row == null)
                        row = sheet.createRow(rownum)
                    write_sheet_metric_definition(sheet, row, test_metric2)
                    write_sheet_summary_values_line(row, platform_metric2, sheet_summary, sheet_design)
                    rownum ++
                }
            }

            Row row = sheet.getRow(rownum)
            if (row == null)
                row = sheet.createRow(rownum)

            categorys << test_metric.category
            def sheet_metric  = sheet_design.sheet_metrics[platform_metric]
            // println "ROWNUM: $rownum, PLATFORM_METRIC: $platform_metric, SHEET_METRIC: $sheet_metric"
            if (!sheet_metric)
                return

            write_sheet_metric_definition(sheet, row, sheet_metric)
            write_sheet_summary_values_line(row, platform_metric, sheet_summary, sheet_design)
            rownum ++
            last_rownum = rownum
        }

        write_sheet_summary_group(sheet, categorys, sheet_design)
        long elapse = System.currentTimeMillis() - start
        log.debug "Write summary sheet ${sheet_name} : ${elapse} ms"

        return sheet
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
                def header_source = test_result?.devices?.header
                def csv    = test_result?.devices?.csv
                // println "header_source : ${header_source}"
                // println "csv : ${csv}"
                if (header_source == null || csv == null)
                    return
                def header = header_source.clone()
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

    def create_cell_style(Cell cell, ResultCellStyle result_cell_type) {
        BorderStyle thin = BorderStyle.THIN;
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

        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.0"));

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
                font.setFontName("ＭＳ Ｐゴシック")
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
                font.setFontName("ＭＳ Ｐゴシック")
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
                font.setFontName("ＭＳ Ｐゴシック")
                // font.setColor(IndexedColors.BLACK.getIndex());
                font.setColor(black);
                style.setFont(font);
                break

            case ResultCellStyle.WARNING :
                // style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
                style.setFillForegroundColor(COLOR_LEMON_CHIFFON)
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                def font = wb.createFont();
                font.setFontName("ＭＳ Ｐゴシック")
                // font.setColor(IndexedColors.BLACK.getIndex());
                font.setColor(black);
                style.setFont(font);
                break

            case ResultCellStyle.NG :
                // style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
                style.setFillForegroundColor(COLOR_ROSE)
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                def font = wb.createFont();
                font.setFontName("ＭＳ Ｐゴシック")
                font.setBold(true);
                // font.setColor(IndexedColors.BLACK.getIndex());
                font.setColor(black);
                style.setFont(font);
                break

            case ResultCellStyle.ERROR :
                def font = wb.createFont();
                font.setFontName("ＭＳ Ｐゴシック")
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
        return style;
    }

    def set_test_result_cell_style(cell, ResultCellStyle result_cell_type) {
        def style = cell_styles[result_cell_type] 
        if (!style) {
            style = create_cell_style(cell, result_cell_type)
            cell_styles[result_cell_type] = style
        }
        cell.setCellStyle(style)
    }

}

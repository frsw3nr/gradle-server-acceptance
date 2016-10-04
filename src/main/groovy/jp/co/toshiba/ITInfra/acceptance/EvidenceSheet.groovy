package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils.*
import groovy.transform.ToString
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import static groovy.json.JsonOutput.*

@Slf4j
class EvidenceSheet {

    String evidence_source
    String evidence_target
    String sheet_name_server
    String sheet_name_specs

    def platforms
    def domains

    TestServer test_servers
    TestItem   test_specs

    EvidenceSheet(String config_file = 'config/config.groovy') {
        def config = Config.instance.read(config_file)['evidence']

        evidence_source   = config['source'] ?: './check_sheet.xlsx'
        evidence_target   = config['target'] ?: './build/check_sheet.xlsx'
        sheet_name_server = config['sheet_name_server'] ?: 'Target'
        sheet_name_specs  = config['sheet_name_specs'] ?: [
                                'Linux' : 'Check(Linux)',
                                'Windows' : 'Check(Windows)'
                            ]
        log.debug("Open evidence source ${evidence_source}")

        println config
    }

    // エクセル検査結果列のセルフォーマット
    // 検査結果列に対して罫線を追加して、行幅をオートスケールに設定
    private static CellStyle createBorderedStyle(Workbook wb) {
        BorderStyle thin = BorderStyle.THIN;
        short black = IndexedColors.BLACK.getIndex();

        CellStyle style = wb.createCellStyle();
        style.setWrapText(true);
        style.setBorderRight(thin);
        style.setRightBorderColor(black);
        style.setBorderBottom(thin);
        style.setBottomBorderColor(black);
        style.setBorderTop(thin);
        style.setTopBorderColor(black);
        return style;
    }

    def readSheet() {
        log.info("read test spec from ${evidence_source}")

        def items = new FileInputStream(evidence_source).withStream { ins ->
            WorkbookFactory.create(ins).with { workbook ->
                // 検査対象サーバリスト取得
                workbook.getSheet(sheet_name_server).with { sheet ->
                (2 .. sheet.getLastRowNum()).each { rownum ->
                    Row row = sheet.getRow(rownum)
                        VmConfigs.push([
                            test_server : row.getCell(2).getStringCellValue(),
                            ip          : row.getCell(3).getStringCellValue(),
                            os          : row.getCell(4).getStringCellValue(),
                            vcenter_id  : row.getCell(5).getStringCellValue(),
                            vm          : row.getCell(6).getStringCellValue(),
                        ])
                    }
                }
                // 検査仕様リスト取得
                sheet_name_specs.each { os, test_spec_sheet ->
                    workbook.getSheet(test_spec_sheet).with { sheet ->
                        (4 .. sheet.getLastRowNum()).each { rownum ->
                            Row row = sheet.getRow(rownum)
                            def yes_no      = row.getCell(0).getStringCellValue()
                            def test_id     = row.getCell(1).getStringCellValue()
                            def test_domain = row.getCell(3).getStringCellValue()
                            if (test_id && test_domain && yes_no.toUpperCase() == "Y") {
                                TestSpecs[os][test_domain][test_id] = 1
                            }
                        }
                    }
                }
            }
        }

    }

    def readSpecSheet() {
    }

    def updateTestResult(String platform, String test_server, int sequence, TestItem[] test_specs) {
        log.info("update test results to ${evidence_target} : platform = ${platform}, server = ${test_server}")

        def inp = new FileInputStream("build/${TestSpecFile}")
        def wb  = WorkbookFactory.create(inp)
        def sheetHosts = wb.getSheet(SheetTestSpecs[os])
        def cell_style = createBorderedStyle(wb)

        // 検査結果列の列幅 30 文字に設定 (in units of 1/256th of a character width)
        def column = 5 + sequence
        sheetHosts.setColumnWidth(column, 7680)

        // ヘッダーに検査対象サーバ名を登録
        def title_cell = sheetHosts.getRow(3).createCell(column)
        title_cell.setCellValue(test_server)
        title_cell.setCellStyle(cell_style)

        // 検査結果列を順に登録
        sheetHosts.with { sheet ->
            (4 .. sheet.getLastRowNum()).each { rownum ->
                Row row = sheet.getRow(rownum)
                def row_style  = wb.createCellStyle().setWrapText(true);
                row.setRowStyle(row_style);

                // 検査ID列から検査IDを取得
                Cell cell_test_id = row.getCell(1)
                Cell cell_result  = row.createCell(column)
                if (cell_test_id) {
                    def test_id = cell_test_id.getStringCellValue()

                    // 検査IDをキーに検査結果を登録
                    if (test_id) {
                        if (results.containsKey(test_id)) {
                            cell_result.setCellValue(results[test_id])
                        } else {
                            cell_result.setCellValue('[NoRun]')
                        }
                    }
                }
                cell_result.setCellStyle(cell_style)
            }
        }
        def fos = new FileOutputStream("build/${TestSpecFile}")
        wb.write(fos)
        fos.close()
    }
}

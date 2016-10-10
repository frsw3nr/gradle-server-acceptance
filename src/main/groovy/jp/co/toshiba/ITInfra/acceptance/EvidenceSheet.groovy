package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils.*
import groovy.transform.ToString
import static groovy.json.JsonOutput.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook

@Slf4j
class EvidenceSheet {

    String evidence_source
    String evidence_target
    String sheet_name_server
    String staging_dir
    def sheet_name_specs

    def test_platforms
    def test_domains

    def test_servers
    def test_specs

    EvidenceSheet(String config_file = 'config/config.groovy') {
        def config = Config.instance.read(config_file)['evidence']

        log.debug("initialize evidence")
        evidence_source   = config['source'] ?: './check_sheet.xlsx'
        evidence_target   = config['target'] ?: './build/check_sheet.xlsx'
        sheet_name_server = config['sheet_name_server'] ?: 'Target'
        staging_dir       = config['staging_dir'] ?: './build/log'
        sheet_name_specs  = config['sheet_name_specs'] ?: [
                                'Linux'   : 'CheckSheet(Linux)',
                                'Windows' : 'CheckSheet(Windows)'
                            ]

        def date = new Date().format("yyyyMMdd_hhmmss")
        evidence_target = evidence_target.replaceAll(/<date>/, date)
        staging_dir     = staging_dir.replaceAll(/<date>/, date)
        test_platforms = [:]
        test_domains   = [:]
        test_servers   = []
        test_specs     = [:].withDefault{[:].withDefault{[:]}}
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

    def readSheetServer(Sheet sheet_server) throws IOException {
        sheet_server.with { sheet ->
            (2 .. sheet.getLastRowNum()).each { rownum ->
                Row row = sheet.getRow(rownum)
                def test_server = new TargetServer(
                    server_name   : row.getCell(2).getStringCellValue(),
                    ip            : row.getCell(3).getStringCellValue(),
                    platform      : row.getCell(4).getStringCellValue(),
                    os_account_id : row.getCell(5).getStringCellValue(),
                    vcenter_id    : row.getCell(6).getStringCellValue(),
                    vm            : row.getCell(7).getStringCellValue(),
                )
                def null_checks = [:]
                ['server_name', 'ip', 'platform', 'os_account_id'].each {
                    def value = test_server[it]
                    if ( value == null || value.length() == 0 )
                        null_checks[it] = value
                }
                switch (null_checks.size()) {
                    case 0:
                        test_platforms[test_server['platform']] = 1
                        test_servers.push(test_server)
                        break

                    case 1..3:
                        log.warn("Malformed input '${sheet_name_server}:${rownum}'")
                        log.warn(null_checks.toString())
                        break
                }
            }
        }
    }

    def readSheetSpec(String platform, Sheet sheet_spec) throws IOException {
        sheet_spec.with { sheet ->
            (4 .. sheet.getLastRowNum()).each { rownum ->
                Row row = sheet.getRow(rownum)
                def yes_no      = row.getCell(0).getStringCellValue()
                def test_id     = row.getCell(1).getStringCellValue()
                def test_domain = row.getCell(3).getStringCellValue()
                if (test_id && test_domain && yes_no.toUpperCase() == "Y") {
                    test_domains[test_domain] = 1
                    test_specs[platform][test_domain][test_id] = new TestItem(test_id)
                }
            }
        }
    }

    def readSheet() throws IOException {
        log.info("Read test spec from ${evidence_source}")

        new FileInputStream(evidence_source).withStream { ins ->
            WorkbookFactory.create(ins).with { workbook ->
                // Read Excel test server sheet.
                log.debug("Read excel sheet '${evidence_source}:${sheet_name_server}'")
                def sheet_server = workbook.getSheet(sheet_name_server)
                if (sheet_server) {
                    readSheetServer(sheet_server)
                } else {
                    def msg = "Not found excel server list sheet '${sheet_name_server}'"
                    log.error(msg)
                    throw new IllegalArgumentException(msg)
                }
                // Read Excel test spec sheet.
                sheet_name_specs.each { platform, sheet_name_spec ->
                    log.debug("Read excel sheet '${evidence_source}:${sheet_name_spec}'")
                    def sheet_spec = workbook.getSheet(sheet_name_spec)
                    if (sheet_spec) {
                        readSheetSpec(platform, sheet_spec)
                    } else {
                        def msg = "Not found excel test spec sheet '${sheet_name_spec}'"
                        log.error(msg)
                        throw new IllegalArgumentException(msg)
                    }
                }
            }
        }

    }

    def updateTestResult(String platform, String server_name, int sequence, Map results)
        throws IOException {
        log.info("Update evidence : platform = ${platform}, server = ${server_name}")

        def inp = new FileInputStream(evidence_target)
        def wb  = WorkbookFactory.create(inp)
        def sheet_result = wb.getSheet(sheet_name_specs[platform])
        def cell_style = createBorderedStyle(wb)

        // 5列名以降の検査結果列の列幅 30 文字に設定 (in units of 1/256th of a character width)
        def column = 5 + sequence
        sheet_result.setColumnWidth(column, 7680)

        // ヘッダーに検査対象サーバ名を登録
        def title_cell = sheet_result.getRow(3).createCell(column)
        title_cell.setCellValue(server_name)
        title_cell.setCellStyle(cell_style)

        // 検査結果列を順に登録
        sheet_result.with { sheet ->
            (4 .. sheet.getLastRowNum()).each { rownum ->
                Row row = sheet.getRow(rownum)
                def row_style  = wb.createCellStyle().setWrapText(true)
                row.setRowStyle(row_style)

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
        def fos = new FileOutputStream(evidence_target)
        wb.write(fos)
        fos.close()
    }

    def prepareTestStage() throws IOException {
        def log_dir = new File(staging_dir)
        log_dir.deleteDir()
        log_dir.mkdir()
        test_domains.each { platform, flag ->
            def test_log_dir = new File("${staging_dir}/${platform}")
            log.info("Creating staging dir : ${test_log_dir}")
            test_log_dir.mkdir()
        }

        log.info("Copy evidence sheet : ${evidence_target}")
        File dest = new File(evidence_target)
        if (dest.exists()) {
            dest.delete()
        }
        dest << new File(evidence_source).readBytes()
    }
}

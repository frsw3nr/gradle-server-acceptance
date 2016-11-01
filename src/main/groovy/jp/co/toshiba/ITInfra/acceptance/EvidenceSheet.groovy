package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.io.FileUtils.*
import groovy.transform.ToString
import static groovy.json.JsonOutput.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.IndexedColors

public enum ResultCellStyle {
    NORMAL,
    TITLE,
    OK,
    NG,
}

@Slf4j
class EvidenceSheet {

    final row_header          = 3
    final row_body_begin      = 4
    final column_server_begin = 3
    final column_body_begin   = 6
    final column_device_begin = 2

    final device_cell_width   = 5760
    final evidence_cell_width = 11520

    String evidence_source
    String evidence_target
    String sheet_name_server
    String sheet_name_rule
    String staging_dir
    def sheet_name_specs

    def test_platforms
    def test_domains

    def test_servers
    def domain_test_ids
    def verify_rules
    def device_test_ids

    EvidenceSheet(String config_file = 'config/config.groovy') {
        def config = Config.instance.read(config_file)['evidence']

        log.debug("initialize evidence")
        evidence_source   = config['source'] ?: './check_sheet.xlsx'
        evidence_target   = config['target'] ?: './build/check_sheet.xlsx'
        sheet_name_server = config['sheet_name_server'] ?: 'Target'
        sheet_name_rule   = config['sheet_name_rule'] ?: 'Rule'
        staging_dir       = config['staging_dir'] ?: './build/log'
        sheet_name_specs  = config['sheet_name_spec'] ?: [
                                'Linux'   : 'CheckSheet(Linux)',
                                'Windows' : 'CheckSheet(Windows)'
                            ]

        def date = Config.instance.date
        test_platforms  = [:]
        test_domains    = [:]
        test_servers    = []
        domain_test_ids = [:].withDefault{[:].withDefault{[]}}
        verify_rules    = [:].withDefault{[:].withDefault{[:]}}
        device_test_ids = [:].withDefault{[:]}
    }

    // To add a border to the test results column, set the line width to the auto scale
    private static CellStyle createBorderedStyle(Workbook wb) {
        BorderStyle thin = BorderStyle.THIN;
        short black = IndexedColors.BLACK.getIndex();

        CellStyle style = wb.createCellStyle();
        style.with {
            setWrapText(true);
            setBorderRight(thin);
            setRightBorderColor(black);
            setBorderBottom(thin);
            setBottomBorderColor(black);
            setBorderTop(thin);
            setTopBorderColor(black);
        }
        return style;
    }

    def readSheetServer(Sheet sheet_server) throws IOException {
        log.debug("Read sheet '${sheet_name_server}'")
        def server_ids = [:]
        def server_info = [:].withDefault{[:]}
        def max_server_columns = 0
        sheet_server.with { sheet ->
            // check server_ids from header
            Row header_row = sheet.getRow(4)
            (column_server_begin .. header_row.getLastCellNum()).each { column ->
                def position = "4:${column}"
                def server_id_cell = header_row.getCell(column)
                if (server_id_cell) {
                    def server_id = server_id_cell.getStringCellValue()
                    if (server_id.size() > 0) {
                        log.debug "\t${position} : Add server_id '${server_id}'"
                        server_ids[column] = server_id
                        max_server_columns = column
                    }
                }
            }
            // check server from body
            (row_body_begin .. sheet.getLastRowNum()).each { rownum ->
                Row row = sheet.getRow(rownum)
                def item_id     = row.getCell(1).getStringCellValue()
                server_ids.each { column, server_id ->
                    def position = "${rownum}:${column}"
                    def server_text_cell = row.getCell(column)
                    if (server_text_cell) {
                        server_text_cell.setCellType(Cell.CELL_TYPE_STRING)
                        def server_text = server_text_cell.getStringCellValue()
                        if (server_text.size() > 0) {
                            def index = "${server_id},${item_id}"
                            log.debug "\t${position} : Add server ${index} = '${server_text}'"
                            server_info[server_id][item_id] = server_text
                        }
                    }
                }
            }
        }
        (column_server_begin..max_server_columns).each {
            def server_id = server_ids[it]
            def test_server = new TargetServer(server_info[server_id])
            test_platforms[test_server.platform] = 1
            test_servers.add(test_server)
        }
    }

    def readSheetSpec(String platform, Sheet sheet_spec) throws IOException {
        sheet_spec.with { sheet ->
            (row_body_begin .. sheet.getLastRowNum()).each { rownum ->
                Row row = sheet.getRow(rownum)
                try {
                    def yes_no    = row.getCell(0).getStringCellValue()
                    def test_id   = row.getCell(1).getStringCellValue()
                    def domain    = row.getCell(3).getStringCellValue()
                    def is_device = row.getCell(4).getStringCellValue()
                    if (test_id && domain && yes_no.toUpperCase() == "Y") {
                        test_domains[domain] = 1
                        domain_test_ids[platform][domain].add(test_id)
                        def index = "${platform}, ${domain}, ${test_id}"
                        log.debug "\t${rownum} : Add test_id '${index}'"
                        if (is_device.toUpperCase() == "Y") {
                            device_test_ids[domain][test_id] = 1
                        }
                    }
                } catch (NullPointerException e) {
                    log.warn "Malformed input '${platform}:${rownum}'"
                }
            }
        }
    }

    def readSheetRule(Sheet sheet_rule) throws IOException {
        log.debug("Read sheet '${sheet_name_rule}'")
        sheet_rule.with { sheet ->
            def verify_rule_ids = [:]
            // check domain_test_ids from header
            Row header_row = sheet.getRow(row_header)
            (row_body_begin .. header_row.getLastCellNum()).each { column ->
                def position = "${row_header}:${column}"
                def rule_id_cell = header_row.getCell(column)
                if (rule_id_cell) {
                    def rule_id = rule_id_cell.getStringCellValue()
                    if (rule_id.size() > 0) {
                        log.debug "\t${position} : Add rule_id '${rule_id}'"
                        verify_rule_ids[rule_id] = column
                    } else {
                        def msg = "Not found 'rule_id' at ${position}"
                        throw new IllegalArgumentException(msg)
                    }
                }
            }
            // check rule from body
            (row_body_begin .. sheet.getLastRowNum()).each { rownum ->
                Row row = sheet.getRow(rownum)
                def test_id     = row.getCell(1).getStringCellValue()
                def test_domain = row.getCell(3).getStringCellValue()
                verify_rule_ids.each { rule_id, column ->
                    def position = "${rownum}:${column}"
                    def rule_text_cell = row.getCell(column)
                    if (rule_text_cell) {
                        def rule_text = rule_text_cell.getStringCellValue()
                        if (rule_text.size() > 0) {
                            def index = "${rule_id},${test_domain},${test_id}"
                            log.debug "\t${position} : Add rule ${index} = '${rule_text}'"
                            verify_rules[rule_id][test_domain][test_id] = rule_text
                        }
                    }
                }
            }
        }
    }

    def readSheet() throws IOException {
        log.debug("Read test spec from ${evidence_source}")
        long start = System.currentTimeMillis()
        new FileInputStream(evidence_source).withStream { ins ->
            WorkbookFactory.create(ins).with { workbook ->
                // Read Excel test server sheet.
                log.debug("Read excel sheet '${evidence_source}:${sheet_name_server}'")
                def sheet_server = workbook.getSheet(sheet_name_server)
                if (sheet_server) {
                    readSheetServer(sheet_server)
                } else {
                    def msg = "Not found excel server list sheet '${sheet_name_server}'"
                    throw new IllegalArgumentException(msg)
                }
                // Read Excel test spec sheet.
                sheet_name_specs.each { platform, sheet_name_spec ->
                    log.debug("Read sheet '${sheet_name_spec}'")
                    def sheet_spec = workbook.getSheet(sheet_name_spec)
                    if (sheet_spec) {
                        readSheetSpec(platform, sheet_spec)
                    } else {
                        def msg = "Not found excel test spec sheet '${sheet_name_spec}'"
                        throw new IllegalArgumentException(msg)
                    }
                }
                // Read Excel verify rule sheet.
                log.debug("Read sheet '${sheet_name_rule}'")
                def sheet_rule = workbook.getSheet(sheet_name_rule)
                if (sheet_rule) {
                    readSheetRule(sheet_rule)
                } else {
                    def msg = "Not found excel server list sheet '${sheet_name_rule}'"
                    throw new IllegalArgumentException(msg)
                }
            }
        }
        long elapsed = System.currentTimeMillis() - start
        log.info "Load Sheet, Elapsed : ${elapsed} ms"
    }

    public static void setTestResultCellStyle(XSSFCell cell, ResultCellStyle type) {
        BorderStyle thin = BorderStyle.THIN;
        short black = IndexedColors.BLACK.getIndex();
        XSSFWorkbook wb = cell.getRow().getSheet().getWorkbook();
        CellStyle style = wb.createCellStyle();

        // Set Boder line
        style.setBorderRight(thin);
        style.setRightBorderColor(black);
        style.setBorderBottom(thin);
        style.setBottomBorderColor(black);
        style.setBorderTop(thin);
        style.setTopBorderColor(black);

        // Set Text font and Foreground color
        switch (type) {
            case ResultCellStyle.NORMAL :
                style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                break

            case ResultCellStyle.TITLE :
                XSSFFont font = wb.createFont();
                font.setBold(true);
                font.setColor(IndexedColors.BLACK.getIndex());
                style.setFont(font);
                break

            case ResultCellStyle.OK :
                style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                XSSFFont font = wb.createFont();
                font.setBold(true);
                font.setColor(IndexedColors.BLACK.getIndex());
                style.setFont(font);
                break

            case ResultCellStyle.NG :
                style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                XSSFFont font = wb.createFont();
                font.setBold(true);
                font.setColor(IndexedColors.BLACK.getIndex());
                style.setFont(font);
                break

            default :
                break
        }
        style.setWrapText(true);

        cell.setCellStyle(style);
    }

    def updateTestResult(String platform, String server_name, int sequence, Map results)
        throws IOException {
        log.info("Update evidence : platform = ${platform}, server = ${server_name}")

        def inp = new FileInputStream(evidence_target)
        def wb  = WorkbookFactory.create(inp)
        def sheet_result = wb.getSheet(sheet_name_specs[platform])
        def cell_style = createBorderedStyle(wb)

        // Set the column width to 45 characters of the inspection result column 
        // after the body column name
        // (in units of 1/256th of a character width)
        def column = column_body_begin + sequence
        sheet_result.setColumnWidth(column, evidence_cell_width)

        // Register the target server name in the header
        def title_cell = sheet_result.getRow(row_header).createCell(column)
        title_cell.setCellValue(server_name)
        title_cell.setCellStyle(cell_style)
        setTestResultCellStyle(title_cell, ResultCellStyle.TITLE)
        log.debug "Update data : " + results


        // Registering the test result column in the order
        sheet_result.with { sheet ->
            (row_body_begin .. sheet.getLastRowNum()).each { rownum ->
                Row row = sheet.getRow(rownum)
                def row_style  = wb.createCellStyle().setWrapText(true)
                row.setRowStyle(row_style)

                Cell cell_test_id = row.getCell(1)
                Cell cell_domain  = row.getCell(3)
                Cell cell_result  = row.createCell(column)
                cell_result.setCellStyle(cell_style)
                if (cell_test_id && cell_domain) {
                    def test_id = cell_test_id.getStringCellValue()
                    def domain  = cell_domain.getStringCellValue()
                    log.debug "Check row ${domain},${test_id} in ${platform}"

                    try {
                        if (results[domain]['test'].containsKey(test_id)) {
                            def value = results[domain]['test'][test_id].toString()
                            if (NumberUtils.isDigits(value)) {
                                cell_result.setCellValue(NumberUtils.toDouble(value))
                            } else {
                                cell_result.setCellValue(value)
                            }
                            log.debug "Update cell(${platform}:${domain}:${test_id}) = ${value}"
                        }
                        if (results[domain]['verify'].containsKey(test_id)) {
                            def is_ok = results[domain]['verify'][test_id]
                            if (is_ok == true) {
                                setTestResultCellStyle(cell_result, ResultCellStyle.OK)
                            } else if (is_ok == false) {
                                setTestResultCellStyle(cell_result, ResultCellStyle.NG)
                            }
                            log.debug "Update Verify status : ${domain},${test_id} = ${is_ok}"
                        } else {
                            setTestResultCellStyle(cell_result, ResultCellStyle.NORMAL)
                        }
                    } catch (NullPointerException e) {
                        log.debug "Not found row ${domain},${test_id} in ${platform}"
                    }
                }
            }
        }
        def fos = new FileOutputStream(evidence_target)
        wb.write(fos)
        fos.close()
    }

    def insertDeviceSheet(String platform, String test_id, List headers, Map csvs)
        throws IOException {
        def device_sheet_name = "${platform}_${test_id}"
        log.info("Insert device sheet : ${device_sheet_name}")
        def inp = new FileInputStream(evidence_target)
        def wb  = WorkbookFactory.create(inp)

        Sheet device_sheet = wb.createSheet(device_sheet_name)

        device_sheet.with { sheet ->
            // Header registration
            Row header_row = sheet.createRow(row_header)
            def header_column_index = column_device_begin
            sheet.setColumnWidth(header_column_index, device_cell_width)
            header_row.createCell(header_column_index).setCellValue('server_name')
            header_column_index ++
            headers.each { header_name ->
                header_row.createCell(header_column_index).setCellValue(header_name)
                sheet.setColumnWidth(header_column_index, device_cell_width)
                header_column_index ++
            }
            // Body registration
            def body_row_index = row_body_begin
            csvs.each {server_name, server_csvs ->
                server_csvs.each { server_csv ->
                    Row body_row = sheet.createRow(body_row_index)
                    def body_column_index = column_device_begin
                    body_row.createCell(body_column_index).setCellValue(server_name)
                    body_column_index ++
                    server_csv.each { column_value ->
                        def value = column_value.toString().trim()
                        def csv_cell = body_row.createCell(body_column_index)
                        if (NumberUtils.isDigits(value)) {
                            csv_cell.setCellValue(NumberUtils.toDouble(value))
                        } else {
                            csv_cell.setCellValue(value)
                        }

                        body_column_index ++
                    }
                    body_row_index ++
                }
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
        test_platforms.each { platform, flag ->
            def test_log_dir = new File("${staging_dir}/${platform}")
            log.debug("Creating staging dir : ${test_log_dir}")
            test_log_dir.mkdir()
        }

        log.debug("Copy evidence sheet : ${evidence_target}")
        File dest = new File(evidence_target)
        if (dest.exists()) {
            dest.delete()
        }
        dest << new File(evidence_source).readBytes()
    }
}

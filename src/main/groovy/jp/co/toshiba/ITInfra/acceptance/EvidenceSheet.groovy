package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.io.FileUtils.*
import groovy.transform.ToString
import groovy.json.*
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
    SAME,
    NOTFOUND,
    NOTEST,
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
    final node_dir_prefix     = "_node"

    String config_file
    String evidence_source
    String evidence_target
    String target_original
    String sheet_name_server
    String sheet_name_rule
    String staging_dir
    def sheet_name_specs

    def test_platforms
    def test_domains

    def test_servers
    def test_servers_hash
    def compare_servers
    def domain_test_ids
    def verify_rules
    def device_test_ids

    EvidenceSheet(String config_file = 'config/config.groovy') {
        this.config_file = config_file
        def config = Config.instance.read(config_file)['evidence']

        log.debug("initialize evidence")
        evidence_source   = config['source'] ?: './check_sheet.xlsx'
        evidence_target   = config['target'] ?: './build/check_sheet.xlsx'
        target_original   = config['target_original'] ?: './build/check_sheet.xlsx'
        sheet_name_server = config['sheet_name_server'] ?: 'Target'
        sheet_name_rule   = config['sheet_name_rule'] ?: 'Rule'
        staging_dir       = config['staging_dir'] ?: './build/log'
        sheet_name_specs  = config['sheet_name_spec'] ?: [
                                'Linux'   : 'CheckSheet(Linux)',
                                'Windows' : 'CheckSheet(Windows)'
                            ]

        def date = Config.instance.date
        test_platforms    = [:]
        test_domains      = [:]
        test_servers      = []
        test_servers_hash = [:]
        compare_servers   = [:]
        domain_test_ids   = [:].withDefault{[:].withDefault{[]}}
        verify_rules      = [:].withDefault{[:].withDefault{[:]}}
        device_test_ids   = [:].withDefault{[:]}
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
        def errors = []
        def max_server_columns = 0
        sheet_server.with { sheet ->
            // check server_ids from header
            Row header_row = sheet.getRow(4)
            (column_server_begin .. header_row.getLastCellNum()).each { column ->
                def position = "4:${column}"
                def server_id_cell = header_row.getCell(column)
                if (server_id_cell) {
                    try {
                        def server_id = server_id_cell.getStringCellValue()
                        if (server_id.size() > 0) {
                            log.debug "\t${position} : Add server_id '${server_id}'"
                            server_ids[column] = server_id
                            max_server_columns = column
                        }
                    } catch (IllegalStateException e) {
                        errors << "Malformed input @ ${sheet_name_server}{${position}} : " + e
                    }
                }
            }
            // check server from body
            (row_body_begin .. sheet.getLastRowNum()).find { rownum ->
                Row row = sheet.getRow(rownum)
                if (row == null)
                    return true
                def item_id     = row.getCell(1).getStringCellValue()
                server_ids.each { column, server_id ->
                    def position = "${rownum}:${column}"
                    def server_text_cell = row.getCell(column)
                    if (server_text_cell) {
                        server_text_cell.setCellType(Cell.CELL_TYPE_STRING)
                        try {
                            def server_text = server_text_cell.getStringCellValue()
                            if (server_text.size() > 0) {
                                def index = "${server_id},${item_id}"
                                log.debug "\t${position} : Add server ${index} = '${server_text}'"
                                server_info[server_id][item_id] = server_text
                            }
                        } catch (IllegalStateException e) {
                            errors << "Malformed input @ ${sheet_name_server}{${position}} : " + e
                        }
                    }
                }
                return
            }
        }
        if (errors) {
            errors.each { error_message ->
                log.error error_message
            }
            throw new IllegalArgumentException('Excel error')
        }
        (column_server_begin..max_server_columns).each {
            def server_id = server_ids[it]
            def test_server = new TargetServer(server_info[server_id])
            test_platforms[test_server.platform] = 1
            test_servers.add(test_server)
            test_servers_hash[server_id] = test_server
        }
    }

    def readServerConfigScript(String server_config_script) throws IOException {
        log.debug("Read script '${server_config_script}'")

        def config = new ConfigSlurper().parse(new File(server_config_script).getText())
        (config['server_infos']).each { server_info ->
            def test_server = new TargetServer(server_info)
            test_platforms[test_server.platform] = 1
            test_servers.add(test_server)
        }
    }

    def readSheetSpec(String platform, Sheet sheet_spec) throws IOException {
        sheet_spec.with { sheet ->
            (row_body_begin .. sheet.getLastRowNum()).find { rownum ->
                Row row = sheet.getRow(rownum)
                if (row == null)
                    return true
                try {
                    def yes_no    = row.getCell(0).getStringCellValue().trim()
                    def test_id   = row.getCell(1).getStringCellValue().trim()
                    def domain    = row.getCell(3).getStringCellValue().trim()
                    def is_device = row.getCell(4).getStringCellValue().trim()
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
                    log.warn "Malformed input '${platform}:${rownum}'" + e
                }
                return
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
            (row_body_begin .. sheet.getLastRowNum()).find { rownum ->
                Row row = sheet.getRow(rownum)
                if (row == null)
                    return true
                def test_id_cell = row.getCell(1)
                if (!test_id_cell)
                    return true
                def test_id     = test_id_cell.getStringCellValue()
                def test_domain = row.getCell(3).getStringCellValue() ?: '_common'
                if (test_domain) {
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
                return
            }
            verify_rules.each { verify_id, domain_rules ->
                domain_rules['_common'].with {
                    if (compare_server && compare_source) {
                        this.compare_servers[compare_server] = compare_source
                    } else if (compare_server || compare_source) {
                        log.error "'compare_server'(${compare_server}) and 'compare_source'(${compare_source}) are mandatory in pairs. Skip."
                    }
                }
            }
        }
    }

    def readSheet(String server_config_script = null) throws IOException {
        log.debug("Read test spec from ${evidence_source}")
        long start = System.currentTimeMillis()
        new FileInputStream(evidence_source).withStream { ins ->
            WorkbookFactory.create(ins).with { workbook ->
                // Read Excel test server sheet.
                if (server_config_script) {
                    readServerConfigScript(server_config_script)
                } else {
                    log.debug("Read excel sheet '${evidence_source}:${sheet_name_server}'")
                    def sheet_server = workbook.getSheet(sheet_name_server)
                    if (sheet_server) {
                        readSheetServer(sheet_server)
                    } else {
                        def msg = "Not found excel server list sheet '${sheet_name_server}'"
                        throw new IllegalArgumentException(msg)
                    }
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
        // Set compare target server info. in test servers.
        this.test_servers.each { test_server ->
            def verify_id = test_server.infos['verify_id']
            if (verify_id) {
                def compare_server = this.verify_rules[verify_id]['_common']['compare_server']
                def compare_source = compare_servers[compare_server]
                test_server.compare_server = compare_server
                test_server.compare_source = compare_source
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

            case ResultCellStyle.SAME :
                style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                XSSFFont font = wb.createFont();
                font.setColor(IndexedColors.ROYAL_BLUE.getIndex());
                style.setFont(font);
                break

            case ResultCellStyle.NOTFOUND :
                style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                XSSFFont font = wb.createFont();
                font.setColor(IndexedColors.CORAL.getIndex());
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

    def writeNodeFile(String platform, String server_name, def node_config)
        throws IOException {
        def base_dir = "${staging_dir}/${node_dir_prefix}"
        def node_dir = "${base_dir}/${platform}"
        new File(node_dir).mkdirs()
        new File("${node_dir}/${server_name}.json").with {
            def json = JsonOutput.toJson(node_config)
            it.text = JsonOutput.prettyPrint(json)
        }
        new File("./build/.last_run").with {
            def run_config = ['node_dir' : base_dir, 'evidence' : evidence_target,
                              'target' : target_original, 'config_file' : config_file ]
            it.write( JsonOutput.toJson(run_config))
        }
    }

    def updateTemplateResult(String platform, String server_name, int sequence)
        throws IOException {
        log.info("Update template : platform = ${platform}, server = ${server_name}")
        def results = ResultContainer.instance.test_results[server_name][platform]
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
        title_cell.setCellValue("template:${server_name}")
        title_cell.setCellStyle(cell_style)
        setTestResultCellStyle(title_cell, ResultCellStyle.TITLE)

        // Registering the test result column in the order
        sheet_result.with { sheet ->
            (row_body_begin .. sheet.getLastRowNum()).find { rownum ->
                Row row = sheet.getRow(rownum)
                if (row == null)
                    return true
                def row_style  = wb.createCellStyle().setWrapText(true)
                row.setRowStyle(row_style)

                Cell cell_test_id = row.getCell(1)
                Cell cell_domain  = row.getCell(3)
                Cell cell_result  = row.createCell(column)
                cell_result.setCellStyle(cell_style)
                if (cell_test_id && cell_domain) {
                    def test_id = cell_test_id.getStringCellValue()
                    def domain  = cell_domain.getStringCellValue()

                    try {
                        def value = results[test_id]?.toString()
                        if (!value || value == '[:]') {
                            value = (test_id ==~ /.+\..+/) ? 'Not found' : ''
                        }
                        if (NumberUtils.isDigits(value)) {
                            cell_result.setCellValue(NumberUtils.toDouble(value))
                        } else {
                            cell_result.setCellValue(value)
                        }
                        log.debug "Update $server_name:$test_id = $value"
                        setTestResultCellStyle(cell_result, ResultCellStyle.NORMAL)
                    } catch (NullPointerException e) {
                        log.debug "Not found row ${domain},${test_id} in ${platform}"
                    }
                }
                return
            }
        }
        def fos = new FileOutputStream(evidence_target)
        wb.write(fos)
        fos.close()
    }

    def updateTestResult(String platform, String server_name, int sequence, Map results)
        throws IOException {
        log.info("Update evidence : platform = ${platform}, server = ${server_name}")

        def node_config = []
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
        def compare_server = test_servers_hash[server_name]?.compare_server

        // Registering the test result column in the order
        sheet_result.with { sheet ->
            (row_body_begin .. sheet.getLastRowNum()).find { rownum ->
                Row row = sheet.getRow(rownum)
                if (row == null)
                    return true
                def row_style  = wb.createCellStyle().setWrapText(true)
                row.setRowStyle(row_style)

                Cell cell_test_id = row.getCell(1)
                Cell cell_domain  = row.getCell(3)
                Cell cell_result  = row.createCell(column)
                cell_result.setCellStyle(cell_style)
                if (cell_test_id && cell_domain) {
                    def rows = [:]
                    def test_id = cell_test_id.getStringCellValue()
                    rows['test_id'] = test_id
                    def domain  = cell_domain.getStringCellValue()
                    rows['domain']  = domain

                    try {
                        def style = ResultCellStyle.NORMAL
                        def value = ''
                        if (results[domain]['test'].containsKey(test_id)) {
                            value = results[domain]['test'][test_id].toString()
                            rows['value']  = value
                            if (results[domain]['verify'].containsKey(test_id)) {
                                def is_ok = results[domain]['verify'][test_id]
                                rows['verify']  = is_ok
                                style = (is_ok == true) ? ResultCellStyle.OK : ResultCellStyle.NG
                            } else if (compare_server && ResultContainer.instance.compareMetric(
                                compare_server, platform, test_id, value)) {
                                style = ResultCellStyle.SAME
                                value = "Same as '${compare_server}'"
                            }
                        } else if (test_id ==~ /.+\..+/) {
                            if (compare_server &&
                                !ResultContainer.instance.test_results[compare_server][platform][test_id]) {
                                style = ResultCellStyle.SAME
                                value = "Same as '${compare_server}'"
                            } else {
                                style = ResultCellStyle.NOTFOUND
                                value = 'Not found'
                            }
                        } else {
                            style = ResultCellStyle.NOTEST
                        }
                        log.debug "Update cell(${platform}:${domain}:${test_id}) = ${value}"
                        setTestResultCellStyle(cell_result, style)
                        if (NumberUtils.isDigits(value)) {
                            cell_result.setCellValue(NumberUtils.toDouble(value))
                        } else {
                            cell_result.setCellValue(value)
                        }
                    } catch (NullPointerException e) {
                        log.debug "Not found row ${domain},${test_id} in ${platform}"
                    }
                    if (rows.containsKey('value')) {
                        node_config << rows
                    }
                }
                return
            }
        }
        def fos = new FileOutputStream(evidence_target)
        wb.write(fos)
        fos.close()
        writeNodeFile(platform, server_name, node_config)
    }

    def writeDeviceFile(String platform, String test_id, List headers, Map csvs)
        throws IOException {
        def base_dir = "${staging_dir}/${node_dir_prefix}"
        // Body registration
        csvs.each {server_name, server_csvs ->
            def device_config = []
            server_csvs.each { server_csv ->
                def columns = [:]
                def column_index = 0
                server_csv.each { column_value ->
                    def value = column_value.toString().trim()
                    columns[headers[column_index]] = value
                    column_index ++
                }
                device_config << columns

                def node_dir = "${base_dir}/${platform}/${server_name}"
                new File(node_dir).mkdirs()
                new File("${node_dir}/${test_id}.json").with {
                    def json = JsonOutput.toJson(device_config)
                    it.text = JsonOutput.prettyPrint(json)
                }
            }
        }
    }

    def insertDeviceSheet(String platform, String test_id, List headers, Map csvs)
        throws IOException {
        def device_sheet_name = "${platform}_${test_id}"
        log.info("Insert device sheet : ${device_sheet_name}")
        def inp = new FileInputStream(evidence_target)
        def wb  = WorkbookFactory.create(inp)
        def is_exists = false
        Sheet device_sheet = wb.getSheet(device_sheet_name)
        if (device_sheet) {
            is_exists = true
        } else {
            device_sheet = wb.createSheet(device_sheet_name)
        }

        device_sheet.with { sheet ->
            // Header registration
            def header_column_index = column_device_begin + headers.size()
            if (!is_exists) {
                Row header_row = sheet.createRow(row_header)
                header_column_index = column_device_begin
                sheet.setColumnWidth(header_column_index, device_cell_width)
                header_row.createCell(header_column_index).setCellValue('server_name')
                header_column_index ++
                headers.each { header_name ->
                    header_row.createCell(header_column_index).setCellValue(header_name)
                    sheet.setColumnWidth(header_column_index, device_cell_width)
                    header_column_index ++
                }
            }
            // Find last row
            def body_row_index = row_body_begin
            if (is_exists) {
                (row_body_begin .. device_sheet.getLastRowNum()).find { rownum ->
                    body_row_index = rownum
                    if (sheet.getRow(rownum) == null)
                        return true
                    return
                }
            }
            // Body registration
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
            (0..header_column_index).each {
                sheet.autoSizeColumn(it, true)
            }
        }
        def fos = new FileOutputStream(evidence_target)
        wb.write(fos)
        fos.close()
        writeDeviceFile(platform, test_id, headers, csvs)
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

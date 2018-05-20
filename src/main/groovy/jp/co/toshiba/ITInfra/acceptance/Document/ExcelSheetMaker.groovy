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

    def output(String excel_evidence) {
        println excel_parser.sheet_sources
        // SheetDesign sheet_design = excel_parser.sheet_sources['check_sheet']['Linux']
        // println sheet_design.sheet
        excel_parser.sheet_sources['check_sheet'].each { domain, sheet_design ->
            evidence_maker.summary_sheets[domain].each { summary_sheet ->
                write_sheet_summary(summary_sheet, sheet_design)
            }
        }
        def fos = new FileOutputStream(excel_evidence)
        excel_parser.workbook.write(fos)
        fos.close()

    }

    // def write_cell_summary(Cell cell, TestResult test_result) {
    def write_cell_summary(Cell cell, test_result) {
        if (test_result) {
            cell.setCellValue(test_result.value)
            if (test_result.status == null) {
                setTestResultCellStyle(cell, ResultCellStyle.NOTEST)
            } else if (test_result.status == ResultStatus.NG) {
                setTestResultCellStyle(cell, ResultCellStyle.ERROR)
            } else if (test_result.status == ResultStatus.MATCH) {
                setTestResultCellStyle(cell, ResultCellStyle.SAME)
            } else if (test_result.verify == ResultStatus.OK) {
                setTestResultCellStyle(cell, ResultCellStyle.OK)
            } else if (test_result.verify == ResultStatus.NG) {
                setTestResultCellStyle(cell, ResultCellStyle.NG)
            } else if (test_result.status == ResultStatus.WARNING) {
                setTestResultCellStyle(cell, ResultCellStyle.WARNING)
            } else {
                setTestResultCellStyle(cell, ResultCellStyle.NORMAL)
            }
        }
    }

    def write_cell_summary_header(Cell cell, String header) {
        cell.setCellValue(header)
        setTestResultCellStyle(cell, ResultCellStyle.TITLE)
    }

    def write_sheet_header(sheet, int[] position, String[] headers) {
        Row header_row = sheet.getRow(position[0])
        def colnum = 0
        headers.each { header ->
            Cell cell = header_row.createCell(colnum + position[1])
            cell.setCellValue(header)
            setTestResultCellStyle(cell, ResultCellStyle.TITLE)
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
                println "ROW: $rownum:$platform_metric_key"
                Row row = sheet.getRow(rownum + result_position[0])
                if (row == null)
                    return true
                row.setRowStyle(row_style)
                sheet_summary.cols.each { target, column_index ->
                    def colnum = column_index + result_position[1] - 1
                    sheet.setColumnWidth(colnum, evidence_cell_width)
                    Cell cell = row.createCell(colnum)
                    def platform = platform_metric_key[0]
                    def metric = platform_metric_key[1]
                    def test_result = summary_results[platform][metric][target]
                    try {
                        write_cell_summary(cell, test_result)

    // XSSFWorkbook wb = cell.getRow().getSheet().getWorkbook();
    XSSFCellStyle cs1 = workbook.createCellStyle();
    // cs1.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
    cs1.setFillBackgroundColor(IndexedColors.LIGHT_GREEN.getIndex());
    cs1.setFillPattern(CellStyle.SOLID_FOREGROUND);

    XSSFFont f = workbook.createFont();
    f.setBold(true);
    f.setColor(IndexedColors.RED.getIndex());
    cs1.setFont(f);
    cell.setCellStyle(cs1);

    // XSSFWorkbook wb = cell.getRow().getSheet().getWorkbook();
    // XSSFCellStyle style = wb.createCellStyle();
    // XSSFFont font = wb.createFont();
    // font.setBold(false);
    // def FontColor = IndexedColors.LIGHT_GREEN
    // font.setColor(FontColor.getIndex());
    // style.setFont(font);
    // def FGcolor = IndexedColors.RED
    // style.setFillForegroundColor(FGcolor.getIndex());
    // style.setFillPattern(CellStyle.SOLID_FOREGROUND);
    // cell.setCellStyle(style);

        // CellStyle style = workbook.createCellStyle();
        // BorderStyle thin = BorderStyle.THIN;
        // short black = IndexedColors.BLACK.getIndex();
        // style.setBorderRight(thin);
        // style.setRightBorderColor(black);
        // style.setBorderBottom(thin);
        // style.setBottomBorderColor(black);
        // style.setBorderTop(thin);
        // style.setTopBorderColor(black);

        // XSSFFont font = workbook.createFont();
        // font.setBold(true);
        // style.setFont(font);
        // cell.setCellStyle(style);

                    } catch (NullPointerException e) {
                        log.debug "Not found row ${platform},${metric}"
                    }
                }
            }
        }
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

    // def readSheetServer(Sheet sheet_server) throws IOException {
    //     log.debug("Read sheet '${sheet_name_server}'")
    //     def server_ids = [:]
    //     def server_info = [:].withDefault{[:]}
    //     def errors = []
    //     def server_name_row = 7
    //     def max_server_columns = 0
    //     sheet_server.with { sheet ->

    //         // Check rownum of 'server_name'
    //         (row_body_begin .. sheet.getLastRowNum()).find { rownum ->
    //             Row row = sheet.getRow(rownum)
    //             if (row == null)
    //                 return true
    //             if (row.getCell(1).getStringCellValue() == 'server_name') {
    //                 server_name_row = rownum
    //                 return
    //             }
    //         }
    //         // Check server name from header, set the map of 'server_ids'.
    //         Row header_row = sheet.getRow(server_name_row)
    //         (column_server_begin .. header_row.getLastCellNum()).each { column ->
    //             def position = "${server_name_row}:${column}"
    //             def server_id_cell = header_row.getCell(column)
    //             if (server_id_cell) {
    //                 try {
    //                     def server_id = server_id_cell.getStringCellValue()
    //                     if (server_id.size() > 0) {
    //                         log.debug "\t${position} : Add server_id '${server_id}'"
    //                         server_ids[column] = server_id
    //                         max_server_columns = column
    //                     }
    //                 } catch (IllegalStateException e) {
    //                     errors << "Malformed input @ ${sheet_name_server}{${position}} : " + e
    //                 }
    //             }
    //         }
    //         if (server_ids.size() == 0) {
    //             log.info "Target server not found in '${sheet_name_server}' sheet."
    //             return
    //         }

    //         // Check server from body
    //         (row_body_begin .. sheet.getLastRowNum()).find { rownum ->
    //             Row row = sheet.getRow(rownum)
    //             if (row == null)
    //                 return true
    //             def item_id     = row.getCell(1).getStringCellValue()
    //             server_ids.each { column, server_id ->
    //                 def position = "${rownum}:${column}"
    //                 def server_text_cell = row.getCell(column)
    //                 if (server_text_cell) {
    //                     server_text_cell.setCellType(Cell.CELL_TYPE_STRING)
    //                     try {
    //                         def server_text = server_text_cell.getStringCellValue()
    //                         if (server_text.size() > 0) {
    //                             def index = "${server_id},${item_id}"
    //                             log.debug "\t${position} : Add server ${index} = '${server_text}'"
    //                             server_info[server_id][item_id] = server_text
    //                         }
    //                     } catch (IllegalStateException e) {
    //                         errors << "Malformed input @ ${sheet_name_server}{${position}} : " + e
    //                     }
    //                 }
    //             }
    //             return
    //         }
    //     }
    //     if (errors) {
    //         errors.each { error_message ->
    //             log.error error_message
    //         }
    //         throw new IllegalArgumentException('Excel error')
    //     }
    //     (column_server_begin..max_server_columns).each {
    //         def server_id = server_ids[it]
    //         if (!server_id)
    //             return
    //         def test_server = new TargetServer(server_info[server_id])
    //         if (sheet_name_specs.containsKey(test_server.platform)) {
    //             test_platforms[test_server.platform] = 1
    //             test_servers.add(test_server)
    //             test_servers_hash[server_id] = test_server
    //         } else {
    //             log.warn "Malformed input : ${test_server.platform}"
    //         }
    //     }
    // }

    // def setServerInfos(server_infos = [:]) {
    //     server_infos.each { id, server_info ->
    //         def test_server = new TargetServer(server_info)
    //         test_platforms[test_server.platform] = 1
    //         test_servers.add(test_server)
    //     }
    // }

    // def readServerConfigScript(String server_config_script) throws IOException {
    //     log.debug("Read script '${server_config_script}'")

    //     def config = new ConfigSlurper().parse(new File(server_config_script).getText())
    //     (config['server_infos']).each { server_info ->
    //         def test_server = new TargetServer(server_info)
    //         test_platforms[test_server.platform] = 1
    //         test_servers.add(test_server)
    //     }
    // }

    // def readServerConfigCSV(String server_config_csv) throws IOException {
    //     log.debug("Read CSV '${server_config_csv}'")

    //     def config = Config.instance.read(config_file)
    //     def csv_item_map = config['evidence']['csv_item_map']
    //     if (!csv_item_map) {
    //         def msg = "Not found parameter 'evidence.csv_item_map' in config.groovy"
    //         throw new IllegalArgumentException(msg)
    //     }

    //     def csv = new File(server_config_csv).getText("MS932")
    //     def data = new CsvParser().parse(csv, separator: ',', quoteChar: '"')
    //     def row_num = 1
    //     data.each { row ->
    //         def server_info = [:]
    //         def set_count = 0
    //         csv_item_map.each { column_name, property ->
    //             def column_pos = row.columns.get(column_name)
    //             if (column_pos != null) {
    //                 set_count ++
    //                 def value = row.values[column_pos]
    //                 server_info[property] = value
    //             }
    //         }
    //         if (set_count != csv_item_map.size()) {
    //             def msg = "CSV header is not appropriate"
    //             throw new IllegalArgumentException(msg)
    //         }
    //         if (!server_info) {
    //             def msg = "Parse error at '${server_config_csv}:${row_num}'"
    //             throw new IllegalArgumentException(msg)
    //         }
    //         def test_server = new TargetServer(server_info)
    //         test_platforms[test_server.platform] = 1
    //         test_servers.add(test_server)
    //         row_num ++
    //     }
    // }

    // def readSheetSpec(String platform, Sheet sheet_spec) throws IOException {
    //     sheet_spec.with { sheet ->
    //         (row_body_begin .. sheet.getLastRowNum()).find { rownum ->
    //             Row row = sheet.getRow(rownum)
    //             if (row == null)
    //                 return true
    //             try {
    //                 def yes_no    = row.getCell(0).getStringCellValue().trim()
    //                 def test_id   = row.getCell(1).getStringCellValue().trim()
    //                 def domain    = row.getCell(3).getStringCellValue().trim()
    //                 def is_device = row.getCell(4).getStringCellValue().trim()
    //                 if (test_id && domain && yes_no.toUpperCase() == "Y") {
    //                     test_domains[domain] = 1
    //                     domain_test_ids[platform][domain].add(test_id)
    //                     def index = "${platform}, ${domain}, ${test_id}"
    //                     log.debug "\t${rownum} : Add test_id '${index}'"
    //                     if (is_device.toUpperCase() == "Y") {
    //                         device_test_ids[domain][test_id] = 1
    //                     }
    //                 }
    //             } catch (NullPointerException e) {
    //                 log.warn "Malformed input '${platform}:${rownum}'" + e
    //             }
    //             return
    //         }
    //     }
    // }

    // def readSheetRule(Sheet sheet_rule) throws IOException {
    //     log.debug("Read sheet '${sheet_name_rule}'")
    //     sheet_rule.with { sheet ->
    //         def verify_rule_ids = [:]
    //         // check domain_test_ids from header
    //         Row header_row = sheet.getRow(row_header)
    //         (row_body_begin .. header_row.getLastCellNum()).each { column ->
    //             def position = "${row_header}:${column}"
    //             def rule_id_cell = header_row.getCell(column)
    //             if (rule_id_cell) {
    //                 def rule_id = rule_id_cell.getStringCellValue()
    //                 if (rule_id.size() > 0) {
    //                     log.debug "\t${position} : Add rule_id '${rule_id}'"
    //                     verify_rule_ids[rule_id] = column
    //                 } else {
    //                     def msg = "Not found 'rule_id' at ${position}"
    //                     throw new IllegalArgumentException(msg)
    //                 }
    //             }
    //         }
    //         // check rule from body
    //         (row_body_begin .. sheet.getLastRowNum()).find { rownum ->
    //             Row row = sheet.getRow(rownum)
    //             if (row == null)
    //                 return true
    //             def test_id_cell = row.getCell(1)
    //             if (!test_id_cell)
    //                 return true
    //             def test_id     = test_id_cell.getStringCellValue()
    //             def test_domain = row.getCell(3).getStringCellValue() ?: '_common'
    //             if (test_domain) {
    //                 verify_rule_ids.each { rule_id, column ->
    //                     def position = "${rownum}:${column}"
    //                     def rule_text_cell = row.getCell(column)
    //                     if (rule_text_cell) {
    //                         def rule_text = rule_text_cell.getStringCellValue()
    //                         if (rule_text.size() > 0) {
    //                             def index = "${rule_id},${test_domain},${test_id}"
    //                             log.debug "\t${position} : Add rule ${index} = '${rule_text}'"
    //                             verify_rules[rule_id][test_domain][test_id] = rule_text
    //                         }
    //                     }
    //                 }
    //             }
    //             return
    //         }
    //         verify_rules.each { verify_id, domain_rules ->
    //             domain_rules['_common'].with {
    //                 if (compare_server && compare_source) {
    //                     this.compare_servers[compare_server] = compare_source
    //                 }
    //             }
    //         }
    //     }
    // }

    // def readTestResult(Sheet sheet) throws IOException {
    //     log.info "Check test result sheet '${sheet.getSheetName()}'"
    //     // check servers from header
    //     def servers = [:]
    //     Row header_row = sheet.getRow(row_header)
    //     def column_body_end = header_row.getLastCellNum() - 1

    //     if (column_body_end < column_body_begin)
    //         return;

    //     (column_body_begin .. column_body_end).each { colnum ->
    //         servers[colnum] = header_row.getCell(colnum).getStringCellValue()
    //     }
    //     log.info "Fetch server '${servers.toString()}'"
    //     // Fetch test results
    //     def csv_cols = [:].withDefault{[]}
    //     (row_body_begin .. sheet.getLastRowNum()).each { rownum ->
    //         Row row = sheet.getRow(rownum)
    //         if (row == null)
    //             return true
    //         def testid = "${row?.getCell(1) ?: ''}"
    //         def domain = "${row?.getCell(3) ?: ''}"
    //         if (testid == '' || domain == '')
    //             return
    //         (column_body_begin .. column_body_end).each { colnum ->
    //             def key = "${servers[colnum]}:${domain}:${testid}"
    //             csv_cols[colnum] << [key, servers[colnum], domain, testid, "${row.getCell(colnum)}"]
    //         }
    //     }
    //     def csv = []
    //     csv_cols.sort().each { colnum, csv_col ->
    //         csv += csv_col
    //     }
    //     return csv
    // }

    // def readAllTestResult() throws IOException {
    //     def csv = []
    //     csv << ['Key', 'ServerName', 'Domain', 'TestItem', 'Value']
    //     def results = [:]
    //     new FileInputStream(evidence_source).withStream { ins ->
    //         WorkbookFactory.create(ins).with { workbook ->
    //             Iterator<Sheet> sheets = workbook.sheetIterator()
    //             while(sheets.hasNext()) {
    //                 Sheet sheet = sheets.next()
    //                 // 4行目が Test,ID で始まる行かチェック
    //                 if ("${sheet.getRow(3)?.getCell(0)}" == 'Test' &&
    //                     "${sheet.getRow(3)?.getCell(1)}" == 'ID') {
    //                     def sheet_csv = readTestResult(sheet)
    //                     if (sheet_csv)
    //                         csv += sheet_csv
    //                 }
    //             }
    //         }
    //     }
    //     return csv
    // }

    // def readSheet(HashMap options = [:]) throws IOException {
    //     log.debug("Read test spec from ${evidence_source}")
    //     long start = System.currentTimeMillis()
    //     new FileInputStream(evidence_source).withStream { ins ->
    //         WorkbookFactory.create(ins).with { workbook ->
    //             // Read Excel test server sheet.
    //             if (options['server_config']) {
    //                 def server_config_script = options['server_config']
    //                 def ext = server_config_script[server_config_script.lastIndexOf('.')..-1]
    //                 if (ext == '.groovy') {
    //                     readServerConfigScript(server_config_script)
    //                 } else if (ext == '.csv') {
    //                     readServerConfigCSV(server_config_script)
    //                 } else {
    //                     def msg = "Usage :getconfig -i (input.groovy|input.csv)"
    //                     throw new IllegalArgumentException(msg)
    //                 }

    //             } else if (options['server_infos']) {
    //                 setServerInfos(options['server_infos'])

    //             } else {
    //                 log.debug("Read excel sheet '${evidence_source}:${sheet_name_server}'")
    //                 def sheet_server = workbook.getSheet(sheet_name_server)
    //                 if (sheet_server) {
    //                     readSheetServer(sheet_server)
    //                 } else {
    //                     def msg = "Not found excel server list sheet '${sheet_name_server}'"
    //                     throw new IllegalArgumentException(msg)
    //                 }
    //             }
    //             // Read Excel test spec sheet.
    //             sheet_name_specs.each { platform, sheet_name_spec ->
    //                 log.debug("Read sheet '${sheet_name_spec}'")
    //                 def sheet_spec = workbook.getSheet(sheet_name_spec)
    //                 if (sheet_spec) {
    //                     readSheetSpec(platform, sheet_spec)
    //                 } else {
    //                     def msg = "Not found excel test spec sheet '${sheet_name_spec}'"
    //                     throw new IllegalArgumentException(msg)
    //                 }
    //             }
    //             // Read Excel verify rule sheet.
    //             log.debug("Read sheet '${sheet_name_rule}'")
    //             def sheet_rule = workbook.getSheet(sheet_name_rule)
    //             if (sheet_rule) {
    //                 readSheetRule(sheet_rule)
    //             } else {
    //                 def msg = "Not found excel server list sheet '${sheet_name_rule}'"
    //                 throw new IllegalArgumentException(msg)
    //             }
    //         }
    //     }
    //     // Set compare target server info. in test servers.
    //     this.test_servers.each { test_server ->
    //         def verify_id = test_server.infos['verify_id']
    //         if (verify_id) {
    //             def compare_server = this.verify_rules[verify_id]['_common']['compare_server']
    //             if (test_server.infos.containsKey('compare_server') &&
    //                 test_server.infos['compare_server'].size() > 0) {
    //                 compare_server = test_server.infos['compare_server']
    //             }
    //             def compare_source = this.verify_rules[verify_id]['_common']['compare_source']
    //             test_server.compare_server = compare_server
    //             test_server.compare_source = compare_source
    //             if (!compare_servers.containsKey(compare_server)) {
    //                 compare_servers[compare_server] = compare_source
    //             }
    //         }
    //     }
    //     long elapsed = System.currentTimeMillis() - start
    //     log.info "Load Sheet, Elapsed : ${elapsed} ms"
    // }

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
                // font.setColor(IndexedColors.BLACK.getIndex());
                println "RED:${IndexedColors.RED.getIndex()}"
                font.setColor(IndexedColors.RED.getIndex());
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

            case ResultCellStyle.WARNING :
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

            case ResultCellStyle.ERROR :
                XSSFFont font = wb.createFont();
                font.setBold(true);
                font.setColor(IndexedColors.RED.getIndex());
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

    // def writeNodeFile(String platform, String server_name, def node_config)
    //     throws IOException {
    //     def base_dir = "${staging_dir}/${node_dir_prefix}"
    //     def node_dir = "${base_dir}/${platform}"
    //     new File(node_dir).mkdirs()
    //     new File("${node_dir}/${server_name}.json").with {
    //         def json = JsonOutput.toJson(node_config)
    //         it.text = JsonOutput.prettyPrint(json)
    //     }
    //     new File("./build/.last_run").with {
    //         def run_config = ['node_dir' : base_dir, 'evidence' : evidence_target,
    //                           'target' : target_original, 'config_file' : config_file ]
    //         it.write( JsonOutput.toJson(run_config))
    //     }
    // }

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

    def addTestItemsToTargetSheet(String platform, test_items = [:])
        throws IOException {
        log.info("Add test items : platform = ${platform}")

        def inp = new FileInputStream(evidence_target)
        def wb  = WorkbookFactory.create(inp)
        def sheet_result = wb.getSheet(sheet_name_specs[platform])
        def cell_style = createBorderedStyle(wb)
        def last_row = 0
        sheet_result.with { sheet ->
            (row_body_begin .. sheet.getLastRowNum()).find { rownum ->
                last_row = rownum
                Row row = sheet.getRow(rownum)
                if (row == null)
                    return true

                def cell_test_id = row.getCell(1).getStringCellValue()
                def cell_domain  = row.getCell(3).getStringCellValue()
                if (cell_test_id.size() == 0 && cell_domain.size() == 0)
                    return true
            }
            last_row ++
            def row_index = 0
            test_items.each { test_id, test_item ->
                def rownum = last_row + row_index
                def row = sheet.createRow(rownum)
                (0..5).find { colnum ->
                    def cell = row.createCell(colnum)
                    cell.setCellStyle(cell_style)
                    if (colnum == 1) {
                        cell.setCellValue(test_id)
                    } else if (colnum == 2) {
                        cell.setCellValue(test_item?.test_name)
                    } else if (colnum == 3) {
                        cell.setCellValue(test_item?.domain)
                    } else if (colnum == 5) {
                        cell.setCellValue(test_item?.desc)
                    }
                }
                row_index ++
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

    def updateTestTargetSheet(server_infos = [:]) throws IOException {
        def inp = new FileInputStream(evidence_source)
        def wb  = WorkbookFactory.create(inp)
        def sheet_result = wb.getSheet(sheet_name_server)

        def server_infos_pivot = [:].withDefault{[]}
        server_infos.each {id, server_info ->
            server_info.each { info_name, info_value ->
                server_infos_pivot[info_name] << info_value
            }
        }
        sheet_result.with { sheet ->
            (row_header .. sheet.getLastRowNum()).find { rownum ->
                Row row = sheet.getRow(rownum)
                if (row == null)
                    return true
                (column_server_begin .. row.getLastCellNum()).each { colnum ->
                    Cell cell = row.getCell(colnum)
                    if (cell)
                        row.removeCell(cell)
                }
                // Register the header
                if (rownum == row_header) {
                    def colnum = column_server_begin
                    (1 .. server_infos.size()).each { server_no ->
                        def cell_result = row.createCell(colnum)
                        cell_result.setCellValue(server_no)
                        setTestResultCellStyle(cell_result, ResultCellStyle.NORMAL)
                        colnum ++
                    }
                    return
                }
                // Register the server info values
                def cell_inventory = "${row?.getCell(column_header)}"
                def server_info_pivot = server_infos_pivot[cell_inventory]
                if (server_info_pivot.size() > 0) {
                    def colnum = column_server_begin
                    server_info_pivot.each { server_info_value ->
                        def cell_result = row.createCell(colnum)
                        cell_result.setCellValue(server_info_value)
                        setTestResultCellStyle(cell_result, ResultCellStyle.NORMAL)
                        colnum ++
                    }
                // Register an empty line
                } else {
                    def colnum = column_server_begin
                    (1 .. server_infos.size()).each { id ->
                        def cell_result = row.createCell(colnum)
                        cell_result.setCellValue('')
                        setTestResultCellStyle(cell_result, ResultCellStyle.NORMAL)
                        colnum ++
                    }
                }
                return
            }
        }
        // Back up the inspection sheet. Copy to sheet file: "{filename}-backup.xlsx"
        def backup_file = evidence_source.replace(".xlsx", "-backup.xlsx")
        log.info "Backup: $backup_file"
        FileUtils.copyFile(new File(evidence_source), new File(backup_file))
        log.info "Update: $evidence_source"
        def fos = new FileOutputStream(evidence_source)
        wb.write(fos)
        fos.close()
    }

    def writeDeviceFile(String platform, String test_id, List headers, Map csvs)
        throws IOException, IllegalArgumentException {
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
        throws IOException, IllegalArgumentException {
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

package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.transform.AutoClone
import groovy.xml.MarkupBuilder
import groovy.util.ConfigObject
import groovy.util.logging.Slf4j
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.IndexedColors
// import com.gh.mygreen.xlsmapper.*
// import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

public enum SheetType {
    vertical,
    horizontal,
}

@AutoClone
@Slf4j
class SheetDesign extends SpecModel {
    String name
    SheetParser sheet_parser
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

abstract class SheetParser {
    String sheet_prefix    = ''
    int[] header_pos       = [0, 0]
    String[] header_checks = []

    abstract def get_sheet_body(Sheet sheet)
}

@Slf4j
class ExcelSheetParserHorizontal extends SheetParser {
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
class ExcelSheetParserVertical extends SheetParser {
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
                lines[target_id][header_name] = "${row.getCell(colnum)}"
            }
        }
        return lines
    }
}

@Slf4j
class ExcelParser {
    String excel_file
    def sheet_desings = []
    ConfigObject sheet_sources

    ExcelParser(excel_file) {
        this.sheet_sources = new ConfigObject()
        this.excel_file = excel_file
        this.sheet_desings = [
            new SheetDesign(name: 'target', 
                            sheet_parser : new ExcelSheetParserVertical(
                                header_pos: [4, 1], sheet_prefix: 'Target',
                                header_checks: ['domain'])),
            new SheetDesign(name: 'check_sheet',
                            sheet_parser : new ExcelSheetParserHorizontal(
                                header_pos: [3, 0], sheet_prefix: 'CheckSheet',
                                header_checks: ['Test', 'ID'])),
            new SheetDesign(name: 'check_rule',
                            sheet_parser : new ExcelSheetParserVertical(
                                header_pos: [4, 1], sheet_prefix: 'Rule',
                                header_checks: ['name', 'compare_server'])),
        ]
    }

    def scan_sheet() throws IOException {
        new FileInputStream(this.excel_file).withStream { ins ->
            WorkbookFactory.create(ins).with { wb ->
                Iterator<Sheet> sheets = wb.sheetIterator()
                while (sheets.hasNext()) {
                    def sheet = sheets.next()
                    def sheet_design = this.make_sheet_design(sheet)
                    if (sheet_design) {
                        if (sheet_design.name == 'check_sheet') {
                            def domain_name = sheet_design.domain_name
                            this.sheet_sources.check_sheet."$domain_name" = sheet_design
                        } else {
                            this.sheet_sources."${sheet_design.name}" = sheet_design
                        }
                    } else {
                        log.warn "Unkown sheet, skip : ${sheet.getSheetName()}"
                    }
                }
            }
        }
    }

    SheetDesign make_sheet_design(Sheet sheet) {
        String sheet_name = sheet.getSheetName()
        log.info "Attach sheet : '${sheet_name}'"
        String domain_name = null
        ( sheet_name =~ /^(.+)[\(](.*)[\)]$/ ).each { m0, postfix, suffix ->
            sheet_name  = postfix
            domain_name = suffix
        }
        
        SheetDesign current_sheet = null
        this.sheet_desings.each { sheet_design ->
            if (sheet_name == sheet_design.sheet_parser.sheet_prefix) {
                current_sheet = sheet_design.create(sheet, domain_name)
                return true
            }
        }
        return current_sheet
    }

    def visit_test_scenario(test_scenario) {
        log.info "Parse spec sheet"
        test_scenario.with {
            test_targets = new TestTargetSet(name: 'root')
            test_targets.accept(this)
            test_rules = new TestRuleSet(name: 'root')
            test_rules.accept(this)
            this.sheet_sources.check_sheet.each { domain_name, check_sheet ->
                def domain_template = new TestDomainTemplate(name: domain_name)
                domain_template.accept(this)
                test_domain_templates[domain_name] = domain_template
            }

        }
    }

    def visit_check_sheet(check_sheet) {
        def domain_name = check_sheet.name
        def source = this.sheet_sources.check_sheet."$domain_name"
        def lines = source.get()
        lines.find { line ->
            def id = line['ID']
            if (!id)
                return true
            def test_metric = new TestMetric(id: id, name: line['項目'], 
                                             enabled: line['Test'], 
                                             device_enabled: line['デバイス'])
            check_sheet.test_metrics[id] = test_metric
            return
        }
        log.info "Read test(${domain_name}) : ${check_sheet.test_metrics.size()} row"
    }

    def visit_test_target(test_target_set) {
        def lines = this.sheet_sources.target.get()
        lines.find { line ->
            if (!line['domain'])
                return true
            line['name'] = line['server_name']
            def test_target = new TestTarget(line)
            test_target_set.add(test_target)
            return
        }
        log.info "Read target : ${test_target_set.get_all().size()} row"
    }

    def visit_test_rule(test_rule_set) {
        def lines = this.sheet_sources.check_rule.get() as Queue
        def line_num = 0
        def platforms = [:]
        def platform_line = lines.poll()
        platform_line.each { key, value ->
            if (value)
                platforms[key] = value
        }
        lines.find { line ->
            if (!line['name'])
                return true
            def params = new ConfigObject()
            line.each { key, value ->
                if (!value)
                    return
                def platform = platforms[key]
                if (platform) {
                    params.config[platform][key] = value
                } else {
                    params[key] = value
                }
            }
            def test_rule = new TestRule(params)
            test_rule_set.add(test_rule)
            return
        }
        log.info "Read rule : ${test_rule_set.get_all().size()} row"
    }

}

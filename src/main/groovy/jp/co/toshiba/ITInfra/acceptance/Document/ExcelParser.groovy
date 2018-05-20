package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.transform.AutoClone
import groovy.xml.MarkupBuilder
import groovy.util.ConfigObject
import groovy.util.logging.Slf4j
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.IndexedColors
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
class ExcelParser {
    String excel_file
    def sheet_desings = []
    ConfigObject sheet_sources
    Workbook workbook

    ExcelParser(excel_file) {
        this.sheet_sources = new ConfigObject()
        this.excel_file = excel_file
        this.sheet_desings = [
            new SheetDesign(name: 'target', 
                            sheet_parser : new ExcelSheetParserHorizontal(
                                header_pos: [1, 0], sheet_prefix: 'Target',
                                header_checks: ['#', 'domain'])),
            new SheetDesign(name: 'check_sheet',
                            sheet_parser : new ExcelSheetParserHorizontal(
                                header_pos: [3, 0], sheet_prefix: 'CheckSheet',
                                header_checks: ['Test', 'ID'],
                                result_pos: [3, 6])),
            new SheetDesign(name: 'check_rule',
                            sheet_parser : new ExcelSheetParserVertical(
                                header_pos: [4, 1], sheet_prefix: 'Rule',
                                header_checks: ['name', 'compare_server'])),
            new SheetDesign(name: 'template',
                            sheet_parser : new ExcelSheetParserVertical(
                                header_pos: [0, 0], sheet_prefix: 'Template',
                                header_checks: ['Platform'])),
        ]
    }

    def scan_sheet() throws IOException {
        log.info "Open excel sheet : '${this.excel_file}'"
        new FileInputStream(this.excel_file).withStream { ins ->
            WorkbookFactory.create(ins).with { wb ->
                this.workbook = wb
                Iterator<Sheet> sheets = wb.sheetIterator()
                while (sheets.hasNext()) {
                    def sheet = sheets.next()
                    def sheet_design = this.make_sheet_design(sheet)
                    if (sheet_design) {
                        if (sheet_design.name == 'check_sheet' ||
                            sheet_design.name == 'template') {
                            def sheet_name = sheet_design.name
                            def domain_name = sheet_design.domain_name
                            this.sheet_sources."$sheet_name"."$domain_name" = sheet_design
                        } else {
                            this.sheet_sources."${sheet_design.name}" = sheet_design
                        }
                    } else {
                        log.warn "Unkown sheet name, skip : '${sheet.getSheetName()}'"
                    }
                }
            }
        }
    }

    SheetDesign make_sheet_design(Sheet sheet) {
        String sheet_name = sheet.getSheetName()
        String sheet_prefix = sheet_name
        String domain_name = null
        ( sheet_prefix =~ /^(.+)[\(](.*)[\)]$/ ).each { m0, postfix, suffix ->
            sheet_prefix  = postfix
            domain_name = suffix
        }
        SheetDesign current_sheet = null
        this.sheet_desings.each { sheet_design ->
            if (sheet_prefix == sheet_design.sheet_parser.sheet_prefix) {
                current_sheet = sheet_design.create(sheet, domain_name)
                log.info "Attach sheet '${sheet_name}'"
                return true
            }
        }
        return current_sheet
    }

    def make_template_link(TestTarget test_target, TestScenario test_scenario) {
        test_target.with {
            def template = test_scenario.test_templates.get(template_id)
            if (template) {
                it.test_templates[template_id] = template
            }
        }
    }

    def make_template_links(TestScenario test_scenario) {
        def all_targets = test_scenario.test_targets.get_all()
        all_targets.each { target_name, domain_targets ->
            domain_targets.each { domain_name, target ->
                this.make_template_link(target, test_scenario)
            }
        }
    }

    def visit_test_scenario(test_scenario) {
        log.info "Parse spec sheet"
        test_scenario.with {
            test_targets = new TestTargetSet(name: 'root')
            test_targets.accept(this)
            test_rules = new TestRuleSet(name: 'root')
            test_rules.accept(this)
            test_metrics = new TestMetricSet(name: 'root')
            this.sheet_sources.check_sheet.each { domain_name, check_sheet ->
                def check_sheet_metrics = new TestMetricSet(name: domain_name)
                check_sheet_metrics.accept(this)
                test_metrics.add(check_sheet_metrics)
            }
            test_templates = new TestTemplateSet(name: 'root')
            this.sheet_sources.template.each { template_name, template_sheet ->
                def test_template = new TestTemplate(name: template_name)
                test_template.accept(this)
                test_templates.add(test_template)
            }
        }
        make_template_links(test_scenario)
    }

    def visit_test_metric_set(test_metric_set) {
        def domain_name = test_metric_set.name
        def sheet_design = this.sheet_sources.check_sheet."$domain_name"
        def lines = sheet_design.get()
        def platform_tests = [:].withDefault{[:]}
        def sheet_row = 0
        lines.find { line ->
            sheet_row ++
            def id = line['ID']
            def platform = line['分類']
            sheet_design.sheet_row[[platform, id]] = sheet_row
            if (!id && !platform)
                return true
            def test_metric = new TestMetric(name: id, description: line['項目'], 
                                             platform: platform,
                                             enabled: line['Test'], 
                                             device_enabled: line['デバイス'])
            platform_tests[platform][id] = test_metric
            return
        }
        platform_tests.each { platform, platform_test ->
            def platform_test_set = new TestMetricSet(name: platform)
            test_metric_set.add(platform_test_set)
            platform_test.each { name, test_metric ->
                platform_test_set.add(test_metric)
            }
        }
        log.info "Read test spec(${domain_name}) : ${test_metric_set.count()} row"
    }

    def visit_test_target_set(test_target_set) {
        def lines = this.sheet_sources.target.get()
        lines.find { line ->
            if (!line['domain'])
                return true
            line['name'] = line['server_name']
            if (!line['remote_alias'])
                line['remote_alias'] = line['server_name']
            // line << [name: line['server_name']]
            def test_target = new TestTarget(line)
            test_target_set.add(test_target)
            return
        }
        log.info "Read target : ${test_target_set.get_all().size()} row"
    }

    def visit_test_template(test_template) {
        def template_name = test_template.name
        def source = this.sheet_sources.template."$template_name"
        def lines = source.get()

        def template_values = new ConfigObject()
        def row_count = 0
        lines.find { line ->
            def metric_name = line['#']
            def platform = line['Platform']
            if (!metric_name && !platform)
                return true
            def values = []
            line.each { row_id, value ->
                if (!row_id.isDouble() || value == null)
                    return
                values << value
            }
            row_count ++
            if (values.size() == 1)
                template_values[platform][metric_name] = values[0]
            else if (values.size() >= 2)
                template_values[platform][metric_name] = values
            return
        }
        test_template.values = template_values

        log.info "Read target template($template_name) : ${row_count} row"
    }

    def visit_test_rule_set(test_rule_set) {
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

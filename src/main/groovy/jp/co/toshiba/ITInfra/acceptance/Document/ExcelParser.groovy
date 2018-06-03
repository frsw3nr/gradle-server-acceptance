package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.transform.AutoClone
import groovy.xml.MarkupBuilder
import groovy.util.ConfigObject
import groovy.util.logging.Slf4j
// import org.apache.poi.ss.usermodel.*
// import org.apache.poi.xssf.usermodel.*
// import org.apache.poi.hssf.usermodel.HSSFWorkbook
// import org.apache.poi.ss.usermodel.IndexedColors

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
                                header_pos: [2, 0], sheet_prefix: '検査対象',
                                header_checks: ['#', 'domain'])),
            new SheetDesign(name: 'check_sheet',
                            sheet_parser : new ExcelSheetParserHorizontal(
                                header_pos: [3, 0], sheet_prefix: 'チェックシート',
                                header_checks: ['test', 'id'],
                                result_pos: [3, 6])),
            // new SheetDesign(name: 'check_rule',
            //                 sheet_parser : new ExcelSheetParserVertical(
            //                     header_pos: [4, 1], sheet_prefix: 'Rule',
            //                     header_checks: ['name', 'compare_server'])),
            new SheetDesign(name: 'template',
                            sheet_parser : new ExcelSheetParserVertical(
                                header_pos: [0, 0], sheet_prefix: 'テンプレート',
                                header_checks: ['platform'])),
        ]
    }

    def scan_sheet() throws IOException {
        long start = System.currentTimeMillis()
        log.info "Open excel sheet : '${this.excel_file}'"
        def attached_sheets = [:].withDefault{[]}
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
                            attached_sheets[sheet_design.name] << domain_name
                        } else {
                            this.sheet_sources."${sheet_design.name}" = sheet_design
                            attached_sheets[sheet_design.name] << sheet.getSheetName()
                        }
                    } else {
                        log.warn "Unkown sheet name, skip : '${sheet.getSheetName()}'"
                    }
                }
            }
        }
        attached_sheets.each { design_name, sheet_names ->
            log.info "Attach[${design_name}] : ${sheet_names}"
        }
        long elapsed = System.currentTimeMillis() - start
        log.info "Finish excel sheet scan, Elapsed : ${elapsed} ms"
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
        log.debug "Parse spec sheet"
        test_scenario.with {
            test_targets = new TestTargetSet(name: 'root')
            test_targets.accept(this)
            // test_rules = new TestRuleSet(name: 'root')
            // test_rules.accept(this)
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
            def id = line['id']
            def platform = line['分類']
            sheet_design.sheet_row[[platform, id]] = sheet_row
            if (!id && !platform)
                return
            def test_metric = new TestMetric(name: id, description: line['項目'], 
                                             platform: platform,
                                             enabled: line['test'], 
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

        log.debug "Read test spec(${domain_name}) : ${test_metric_set.count()} row"
    }

    def visit_test_target_set(test_target_set) {
        def lines = this.sheet_sources.target.get()
        lines.find { line ->
            if (!line['domain'])
                return true
            line['name'] = line['server_name']
            // if (!line['remote_alias'])
            //     line['remote_alias'] = line['server_name']
            def test_target = new TestTarget(line)
            test_target_set.add(test_target)
            return
        }
        log.debug "Read target : ${test_target_set.get_all().size()} row"
    }

    def trim(String value) {
        return value.replaceAll(/\A[\s　]+/,"").replaceAll(/[\s　]+\z/,"")
    }

    def visit_test_template(test_template) {
        def template_name = test_template.name
        def source = this.sheet_sources.template."$template_name"
        def lines = source.get()

        def template_values = new ConfigObject()
        def row_count = 0
        def keys_index = new ConfigObject()
        lines.find { line ->
            def metric_name = line['#']
            if (!metric_name)
                return
            metric_name = metric_name.toLowerCase()
            def metric_type = 'unit'
            (metric_name =~ /(.+):(.+)/).each { m0, m1, m2 ->
                metric_name = m1
                metric_type = m2
            }
            def platform = line['platform']
            if (!metric_name && !platform)
                return true
            def values = []
            line.each { row_id, value ->
                if (!row_id.isDouble() || value == null)
                    return
                def trim_value = trim(value)
                values << trim_value
            }
            def rownum = 0
            switch(metric_type) {
                case 'unit':
                template_values[platform][metric_name] = values[0]
                break

                case ['key','k']:
                values.each { key_name ->
                    template_values[platform][metric_name][key_name] = 1
                    keys_index[platform][metric_name][rownum] = key_name
                    rownum ++
                }
                break

                case ['value','v']:
                values.each { value ->
                    def key_name = keys_index[platform][metric_name][rownum]
                    template_values[platform][metric_name][key_name] = value
                    rownum ++
                }
                break

                default:
                log.warn "Unkown template key type : $template_name, $metric_type"
                break
            }
            row_count ++
            return
        }
        test_template.values = template_values
        log.debug "Read target template($template_name) : ${row_count} row"
    }

    // def visit_test_rule_set(test_rule_set) {
    //     def lines = this.sheet_sources.check_rule.get() as Queue
    //     def line_num = 0
    //     def platforms = [:]
    //     def platform_line = lines.poll()
    //     platform_line.each { key, value ->
    //         if (value)
    //             platforms[key] = value
    //     }
    //     lines.find { line ->
    //         if (!line['name'])
    //             return true
    //         def params = new ConfigObject()
    //         line.each { key, value ->
    //             if (!value)
    //                 return
    //             def platform = platforms[key]
    //             if (platform) {
    //                 params.config[platform][key] = value
    //             } else {
    //                 params[key] = value
    //             }
    //         }
    //         def test_rule = new TestRule(params)
    //         test_rule_set.add(test_rule)
    //         return
    //     }
    //     log.debug "Read rule : ${test_rule_set.get_all().size()} row"
    // }
}

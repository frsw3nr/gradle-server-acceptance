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

enum EvidenceMakerCommand {
  OUTPUT_JSON, OUTPUT_EXCEL, READ_JSON
}

@Slf4j
@ToString(includePackage = false)
class EvidenceMaker {
    EvidenceMakerCommand command = EvidenceMakerCommand.OUTPUT_EXCEL
    String evidence_source
    String evidence_target
    String json_dir
    ExcelParser excel_parser
    ConfigObject evidence_sheet
    ConfigObject device_sheet

    def write_platform_result_to_json(String target_name, String platform_name, 
                                      TestPlatform test_platform) throws IOException {
        def output_dir = "${json_dir}/${target_name}"
        new File(output_dir).mkdirs()
        new File("${output_dir}/${platform_name}.json").with {
            def json = JsonOutput.toJson(test_platform.test_results)
            it.text = JsonOutput.prettyPrint(json)
        }
    }

    def output_results_to_json(test_scenario) {
        def targets = test_scenario.test_targets.get_all()

        targets.each { target, domain_targets ->
            domain_targets.each { domain, test_target ->
                test_target.test_platforms.each { platform, test_platform ->
                    write_platform_result_to_json(target, platform, test_platform)
                }
            }
        }
    }

    def convert_to_result_status(String status) {
        def status_hash = [
            'OK'      : ResultStatus.OK,
            'NG'      : ResultStatus.NG,
            'WARNING' : ResultStatus.WARNING,
            'MATCH'   : ResultStatus.MATCH,
            'UNMATCH' : ResultStatus.UNMATCH,
            'UNKOWN'  : ResultStatus.UNKOWN,
        ]
        return(status_hash[status])
    }

    def read_platform_result_from_json(String target_name, String platform_name) 
                                       throws IOException {
        def json_file = new File("${json_dir}/${target_name}/${platform_name}.json")
        if(!json_file.exists())
            return
        def results_json = new JsonSlurper().parseText(json_file.text)
        def test_platform = new TestPlatform(name: platform_name, 
                                             test_results: results_json,
                                             )
        test_platform.test_results.each { metric_name, test_result ->
            test_result.status = convert_to_result_status(test_result.status)
            test_result.verify = convert_to_result_status(test_result.verify)
        }
        return test_platform
    }

    def read_results_from_json(test_scenario) {
        def domain_metrics = test_scenario.test_metrics.get_all()
        def targets = test_scenario.test_targets.get_all()

        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                def platform_metrics = domain_metrics[domain].get_all()
                platform_metrics.each { platform_name, platform_metric ->
                    def test_platform = read_platform_result_from_json(target_name,
                                                                       platform_name)
                    if (test_platform) {
                        test_platform.test_target = test_target
                        test_target.test_platforms[platform_name] = test_platform
                    }
                }
            }
        }
    }

    def visit_test_scenario(test_scenario) {
        long start = System.currentTimeMillis()
        switch (this.command) {
            case EvidenceMakerCommand.OUTPUT_JSON:
                output_results_to_json(test_scenario)
                break;

            case EvidenceMakerCommand.OUTPUT_EXCEL:
                println 'OUTPUT_EXCEL'
                break;

            case EvidenceMakerCommand.READ_JSON:
                println 'READ_JSON'
                read_results_from_json(test_scenario)
                break;

            default :
                println 'EvidenceMaker : Other command'
                break
        }
        long elapse = System.currentTimeMillis() - start
        log.info "Finish command '${this.command}', Elapse : ${elapse} ms"
    }

    def visit_test_platform(test_platform) {
        log.info "visit_test_platform : ${test_platform.name}"
        // this.platform_tester.run(test_platform)
        // def test_domain_template = test_domain_templates[]
    }

    def make_results() {
        
    }

    def make_device_results() {
        
    }

    def update_sheet_results() {
        
    }

    def update_sheet_device_results() {
        
    }

    // EvidenceMaker(excel_file) {
    //     this.sheet_sources = new ConfigObject()
    //     this.excel_file = excel_file
    //     this.sheet_desings = [
    //         new SheetDesign(name: 'target', 
    //                         sheet_parser : new ExcelSheetParserVertical(
    //                             header_pos: [4, 1], sheet_prefix: 'Target',
    //                             header_checks: ['domain'])),
    //         new SheetDesign(name: 'check_sheet',
    //                         sheet_parser : new ExcelSheetParserHorizontal(
    //                             header_pos: [3, 0], sheet_prefix: 'CheckSheet',
    //                             header_checks: ['Test', 'ID'])),
    //         new SheetDesign(name: 'check_rule',
    //                         sheet_parser : new ExcelSheetParserVertical(
    //                             header_pos: [4, 1], sheet_prefix: 'Rule',
    //                             header_checks: ['name', 'compare_server'])),
    //     ]
    // }

    // def scan_sheet() throws IOException {
    //     log.info "Open excel sheet : '${this.excel_file}'"
    //     new FileInputStream(this.excel_file).withStream { ins ->
    //         WorkbookFactory.create(ins).with { wb ->
    //             Iterator<Sheet> sheets = wb.sheetIterator()
    //             while (sheets.hasNext()) {
    //                 def sheet = sheets.next()
    //                 def sheet_design = this.make_sheet_design(sheet)
    //                 if (sheet_design) {
    //                     if (sheet_design.name == 'check_sheet') {
    //                         def domain_name = sheet_design.domain_name
    //                         this.sheet_sources.check_sheet."$domain_name" = sheet_design
    //                     } else {
    //                         this.sheet_sources."${sheet_design.name}" = sheet_design
    //                     }
    //                 } else {
    //                     log.warn "Unkown sheet, skip : ${sheet.getSheetName()}"
    //                 }
    //             }
    //         }
    //     }
    // }

    // SheetDesign make_sheet_design(Sheet sheet) {
    //     String sheet_name = sheet.getSheetName()
    //     log.info "Attach sheet : '${sheet_name}'"
    //     String domain_name = null
    //     ( sheet_name =~ /^(.+)[\(](.*)[\)]$/ ).each { m0, postfix, suffix ->
    //         sheet_name  = postfix
    //         domain_name = suffix
    //     }
    //     SheetDesign current_sheet = null
    //     this.sheet_desings.each { sheet_design ->
    //         if (sheet_name == sheet_design.sheet_parser.sheet_prefix) {
    //             current_sheet = sheet_design.create(sheet, domain_name)
    //             return true
    //         }
    //     }
    //     return current_sheet
    // }

    // def visit_test_scenario(test_scenario) {
    //     log.info "Parse spec sheet"
    //     test_scenario.with {
    //         test_targets = new TestTargetSet(name: 'root')
    //         test_targets.accept(this)
    //         test_rules = new TestRuleSet(name: 'root')
    //         test_rules.accept(this)
    //         test_metrics = new TestMetricSet(name: 'root')
    //         this.sheet_sources.check_sheet.each { domain_name, check_sheet ->
    //             def check_sheet_metrics = new TestMetricSet(name: domain_name)
    //             check_sheet_metrics.accept(this)
    //             test_metrics.add(check_sheet_metrics)
    //         }
    //     }
    // }

    // def visit_test_metric_set(test_metric_set) {
    //     def domain_name = test_metric_set.name
    //     def source = this.sheet_sources.check_sheet."$domain_name"
    //     def lines = source.get()
    //     def platform_tests = [:].withDefault{[:]}
    //     lines.find { line ->
    //         def id = line['ID']
    //         def platform = line['分類']
    //         if (!id && !platform)
    //             return true
    //         def test_metric = new TestMetric(name: id, description: line['項目'], 
    //                                          platform: platform,
    //                                          enabled: line['Test'], 
    //                                          device_enabled: line['デバイス'])
    //         platform_tests[platform][id] = test_metric
    //         return
    //     }
    //     platform_tests.each { platform, platform_test ->
    //         def platform_test_set = new TestMetricSet(name: platform)
    //         test_metric_set.add(platform_test_set)
    //         platform_test.each { name, test_metric ->
    //             platform_test_set.add(test_metric)
    //         }
    //     }
    //     log.info "Read test(${domain_name}) : ${test_metric_set.count()} row"
    // }

    // def visit_test_target_set(test_target_set) {
    //     def lines = this.sheet_sources.target.get()
    //     lines.find { line ->
    //         if (!line['domain'])
    //             return true
    //         line['name'] = line['server_name']
    //         def test_target = new TestTarget(line)
    //         test_target_set.add(test_target)
    //         return
    //     }
    //     log.info "Read target : ${test_target_set.get_all().size()} row"
    // }

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
    //     log.info "Read rule : ${test_rule_set.get_all().size()} row"
    // }
}

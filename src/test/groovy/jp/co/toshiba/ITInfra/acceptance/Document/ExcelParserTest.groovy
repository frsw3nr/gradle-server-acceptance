import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import org.apache.poi.ss.usermodel.*
import spock.lang.Specification

// import org.apache.poi.ss.usermodel.*
// import org.apache.poi.ss.usermodel.IndexedColors
// import org.apache.poi.xssf.usermodel.*
// import org.apache.poi.hssf.usermodel.HSSFWorkbook

// gradle --daemon test --tests "ExcelParserTest.サマリシート更新"

class ExcelParserTest extends Specification {

    def "初期化"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')

        then:
        1 == 1
    }

    def "シート読み込み"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        excel_parser.sheet_sources.check_sheet.each { domain_name, sheet->
            println "Domain:$domain_name"
        }

        then:
        excel_parser.sheet_sources.keySet() as List == ['target', 'report', 'error_report',
                                                        'check_sheet', 'template']
        excel_parser.sheet_sources.check_sheet.keySet() as List == ['Linux', 'Windows']
    }

    def "チェックシートパース"() {
        setup:
        def domains = ['Linux', 'Windows']
        def test_metric_sets = [:]

        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        domains.each { domain ->
            def source = excel_parser.sheet_sources.check_sheet."$domain"
            test_metric_sets[domain] = new TestMetricSet(name: domain)
            test_metric_sets[domain].accept(excel_parser)
        }

        then:
        domains.each { domain ->
            test_metric_sets[domain].name == domain
            test_metric_sets[domain].count() > 0

            def metrics = test_metric_sets[domain].get_all()
            println "METRICS: ${domain}"
            println metrics
            // def json = new groovy.json.JsonBuilder()
            // json(metrics)
            // println json.toPrettyString()
        }
    }

    def "チェックシートパース2"() {
        setup:
        def domain = 'Linux'
        def test_metric_sets = [:]

        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def source = excel_parser.sheet_sources.check_sheet."$domain"
        test_metric_sets[domain] = new TestMetricSet(name: domain)
        test_metric_sets[domain].accept(excel_parser)

        then:
        test_metric_sets[domain].name == domain
        test_metric_sets[domain].count() > 0

        def metrics = test_metric_sets[domain].get_all()
        println "METRICS: ${domain}"
        println metrics
        // def json = new groovy.json.JsonBuilder()
        // json(metrics)
        // println json.toPrettyString()
    }

    def "検査対象パース"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def target_set = new TestTargetSet(name: 'root')
        target_set.accept(excel_parser)
        def test_targets = target_set.get_all()
        def json = new groovy.json.JsonBuilder()
        json(test_targets)
        println json.toPrettyString()

        then:
        1 == 1
        // test_targets['ostrich'].Linux.verify_id   == 'RuleAP'
    }

    def "テンプレートパース"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def template = new TestTemplate(name: 'Linux')
        template.accept(excel_parser)
        println template.values
        def json = new groovy.json.JsonBuilder()
        json(template.values)
        println json.toPrettyString()
        
        then:
        1 == 1
        // test_targets['ostrich'].Linux.verify_id   == 'RuleAP'
    }

    def "レポートパース"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet_rep.xlsx')
        excel_parser.scan_sheet()
        def report_set = new TestReportSet(name: 'root')
        report_set.accept(excel_parser)
        // println report_set.values
        def json = new groovy.json.JsonBuilder()
        json(report_set)
        println json.toPrettyString()
        
        then:
        1 == 1
        // test_targets['ostrich'].Linux.verify_id   == 'RuleAP'
    }

    def "エラーレポートパース"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet_rep.xlsx')
        excel_parser.scan_sheet()
        def error_report_set = new TestErrorReportSet(name: 'root')
        error_report_set.accept(excel_parser)
        // println report_set.values
        def json = new groovy.json.JsonBuilder()
        json(error_report_set)
        println json.toPrettyString()
        
        then:
        1 == 1
        // test_targets['ostrich'].Linux.verify_id   == 'RuleAP'
    }

    // def "ルール定義パース"() {
    //     when:
    //     def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
    //     excel_parser.scan_sheet()
    //     def rule_set = new TestRuleSet(name: 'root')
    //     rule_set.accept(excel_parser)
    //     def test_rules = rule_set.get_all()

    //     then:
    //     test_rules.size() == 2
    //     def result_AP = test_rules['RuleAP'].config.vCenter.NumCpu
    //     result_AP == "x == NumberUtils.toDouble(server_info['NumCpu'])"
    //     test_rules['RuleAP'].config.vCenter.Cluster.size() == 0
    // }

    def "シート全体パース"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'OS情報採取')
        test_scenario.accept(excel_parser)
        def test_domains   = test_scenario.test_metrics.get_all()
        def test_targets   = test_scenario.test_targets.get_all()
        def test_reports   = test_scenario.test_reports.get_all()
        def test_templates = test_scenario.test_templates.get_all()

        def result_platform_keys = [:]
        test_domains.each { domain, test_domain ->
            def platform_metrics = test_domain.get_all()
            platform_metrics.each { platform, platform_metric ->
                result_platform_keys[domain, platform] = platform_metric.count()
            }
        }
        // println result_platform_keys
        println test_templates['AP']
        println test_reports

        then:
        test_targets.size() >= 3
        result_platform_keys.size() > 0
    }

    def "サマリシート更新"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        when:
        // def workbook = excel_parser.workbook
        excel_parser.sheet_sources['check_sheet'].each { domain, sheet_design ->
            println "domain:$domain" 
            sheet_design.sheet.with { sheet ->
                BorderStyle thin = BorderStyle.THIN;
                def black = IndexedColors.BLACK.getIndex();

                // def sheet = wb.getSheet('CheckSheet(Linux)')
                Row row = sheet.getRow(4)
                Cell cell = row.createCell(6)

                def workbook = cell.getRow().getSheet().getWorkbook();
                cell.setCellValue('TEST1')
                def style = workbook.createCellStyle();
                // style.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
                // style.setFillBackgroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                // Set Boder line
                style.setBorderRight(thin);
                style.setRightBorderColor(black);
                style.setBorderBottom(thin);
                style.setBottomBorderColor(black);
                style.setBorderTop(thin);
                style.setTopBorderColor(black);

                def font = workbook.createFont();
                font.setBold(true);
                // font.setColor(IndexedColors.LIGHT_GREEN.getIndex());
                font.setColor(IndexedColors.ROYAL_BLUE.getIndex());
                style.setFont(font);

                cell.setCellStyle(style);

                def fos = new FileOutputStream('build/check_sheet.xlsx')
                workbook.write(fos)
                fos.close()
            }
        }

        then:
        1 == 1
    }

    def "サマリシートセル更新"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
        def sheet_maker = new ExcelSheetMaker(excel_parser: excel_parser)

        when:
        // def workbook = excel_parser.workbook
        excel_parser.sheet_sources['check_sheet'].each { domain, sheet_design ->
            println "domain:$domain" 
            sheet_design.sheet.with { sheet ->
                def rownum = 0
                ResultCellStyle.values().each { cell_style ->
                    println "CELL_STYLE : ${cell_style}"
                    Row row = sheet.getRow(3 + rownum)
                    def cell = row.createCell(6)
                    cell.setCellValue("${cell_style}")
                    sheet_maker.set_test_result_cell_style(cell, cell_style)
                    rownum ++
                }

                def fos = new FileOutputStream('build/check_sheet.xlsx')
                workbook.write(fos)
                fos.close()
            }
        }

        then:
        1 == 1
    }

    def "サマリシート再読み込み後更新"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        when:
        new FileInputStream('src/test/resources/check_sheet.xlsx').withStream { ins ->
            WorkbookFactory.create(ins).with { wb ->
                println "Class wb: ${wb.getClass()}"
                BorderStyle thin = BorderStyle.THIN;
                def black = IndexedColors.BLACK.getIndex();

                def sheet = wb.getSheetAt(1)
                Row row = sheet.getRow(5)
                // Cell cell = row.createCell(6)

                // Aqua background
                CellStyle style = wb.createCellStyle();
                style.setFillBackgroundColor(IndexedColors.AQUA.getIndex());
                style.setFillPattern(FillPatternType.BIG_SPOTS);
                Cell cell = row.createCell(6);
                cell.setCellValue("TEST1");
                cell.setCellStyle(style);

                def fos = new FileOutputStream('build/check_sheet.xlsx')
                wb.write(fos)
                fos.close()
            }
        }

        then:
        1 == 1
    }

    def "サマリシート更新2"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/サーバチェックシート.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        def test_result_reader = new TestResultReader(
                                         result_dir: 'src/test/resources/json')
        test_result_reader. read_entire_result(test_scenario)
        def domain_targets = test_scenario.get_domain_targets()
        // 検査対象のステータスを強制的に FINISH に変更
        domain_targets.each { domain, domain_target ->
            domain_target.each { target, test_target ->
                test_target.target_status = RunStatus.FINISH
            }
        }

        when:
        def data_comparator = new DataComparator()
        test_scenario.accept(data_comparator)
        def evidence_maker = new EvidenceMaker()
        test_scenario.accept(evidence_maker)
        def report_maker = new ReportMaker()
        ConfigTestEnvironment.instance.accept(report_maker)
        test_scenario.accept(report_maker)
        def excel_sheet_maker = new ExcelSheetMaker(
                                    excel_parser: excel_parser,
                                    evidence_maker: evidence_maker,
                                    report_maker: report_maker)
        excel_sheet_maker.output('build/check_sheet2.xlsx')

        then:
        1 == 1
    }

    def "デバイスシート更新"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        def test_result_reader = new TestResultReader(
                                         result_dir: 'src/test/resources/json')
        test_result_reader. read_entire_result(test_scenario)

        def evidence_maker = new EvidenceMaker()
        test_scenario.accept(evidence_maker)
        
        when:
        def evidence_target = 'build/check_sheet.xlsx'

        evidence_maker.device_result_sheets.each { sheet_key, device_result_sheet ->
            def platform = sheet_key[0]
            def metric   = sheet_key[1]
            def device_sheet_name = "${platform}_${metric}"
            def device_sheet = excel_parser.workbook.createSheet(device_sheet_name)

            def rownum = 0
            device_sheet.with { sheet ->
                device_result_sheet.results.each { target, test_result ->
                    def header = test_result?.devices?.header
                    def csv    = test_result?.devices?.csv

                    if (header == null || csv == null)
                        return
                    if (rownum == 0) {
                        Row header_row = sheet.createRow(rownum)
                        def colnum = 0
                        header.each { header_name ->
                            header_row.createCell(colnum).setCellValue(header_name)
                            sheet.setColumnWidth(colnum, 6000)
                            colnum ++
                        }
                        rownum ++
                    }
                    csv.each { csv_values ->
                        // println "csv_values : ${csv_values}"
                        Row row = sheet.createRow(rownum)
                        def colnum = 0
                        csv_values.each { csv_value ->
                            row.createCell(colnum).setCellValue(csv_value)
                            sheet.setColumnWidth(colnum, 6000)
                            colnum ++
                        }
                        rownum ++
                    }
                }
            }
            println "sheet_key : ${sheet_key}"
            println "results : ${device_result_sheet.results}"
        }
        def fos = new FileOutputStream(evidence_target)
        excel_parser.workbook.write(fos)
        fos.close()

        then:
        1 == 1
    }
}

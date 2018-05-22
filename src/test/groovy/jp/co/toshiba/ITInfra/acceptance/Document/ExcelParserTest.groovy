import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*

// import org.apache.poi.ss.usermodel.*
// import org.apache.poi.ss.usermodel.IndexedColors
// import org.apache.poi.xssf.usermodel.*
// import org.apache.poi.hssf.usermodel.HSSFWorkbook

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


// gradle --daemon test --tests "ExcelParserTest.デバイスシート更新"

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
        excel_parser.sheet_sources.keySet() as List == ['target', 'check_sheet', 'template']
        excel_parser.sheet_sources.check_sheet.keySet() as List == ['Linux', 'Windows', 'VMHost']
    }

    def "チェックシートパース"() {
        setup:
        def domains = ['Linux', 'Windows', 'VMHost']
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

            println "DOMAIN:$domain"
            def metrics = test_metric_sets[domain].get_all()
            def json = new groovy.json.JsonBuilder()
            json(metrics)
            println json.toPrettyString()
        }
    }

    def "検査対象パース"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def target_set = new TestTargetSet(name: 'root')
        target_set.accept(excel_parser)
        def test_targets = target_set.get_all()
        println test_targets
        
        then:
        1 == 1
        // test_targets['ostrich'].Linux.verify_id   == 'RuleAP'
    }

    def "テンプレートパース"() {
        when:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def template = new TestTemplate(name: 'Win')
        template.accept(excel_parser)
        println template.values
        def json = new groovy.json.JsonBuilder()
        json(template.values)
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
        // def test_rules     = test_scenario.test_rules.get_all()
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

        then:
        test_targets.size() == 2
        // test_rules.size() == 2
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

                def sheet = wb.getSheet('CheckSheet(Linux)')
                Row row = sheet.getRow(0)
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

    def "デバイスシート更新"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        def test_result_reader = new TestResultReader(
                                         json_dir: 'src/test/resources/json')
        test_result_reader.read_entire_scenario(test_scenario)

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

package jp.co.toshiba.ITInfra.acceptance.Document;

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
public class TicketMaker {

    // ConfigObject item_map
    String excel_file
    String result_dir
    ReportMaker report_maker = new ReportMaker()

    def set_environment(ConfigTestEnvironment env) {
        // this.item_map = env.get_item_map()
        this.excel_file = env.get_excel_file()
        this.result_dir = env.get_node_dir()
        env.accept(report_maker)
    }

    // def visit_test_scenario(TestScenario test_scenario) {
    //     long start = System.currentTimeMillis()
    //     def excel_parser = new ExcelParser(excel_file)
    //     excel_parser.scan_sheet()
    //     test_scenario.accept(excel_parser)
    //     def test_result_reader = new TestResultReader(result_dir: result_dir)
    //     test_result_reader.read_entire_result(test_scenario)
    //     test_scenario.accept(report_maker)

    //     long elapse = System.currentTimeMillis() - start
    //     log.info "Finish ticket maker, Elapse : ${elapse} ms"
    // }

    def run() {
        long start = System.currentTimeMillis()
        def excel_parser = new ExcelParser(this.excel_file)
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
        def test_result_reader = new TestResultReader(result_dir: this.result_dir)
        test_result_reader.read_entire_result(test_scenario)
        test_scenario.accept(report_maker)

        long elapse = System.currentTimeMillis() - start
        log.info "Finish ticket maker, Elapse : ${elapse} ms"
    }
}

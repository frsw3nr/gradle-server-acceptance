package jp.co.toshiba.ITInfra.acceptance.Document;

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.Ticket.*

@Slf4j
@ToString(includePackage = false)
public class TicketRegistor {

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

    def get_redmine_data() {
        return this.report_maker?.redmine_ticket
    }

    def read_redmine_data() {
        def excel_parser = new ExcelParser(this.excel_file)
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
        def test_result_reader = new TestResultReader(result_dir: this.result_dir)
        test_result_reader.read_entire_result(test_scenario)
        test_scenario.accept(report_maker)
    }

    def regist_redmine_ticket() {
        def redmine_data = this.get_redmine_data()
        def tickets    = redmine_data.get_ticket_dict()
        def port_lists = redmine_data.get_port_list_dict()
        tickets.each { tracker, subjects ->
            subjects.each { subject, custom_fields ->
                println "REGIST: ${tracker}, ${subject}, ${custom_fields}"
                println "REGIST_PORT: ${port_lists[subject]}"
            }
        }
    }

    def run() {
        long start = System.currentTimeMillis()
        this.read_redmine_data()
        this.regist_redmine_ticket()
        long elapse = System.currentTimeMillis() - start
        log.info "Finish ticket maker, Elapse : ${elapse} ms"
    }
}

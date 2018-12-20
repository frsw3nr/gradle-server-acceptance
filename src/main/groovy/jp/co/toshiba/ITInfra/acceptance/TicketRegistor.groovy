package jp.co.toshiba.ITInfra.acceptance;

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import com.taskadapter.redmineapi.*
import com.taskadapter.redmineapi.bean.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Ticket.*

@Slf4j
@ToString(includePackage = false)
public class TicketRegistor {

    // ConfigObject item_map
    String excel_file
    String result_dir
    String redmine_project
    ReportMaker report_maker = new ReportMaker()
    TicketManager ticket_manager = TicketManager.instance

    def set_environment(ConfigTestEnvironment env) {
        // this.item_map = env.get_item_map()
        this.excel_file = env.get_excel_file()
        this.result_dir = env.get_node_dir()
        this.redmine_project = env.get_redmine_project()
        println "REDMINE_PROJECT:${this.redmine_project}"
        env.accept(this.report_maker)
        env.accept(this.ticket_manager)
        this.ticket_manager.init()
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

    def regist_redmine_ticket(String project_name) {
        def redmine_data = this.get_redmine_data()
        def tickets = redmine_data.get_ticket_dict()
        tickets.each { tracker, subjects ->
            subjects.each { subject, custom_fields ->
                // println "REGIST: ${tracker}, ${subject}, ${custom_fields}"
                Issue issue = this.ticket_manager.regist('cmdb',
                                                         tracker, 
                                                         subject, 
                                                         custom_fields)
                if (issue) {
                    def port_lists = redmine_data.get_port_lists(subject)
                    if (port_lists) {
                        List<Integer> port_list_ids = []
                        port_lists.each { ip, port_list ->
                            // println "REGIST_PORT_IP: ${ip}"
                            Issue port_list_issue = this.ticket_manager.regist_port_list('cmdb', ip)
                            // println "PORT_LIST_ISSUE: ${port_list_issue}"
                            if (!port_list_issue) {
                                return
                            }
                            port_list_ids << port_list_issue.id
                        }
                        this.ticket_manager.link(issue, port_list_ids)
                    }
                }

            }
        }
    }

    def run(String project_name) {
        long start = System.currentTimeMillis()
        this.read_redmine_data()
        this.regist_redmine_ticket(project_name)
        long elapse = System.currentTimeMillis() - start
        log.info "Finish ticket maker, Elapse : ${elapse} ms"
    }
}

package jp.co.toshiba.ITInfra.acceptance

import com.taskadapter.redmineapi.bean.Issue
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.Document.ExcelParser
import jp.co.toshiba.ITInfra.acceptance.Document.ReportMaker
import jp.co.toshiba.ITInfra.acceptance.Document.TestResultReader
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import jp.co.toshiba.ITInfra.acceptance.Ticket.TicketManager

@Slf4j
@ToString(includePackage = false)
public class TicketRegistor {

    // ConfigObject item_map
    String excel_file
    String project_node_dir
    String redmine_project
    ReportMaker report_maker = new ReportMaker()
    TicketManager ticket_manager = TicketManager.instance

    def set_environment(ConfigTestEnvironment env) {
        // this.item_map = env.get_item_map()
        this.excel_file = env.get_excel_file()
        this.project_node_dir = env.get_node_dir()
        this.redmine_project = env.get_redmine_project()
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
        def test_result_reader = new TestResultReader(node_dir: this.project_node_dir)
        test_result_reader.read_entire_result(test_scenario)
        test_scenario.accept(report_maker)
    }

    def regist_redmine_ticket() {
        def redmine_data = this.get_redmine_data()
        def tickets = redmine_data.get_ticket_dict()
        tickets.each { tracker, subjects ->
            subjects.each { subject, custom_fields ->
                // println "REGIST: ${tracker}, ${subject}, ${custom_fields}"
                if (tracker == '_Multi') {
                    Issue exist_issue = this.ticket_manager.get_issue(subject)
                    if (exist_issue) {
                        this.ticket_manager.update_custom_fields(exist_issue, custom_fields)
                    } else {
                        log.info "Ticket not found '${subject}', skip."
                    }
                } else {
                    Issue issue = this.ticket_manager.regist(this.redmine_project,
                                                             tracker, 
                                                             subject, 
                                                             custom_fields)
                    if (issue) {
                        def port_lists = redmine_data.get_port_lists(subject)
                        if (port_lists) {
                            List<Integer> port_list_ids = []
                            port_lists.each { ip, port_list ->
                                // println "REGIST_PORT_IP: ${ip}, ${port_list}"
                                Issue port_list_issue = 
                                    this.ticket_manager.regist_port_list(this.redmine_project,
                                                                         ip,
                                                                         port_list)
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
    }

    def check_project_node_dir() {
        def json_count = 0
        new File(this.project_node_dir).eachFile {
            ( it.name =~ /(.+).json/ ).each {
                json_count ++
            }
        }
        // println "JSON_COUNT:${json_count}"
        return (json_count > 0)
    }

    def run(String project_name) {
        long start = System.currentTimeMillis()
        log.info "Redmine Project : ${this.redmine_project}"
        if (!this.check_project_node_dir()) {
            log.error "No json in '${this.project_node_dir}'. Execute 'getconfig -u local'"
            return
        }
        this.read_redmine_data()
        this.regist_redmine_ticket()
        long elapse = System.currentTimeMillis() - start
        log.info "Finish ticket maker, Elapse : ${elapse} ms"
    }
}

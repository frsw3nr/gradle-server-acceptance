package jp.co.toshiba.ITInfra.acceptance.Ticket

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import static groovy.json.JsonOutput.*
import groovy.json.*
import com.taskadapter.redmineapi.*
import com.taskadapter.redmineapi.bean.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.Ticket.*

// setenv REDMINE_URL=http://localhost/redmine
// setenv REDMINE_API_KEY={APIキー}
// gradle --daemon test --tests "TestRedmineRepository.*"

/*
Redmine Java API 調査

* チケット作成の場合

  def new_issue = issue_manager.createIssue(issue); // 引数をリファレンスに発番

* チケット更新の場合

  issue_manager.update(issue); // issue を更新

更新の場合は名前でカスタムフィールドの指定ができるが、
作成の場合はカスタムフィールドID、名前、値の指定で登録が必要。

* カスタムフィールドの検索(Redmine 全体)

  def custom_fields = manager.getCustomFieldManager().getCustomFieldDefinitions()

* チケットの検索

  def params = new HashMap<String,String>();
  params.put("status_id","*");
  params.put("subject", subject);
  def results = issue_manager.getIssues(params).getResults()

* チケットのカスタムフィールド更新(新規チケットの場合)

  かなり複雑。一旦カスタムフィールドなしのチケットを作成してから次の更新をした方が良い

  # 事前にカスタムフィールドの辞書作成
  def custom_field_ids = [:]
  def custom_fields = manager.getCustomFieldManager().getCustomFieldDefinitions()
  custom_fields.each { custom_field ->
      custom_field_ids[custom_field.name] = custom_field.id
  }
  # 辞書からフィールドidをつき合わせし、カスタムフィールドを登録
  issue.addCustomField(CustomFieldFactory.create(custom_field_ids['OS名'], 'OS名', 'CentOS 6.10'))
  def new_issue = issue_manager.createIssue(issue);

* チケットのカスタムフィールド更新(既存チケットの場合)

  def custom_field = issue.getCustomFieldByName("OS名")
  custom_field.setValue('CentOS 6.9')
  issue_manager.update(issue);

* チケット削除

  mgr.getIssueManager().deleteIssue(0)

* リレーション登録

  def relations =[]
  def relation = issue_manager.createRelation(new_issue.id, issue_to.id, 'relates')
  relations << relation
  new_issue.addRelations(relations)
  issue_manager.update(new_issue)

* リレーション検索

  def relations = issue2.getRelations()

*/

@Slf4j
@ToString(includePackage = false)
@Singleton
class TicketManager {

    String redmine_uri
    String redmine_api_key
    String inventory_field
    String tracker_port_list
    int in_operation_status_id
    LinkedHashMap<String,String> port_list_custom_fields = [:]
    RedmineManager redmine_manager
    IssueManager   issue_manager
    ProjectManager project_manager

    def set_environment(ConfigTestEnvironment env) {
        this.redmine_uri     = env.get_redmine_uri()
        this.redmine_api_key = env.get_redmine_api_key()
        this.inventory_field = env.get_custom_field_inventory()
        this.tracker_port_list = env.get_tracker_port_list()
        this.in_operation_status_id = env.get_in_operation_status_id()
        this.port_list_custom_fields = env.get_port_list_custom_fields()
    }

    def init() {
        if (!this.redmine_manager) {
            this.redmine_manager = RedmineManagerFactory.createWithApiKey(
                                       this.redmine_uri,
                                       this.redmine_api_key)
        }
        if (!this.issue_manager) {
            this.issue_manager   = this.redmine_manager.getIssueManager()
        }
        if (!this.project_manager) {
            this.project_manager = this.redmine_manager.getProjectManager()
        }
    }

    Issue get_issue(String subject) {
        def params = new HashMap<String,String>();
        params.put("status_id","*");
        params.put("subject", subject);
        def results = this.issue_manager.getIssues(params).getResults()
        // getIssues()だと、リレーションの検索が出来ないため、再度getIssueById()で検索する
        return (results.isEmpty()) ? null : 
                                     this.issue_manager.getIssueById(results[0].id, 
                                                                     Include.relations)
    }

    Project get_project(String project_name) {
        try {
            return this.project_manager.getProjectByKey(project_name)
        } catch (NotFoundException e) {
            def msg = "Not found Redmine project '${project_name}' : ${e}"
            log.error(msg)
            throw new NotFoundException(msg)
        }
    }

    def delete(String subject) {
        def params = new HashMap<String,String>();
        params.put("status_id","*");
        params.put("subject", subject);

        def issues = this.issue_manager.getIssues(params);
        issues.getResults().each { issue ->
            this.issue_manager.deleteIssue(issue.id)
        }
    }

    Issue regist(String project_name, String tracker_name, String subject,
                 Map custom_fields = [:], Boolean in_operation = false) {
        Issue issue = null
        try {
            def project = this.get_project(project_name)
            def tracker = project.getTrackerByName(tracker_name)
            if (!tracker) {
                def msg = "Not found Redmine tracker '${tracker_name}' in '${project_name}'"
                log.error(msg)
                throw new NotFoundException(msg)
            }
            issue = get_issue(subject)
            if (!issue) {
                def new_issue = IssueFactory.create(null)
                new_issue.setProjectId(project.id)  // プロジェクト
                new_issue.setSubject(subject)       // 題名
                new_issue.setTracker(tracker)       // トラッカー
                issue = this.issue_manager.createIssue(new_issue)
            }
            issue.setProjectId(project.id)
            issue.setSubject(subject)
            issue.setTracker(tracker)
            if (in_operation) {
                issue.setStatusId(this.in_operation_status_id)
            }
            update_custom_fields(issue, custom_fields)
            // log.info "Regist '${tracker_name}:${subject}(${project_name})'"
        } catch (NotFoundException e) {
            def msg = "Ticket regist failed '${tracker_name}:${subject}'."
            log.error(msg)
            issue = null
        }
        return issue
    }

    def update_custom_fields(Issue issue, Map custom_fields = [:]) {
        def tracker_name = issue.getTracker().getName()
        def project_name = issue.getProjectName()
        def subject  = issue.getSubject()
        custom_fields.each { field_name, value ->
            // println "UPDATE_CUSTOM_FIELDS:${field_name}, ${value}, ${value.getClass()}"
            def custom_field = issue.getCustomFieldByName(field_name)
            if (!custom_field) {
                def msg = "Not found Redmine custom field '${field_name}' in '${tracker_name}'"
                log.error(msg)
                throw new NotFoundException(msg)
            }
            // 値が"No data"の場合は登録しない
            if (value in String && value.toLowerCase() == 'no data') {
                log.info "Skip 'No data' field update : '${field_name}'."
            // Redmine カスタムフィールドの真偽値(bool_cf)は文字列の"0"か"1"を返す必要がある
            } else if (value in Boolean) {
                def redmine_bool = (value) ? "1" : "0"
                custom_field.setValue(redmine_bool)

            } else {
                custom_field.setValue(value)
            }
        }
        def custom_field_inventory = issue.getCustomFieldByName(this.inventory_field)
        if (custom_field_inventory) {
            custom_field_inventory.setValue(subject)
        }
        try {
            this.issue_manager.update(issue)
        } catch (RedmineProcessingException e) {
            def msg = "Redmine update error '${tracker_name}:${subject}' set to '${custom_fields}' : ${e}."
            log.error(msg)
            throw new NotFoundException(msg)
        }
        log.info "Regist '${tracker_name}:${subject}(${project_name})'"
    }

    LinkedHashMap<String,String> get_port_list_custom_fields(Map custom_fields) {
        LinkedHashMap<String,String> fields = [:]
        this.port_list_custom_fields.each { redmine_name, test_item_name ->
            if (custom_fields.containsKey(test_item_name)) {
                def value = custom_fields[test_item_name]
                // println "PORT_LIST_CUSTOM_FIELDS:${test_item_name},${value}"
                if (value != null) {
                    fields[redmine_name] = value
                }
            }
        }
        return fields
    }

    Boolean check_lookuped_port_list(Map custom_fields = [:]) {
        return (custom_fields.containsKey('lookup') && custom_fields['lookup'] == true)
    }

    Issue regist_port_list(String project_name, String subject, Map custom_fields = [:]) {
        // println "CUSTOM_FIELDS:${custom_fields}"
        // println "PORT_LIST_CUSTOM_FIELDS:${this.port_list_custom_fields}"
        // println "TRACKER_PORT_LIST: ${this.tracker_port_list}"
        if (this.port_list_custom_fields.size() == 0) {
            def msg = "Redmine update error '${subject}' : Not found 'port_list_custom_fields'"
            log.error(msg)
            throw new NotFoundException(msg)
        }
        def fields = this.get_port_list_custom_fields(custom_fields)
        def lookuped = this.check_lookuped_port_list(custom_fields)
        return this.regist(project_name, this.tracker_port_list, subject, fields, lookuped)
    }

    Boolean link(Issue ticket_from, List<Integer> ticket_to_ids) {
        Boolean isok = false
        // 関連するチケットIDの洗い出し
        def existing_relations = [:]
        ticket_from.getRelations().each { issue ->
            existing_relations[issue.issueId] = true
            existing_relations[issue.issueToId] = true
        }
        // println "EXISTING_RELATIONS:${existing_relations}"
        def relations =[]
        try {
            ticket_to_ids.each { ticket_to_id ->
                if (!existing_relations.containsKey(ticket_to_id)) {
                    relations << this.issue_manager.createRelation(ticket_from.id, 
                                                                   ticket_to_id,
                                                                   'relates')
                }
            }
            // println "RELATIONS:${relations}"
            if (relations.size() > 0) {
                ticket_from.addRelations(relations)
                this.issue_manager.update(ticket_from)
            }
            isok = true
        } catch (RedmineProcessingException e) {
            def msg = "Redmine link error '${ticket_from}' to '${ticket_to_ids}' : ${e}."
            log.info(msg)
        }
        return isok
    }

    // def "ポートリスト登録1"() {
    //     when:
    //     def issue_manager = redmine_manager.getIssueManager();
    //     // IAサーバ, ostrich, 設備検索
    //     def server1 = get_issue('ostrich')

    //     // ポートリスト登録
    //     def port_list1 = regist_ticket('cmdb', 'ポートリスト', '192.168.10.1')
    //     def port_list2 = regist_ticket('cmdb', 'ポートリスト', '192.168.98.130')

    //     def relations =[]
    //     relations << issue_manager.createRelation(server1.id, port_list1.id, 'relates')
    //     relations << issue_manager.createRelation(server1.id, port_list2.id, 'relates')
    //     server1.addRelations(relations)
    //     issue_manager.update(server1)

    //     then:
    //     1 == 1
    // }

}

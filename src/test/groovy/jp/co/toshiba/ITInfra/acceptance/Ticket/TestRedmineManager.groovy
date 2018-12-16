import spock.lang.Specification
import static groovy.json.JsonOutput.*
import groovy.json.*
import com.taskadapter.redmineapi.*
import com.taskadapter.redmineapi.bean.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "TestRedmineManager.チケット削除1"

class TestRedmineManager extends Specification {

    def redmine_manager

// REGIST: 
// IAサーバ, ostrich, [OS名:CentOS release 6.7, CPU数:1, MEM容量:1.8332023620605469]
// REGIST_PORT: 
// [192.168.10.1, 192.168.10.4]

// REGIST: 
// IAサーバ, cent7, [OS名:CentOS Linux release 7.3.1611, CPU数:1, MEM容量:1.796844482421875]
// REGIST_PORT: 
// [172.17.0.1, 192.168.0.20]

// REGIST: 
// IAサーバ, win2012, [OS名:Microsoft Windows Server 2012 R2 Standard 評価版, CPU数:null, MEM容量:3.9995651245117188]
// REGIST_PORT: 
// [192.168.0.24]

    def setup() {
        def redmine_uri = System.getenv("REDMINE_URL") ?: "http://localhost/redmine";
        def redmine_api_key = System.getenv("REDMINE_API_KEY") ?: "";
        redmine_manager = RedmineManagerFactory.createWithApiKey(redmine_uri,
                                                                 redmine_api_key);
    }

    def get_issues_by_subject(String subject) {
        def issue_manager = redmine_manager.getIssueManager();

        def params = new HashMap<String,String>();
        params.put("status_id","*");
        params.put("subject", subject);

        def results = issue_manager.getIssues(params).getResults()
        return (results.isEmpty()) ? null : results[0]
    }

    def delete_ticket(String subject) {
        def issue_manager = redmine_manager.getIssueManager();

        def params = new HashMap<String,String>();
        params.put("status_id","*");
        params.put("subject", subject);

        def issues = issue_manager.getIssues(params);
        issues.getResults().each { issue ->
            issue_manager.deleteIssue(issue.id)
        }
    }

    def regist_ticket(String project_name, String tracker_name, String subject,
                      Map custom_fields) {
        def project_manager = redmine_manager.getProjectManager();
        def project = project_manager.getProjectByKey(project_name);
        def tracker = project.getTrackerByName(tracker_name);
        println "Tracker: ${tracker}"

        def issue_manager = redmine_manager.getIssueManager();
        def issue = get_issues_by_subject(subject)
        if (!issue) {
            def new_issue = IssueFactory.create(null);
            new_issue.setProjectId(project.id);       // プロジェクト
            new_issue.setSubject(subject);            // 題名
            new_issue.setTracker(tracker);            // トラッカー

            issue = issue_manager.createIssue(new_issue);
        }

        def custom_field = issue.getCustomFieldByName("OS名")
        custom_field.setValue('CentOS 7.2')
        issue_manager.update(issue);
    }


    def "チケット削除1"() {
        when:
        delete_ticket('ostrich')

        then:
        1 == 1
    }

    def "チケット登録1"() {
        when:
        // IAサーバ, ostrich, 
        def custom_fields = 
            ['OS名':'CentOS release 6.7', 'CPU数':'1', 'MEM容量':'1.8332023620605469']
        regist_ticket('cmdb', 'IAサーバ', 'ostrich', custom_fields)

        then:
        1 == 1
    }

}


import spock.lang.Specification
import com.taskadapter.redmineapi.*
import com.taskadapter.redmineapi.bean.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.Ticket.*

// setenv REDMINE_URL=http://localhost/redmine
// setenv REDMINE_API_KEY={APIキー}
// gradle --daemon test --tests "TestRedmineRepository.チケット検索1"

/*
ToDo
=====

Redmine I/F作成
---------------

redmine_repository.py RedmineRepository
redmine_field.py      RedmineField
redmine_stat.py       RedmineStatistics 必要？
redmine_cache.py      RedmineStatistics 必要？

ticket.py　メイン処理

    def set_custom_fields(self):
        Redmine カスタムフィールドのIDリストと、カスタムフィールドからCSVカラム名の逆引きリストの生成
    def get_issue_cache(self, key, **kwargs):
        SQLite3 キャッシュからキーを指定してチケット検索
    def reset_record_statistics(self):
        チケット登録処理統計の初期化
    def validate_custom_fileds(self):
        チケット登録処理統計から、全カスタムフィールドが設定されているかを返す
    def get_difference_with_cache(self, issue_cache, csv):
        チケット属性のキャッシュ値とCSV値の比較をする。
    def get_custom_field_default_value(self, field_name, row, **kwargs):  ※未使用
    def get_issue_status_id(self, issue, **kwargs):
        チケットのステータスID取得。
    def get_user_updated_fields(self, issue):
        チケットカスタムフィールドのユーザ更新有無をチェックする。
    def make_custom_fields(self, row, csv, user_updated_fields = {}):
        登録するチケットのカスタムフィールド値を設定
    def regist(self, fab, key, row, **kwargs):
        Redmine チケットの登録。use_cache, skip_redmine オプションの条件により処理が変わる

単体テスト
----------

jp/co/toshiba/ITInfra/acceptance/Ticket

import jp.co.toshiba.ITInfra.acceptance.Ticket.RedmineRepository
import jp.co.toshiba.ITInfra.acceptance.Ticket.RedmineField
import jp.co.toshiba.ITInfra.acceptance.Ticket

lib/Ticket/AIX
lib/Ticket/Eternus
lib/Ticket/HitachiVSP
lib/Ticket/iLO
lib/Ticket/Linux
*/


class TestRedmineRepository extends Specification {
    def manager

    def setup() {
        def redmine_uri = System.getenv("REDMINE_URL") ?: "http://localhost/redmine";
        def redmine_api_key = System.getenv("REDMINE_API_KEY") ?: "";
        manager = RedmineManagerFactory.createWithApiKey(redmine_uri, redmine_api_key);
    }

    def "チケット一覧1"() {
        when:
        // override default page size if needed
        manager.setObjectsPerPage(100);
        List<Issue> issues = manager.getIssueManager().getIssues("cmdb", null);
        for (Issue issue : issues) {
            System.out.println(issue.toString());
        }

        then:
        1 == 1
    }

    def "チケット登録1"() {
        when:
        def projectManager = manager.getProjectManager();
        def project = projectManager.getProjectByKey("cmdb");
        println "Project:${project}"

        def tracker = project.getTrackerByName("サポート");
        println "Tracker: ${tracker}"

        def issue_manager = manager.getIssueManager();
        def issue = IssueFactory.create(null);

        // プロジェクト
        issue.setProjectId(project.id);

        // 題名
        issue.setSubject("test123");
        // 説明
        issue.setDescription("テスト redmine-java-api");
        // トラッカー
        issue.setTracker(tracker);

        // チケット登録
        def new_issue = issue_manager.createIssue(issue);
        issue_manager.update(new_issue);

        then:
        1 == 1
    }

    def "チケット検索1"() {
        when:
        def issue_manager = manager.getIssueManager();
        // def issue = issue_manager.getIssueById(40);

        def params = new HashMap<String,String>();
        params.put("status_id","*");
        params.put("subject", "test123");

        // List<Issue> issues = issue_manager.getIssues(params);
        // for (Issue issue : issues) {
        //     System.out.println(issue.toString());
        // }
        def issues = issue_manager.getIssues(params);
        println issues.getResults()
        issues.getResults().each { issue ->
            println issue
        }
        then:
        1 == 1
    }

    def "プロジェクト検索1"() {
        when:
        def projects = manager.getProjectManager().getProjects();
        println projects

        then:
        1 == 1
    }

    def "カスタムフィールド検索1"() {
// List<CustomFieldDefinition> customFieldDefinitions = mgr.getCustomFieldManager().getCustomFieldDefinitions();
// // sample implementation for getCustomFieldByName() is in CustomFieldResolver (test class).
// // in prod code you would typically know the custom field name or id already 
// CustomFieldDefinition customField1 = getCustomFieldByName(customFieldDefinitions, "my_custom_1");
// String custom1Value = "some value 123";
// issue.addCustomField(CustomFieldFactory.create(customField1.getId(), customField1.getName(), custom1Value));
// issueManager.update(issue);
        when:
        def custom_field_ids = [:]
        def custom_fields = manager.getCustomFieldManager().getCustomFieldDefinitions()
        custom_fields.each { custom_field ->
            // println "CustomField:${custom_field.id},${custom_field.name}"
            custom_field_ids[custom_field.name] = custom_field.id
        }
        println "CustomFieldIds: ${custom_field_ids}"
        def result = CustomFieldFactory.create(custom_field_ids['OS名'], 'OS名', 'CentOS 6.9')
        println "CustomField(OS名): ${result}"

        then:
        1 == 1
    }

    def get_issues_by_subject(String subject) {
        def issue_manager = manager.getIssueManager();
        // def issue = issue_manager.getIssueById(40);

        def params = new HashMap<String,String>();
        params.put("status_id","*");
        params.put("subject", subject);

        def results = issue_manager.getIssues(params).getResults()
        return (results.size() == 0) ? Null : results[0]
    }

    def "カスタムフィールド登録1"() {
        when:
        def projectManager = manager.getProjectManager();
        def project = projectManager.getProjectByKey("cmdb");
        println "Project:${project}"

        def issue_manager = manager.getIssueManager();
        def issue = get_issues_by_subject("test_server_123")
        println "Issue:${issue}"
        if (!issue) {
            issue = IssueFactory.create(null);
        }

        // プロジェクト
        issue.setProjectId(project.id);

        // 題名
        issue.setSubject("test_server_123");
        // トラッカー
        issue.setTracker(project.getTrackerByName("IAサーバ"));
        // カスタムフィールド
        def custom_field_ids = [:]
        def custom_fields = manager.getCustomFieldManager().getCustomFieldDefinitions()
        custom_fields.each { custom_field ->
            custom_field_ids[custom_field.name] = custom_field.id
        }
        issue.addCustomField(CustomFieldFactory.create(custom_field_ids['OS名'], 'OS名', 'CentOS 7.2'))

        // チケット登録
        // def new_issue = issue_manager.createIssue(issue);
        issue_manager.update(issue);

        then:
        1 == 1
    }
}

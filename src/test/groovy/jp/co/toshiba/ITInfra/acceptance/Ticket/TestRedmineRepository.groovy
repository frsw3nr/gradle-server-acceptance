import spock.lang.Specification
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

Redmine I/F検討
---------------

redmine_repository.py RedmineRepository
redmine_field.py      RedmineField
redmine_stat.py       RedmineStatistics 廃止
redmine_cache.py      RedmineStatistics 廃止

ticket.py　廃止メソッド

    def set_custom_fields(self):
        Redmine カスタムフィールドのIDリストと、カスタムフィールドからCSVカラム名の逆引きリストの生成
        廃止。issue.getCustomFieldByName(フィールド名)を使用
        def custom_field = issue.getCustomFieldByName("OS名")
        custom_field.setValue('CentOS 6.9')
    def get_issue_cache(self, key, **kwargs):
        SQLite3 キャッシュからキーを指定してチケット検索 ⇒ 廃止
    def reset_record_statistics(self):
        チケット登録処理統計の初期化 ⇒ 廃止
        チケットカスタムフィールドのユーザ更新有無をチェックする。 ⇒ 廃止
    def make_custom_fields(self, row, csv, user_updated_fields = {}):
        登録するチケットのカスタムフィールド値を設定 ⇒ 廃止
    def get_difference_with_cache(self, issue_cache, csv):
        チケット属性のキャッシュ値とCSV値の比較をする。 ⇒ 廃止
    def get_custom_field_default_value(self, field_name, row, **kwargs):  ※未使用

ticket.py　移行メソッド

    def validate_custom_fileds(self):
        チケット登録処理統計から、全カスタムフィールドが設定されているかを返す
    def get_issue_status_id(self, issue, **kwargs):
        チケットのステータスID取得。
    def get_user_updated_fields(self, issue):
    def regist(self, fab, key, row, **kwargs):
        Redmine チケットの登録。use_cache, skip_redmine オプションの条件により処理が変わる

ticket.py regist() 移行検討

前処理：
プロジェクト検索
トラッカー検索

メイン：
サブジェクト名でチケットを検索
ない場合
    チケット新規作成
チケットステータス検索
カスタムフィールドのセット
カスタムフィールドバリデーション⇒チケットステータス更新
チケット更新

ToDo
=====

サーバ機器モデルプロト
----------------------

実行オプション追加
Redmine APIスクリプト追加

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

結合テスト
----------

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

    def "チケット削除"() {
        when:
        def issue_manager = manager.getIssueManager();
        // def issue = issue_manager.getIssueById(40);

        def params = new HashMap<String,String>();
        params.put("status_id","*");
        // params.put("subject", "test123%");
        params.put("subject", "test123");

        def issues = issue_manager.getIssues(params);
        // println issues.getResults()
        issues.getResults().each { issue ->
            issue_manager.deleteIssue(issue.id)
        }

        then:
        1 == 1
    }

    def "チケット削除2"() {
        when:
        def issue_manager = manager.getIssueManager();
        // def issue = issue_manager.getIssueById(40);

        def params = new HashMap<String,String>();
        params.put("status_id","*");
        // params.put("subject", "test123%");
        params.put("subject", "test123b");

        def issues = issue_manager.getIssues(params);
        // println issues.getResults()
        issues.getResults().each { issue ->
            issue_manager.deleteIssue(issue.id)
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
        println "RESULTS:${results}"
        println(results.isEmpty()); 
        // if (results.isEmpty()) 
        //     return null 
        // else
        //     return results[0]
        return (results.isEmpty()) ? null : results[0]
    }

    def "カスタムフィールド登録1"() {
        when:
        def projectManager = manager.getProjectManager();
        def project = projectManager.getProjectByKey("cmdb");
        println "Project:${project}"

        def issue_manager = manager.getIssueManager();
        def issue = get_issues_by_subject("test123")
        println "Issue:${issue}"
        if (!issue) {
            println "CREATE"
            issue = IssueFactory.create(null);
        }
        println "Issue2:${issue}"

        // プロジェクト
        issue.setProjectId(project.id);

        // 題名
        issue.setSubject("test123");
        // トラッカー
        issue.setTracker(project.getTrackerByName("IAサーバ"));
        // カスタムフィールド
        def custom_field_ids = [:]
        def custom_fields = manager.getCustomFieldManager().getCustomFieldDefinitions()
        custom_fields.each { custom_field ->
            custom_field_ids[custom_field.name] = custom_field.id
        }
        issue.addCustomField(CustomFieldFactory.create(custom_field_ids['OS名'], 'OS名', 'CentOS 6.10'))

        // チケット登録
        def new_issue = issue_manager.createIssue(issue);
        // issue_manager.update(new_issue);

        then:
        1 == 1
    }

    def "チケット更新1"() {
        when:
        def projectManager = manager.getProjectManager();
        def project = projectManager.getProjectByKey("cmdb");
        println "Project:${project}"

        def issue_manager = manager.getIssueManager();
        def issue = get_issues_by_subject("test123")

        then:
        issue != null

        when:
        def custom_field = issue.getCustomFieldByName("OS名")
        custom_field.setValue('CentOS 7.2')

        // チケット登録
        issue_manager.update(issue);

        then:
        1 == 1
    }

    def "リレーション登録1"() {
        when:
        def projectManager = manager.getProjectManager();
        def project = projectManager.getProjectByKey("cmdb");
        def tracker = project.getTrackerByName("サポート");
        def issue_manager = manager.getIssueManager();
        def issue = IssueFactory.create(null);

        issue.setProjectId(project.id);     // プロジェクト
        issue.setSubject("test123b");       // 題名
        issue.setTracker(tracker);          // トラッカー

        // チケット登録
        def new_issue = issue_manager.createIssue(issue);
        // issue_manager.update(new_issue);

        // リレーションチケット検索
        def issue_to = get_issues_by_subject("test123")

        // リレーション登録
        // public void addRelations(Collection<IssueRelation> collection) {
        //     storage.get(RELATIONS).addAll(collection);
        // }
        // リレーション:[IssueRelation [getId()=4139, issueId=4275, issueToId=4276, type=relates, delay=0]]
        def relations =[]
        def relation = issue_manager.createRelation(new_issue.id, issue_to.id, 'relates')
        relations << relation
        println "リレーション：${relations}"
        // List <IssueRelation
        // def relations = CustomRelation
        new_issue.addRelations(relations)
        issue_manager.update(new_issue)

        then:
        1 == 1
    }

    def "リレーション検索1"() {
        when:
        def projectManager = manager.getProjectManager();
        def project = projectManager.getProjectByKey("cmdb");

        def issue_manager = manager.getIssueManager();
        def issue = get_issues_by_subject("test123b")
        def issue2 = issue_manager.getIssueById(issue.id, Include.relations);
        println "チケット2:${issue2}"
        def relations = issue2.getRelations()
        println "リレーション:${relations}"

        // リレーションを削除する場合
        // issue_manager.deleteIssueRelations(issue2)
        // リレーション:[IssueRelation [getId()=4139, issueId=4275, issueToId=4276, type=relates, delay=0]]

        then:
        1 == 1
    }
}

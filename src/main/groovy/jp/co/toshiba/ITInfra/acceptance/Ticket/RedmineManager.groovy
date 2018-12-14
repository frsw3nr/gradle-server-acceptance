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


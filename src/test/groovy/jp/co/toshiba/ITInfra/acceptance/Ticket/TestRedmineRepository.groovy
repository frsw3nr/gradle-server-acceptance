import spock.lang.Specification
import com.taskadapter.redmineapi.*
import com.taskadapter.redmineapi.bean.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.Ticket.*

// setenv REDMINE_URL=http://localhost/redmine
// setenv REDMINE_API_KEY={APIキー}
// gradle --daemon test --tests "TestRedmineRepository.デモ1"

class TestRedmineRepository extends Specification {
    def manager

    def setup() {
        def redmine_uri = System.getenv("REDMINE_URL") ?: "http://localhost/redmine";
        def redmine_api_key = System.getenv("REDMINE_API_KEY") ?: "";
        manager = RedmineManagerFactory.createWithApiKey(redmine_uri, redmine_api_key);
    }

    def "デモ1"() {
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

    def "デモ2"() {
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


}

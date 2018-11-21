@GrabConfig( systemClassLoader=true )
@Grapes( 
@Grab(group='com.taskadapter', module='redmine-java-api', version='3.1.1'))

import com.taskadapter.redmineapi.*;
import com.taskadapter.redmineapi.bean.*;

String redmine_uri = System.getenv("REDMINE_URL") ?: "http://localhost/redmine";
String redmine_api_key = System.getenv("REDMINE_API_KEY") ?: "";
Integer queryId = null; // any
def manager = RedmineManagerFactory.createWithApiKey(redmine_uri, redmine_api_key);

def projectManager = manager.getProjectManager();
def project = projectManager.getProjectByKey("ProjectA");
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

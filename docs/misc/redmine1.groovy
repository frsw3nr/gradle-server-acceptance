@GrabConfig( systemClassLoader=true )
@Grapes( 
@Grab(group='com.taskadapter', module='redmine-java-api', version='3.1.1'))

import com.taskadapter.redmineapi.*;
import com.taskadapter.redmineapi.bean.*;

String redmine_uri = System.getenv("REDMINE_URL") ?: "http://localhost/redmine";
String redmine_api_key = System.getenv("REDMINE_API_KEY") ?: "";
def manager = RedmineManagerFactory.createWithApiKey(redmine_uri, redmine_api_key);


// override default page size if needed
manager.setObjectsPerPage(100);
List<Issue> issues = manager.getIssueManager().getIssues(1, "");
for (Issue issue : issues) {
    System.out.println(issue.toString());
}

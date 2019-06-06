package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.Unix

import javax.xml.bind.*
import static groovy.json.JsonOutput.*
import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors

// For ssh session
// import org.hidetake.groovy.ssh.Ssh
import ch.ethz.ssh2.Connection
import ch.ethz.ssh2.Session
import net.sf.expectit.Expect
import net.sf.expectit.ExpectBuilder
import static net.sf.expectit.matcher.Matchers.contains
import static net.sf.expectit.matcher.Matchers.regexp

import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
class SshSession {

    String prompt = '.*[%|$|#|>] $'
    int timeout = 30
    String evidence_log_share_dir
    String local_dir

    Connection ssh
    Session session

    SshSession(test_spec) {
        this.timeout   = test_spec?.timeout ?: 30
        this.local_dir = test_spec?.local_dir
        this.evidence_log_share_dir = test_spec?.evidence_log_share_dir
    }

    def init_session(String ip, String user, String password) {
        ssh = new Connection(ip, 22)
        try {
            ssh.connect()
            def result = ssh.authenticateWithPassword(user, password)
            if (!result) {
                throw new IOException("Connect failed")
            }

            session = ssh.openSession()
            session.requestDumbPTY();
            session.startShell();
            Expect expect = new ExpectBuilder()
                    .withOutput(session.getStdin())
                    .withInputs(session.getStdout(), session.getStderr())
                            // .withEchoOutput(System.out)
                            // .withEchoInput(System.out)
                    .build();
            // expect.expect(contains(prompts['ok'])); 
            expect.expect(regexp(prompt)); 
            expect.sendLine("sh");  //  c: Login to  CLI shell
            expect.expect(regexp(prompt)); 
            expect.sendLine("LANG=C");  //  c: Login to  CLI shell
            // expect.expect(contains(prompts['sh'])); 
            expect.expect(regexp(prompt)); 
            expect.close()

        } catch (Exception e) {
            log.error "[SSH Test] Init test faild.\n" + e
            throw new IllegalArgumentException(e)
        }
    }

    String truncate_first_line(String message) {
        String result = message
        int truncate_size = message.indexOf('\n');
        if (truncate_size > 0) {
            result = message.substring(truncate_size)
        }
        return result
    }

    def run_command(command, test_id, share = false) {
        try {
            def log_path = (share) ? evidence_log_share_dir : local_dir

            Expect expect = new ExpectBuilder()
                    .withOutput(session.getStdin())
                    .withInputs(session.getStdout(), session.getStderr())
                            // .withEchoOutput(System.out)
                            // .withEchoInput(System.out)
                    .build();

            def row = 0
            def lines = command.readLines()
            def max_row = lines.size()
            lines.each { line ->
                expect.sendLine(line)
                if (0 < row && row < max_row) {
                    expect.expect(regexp(prompt))
                }
                row ++
            }
            def res = expect.expect(regexp(prompt))
            String result = res.getBefore(); 
            expect.close()

            println "COMMAND:$command, RESULT:$result<EOF>"

            new File("${log_path}/${test_id}").text = result
            return truncate_first_line(result)

        } catch (Exception e) {
            log.error "[SSH Test] Command error '$command', skip.\n" + e
        }
    }

    def close() {
        ssh.close()
    }
}

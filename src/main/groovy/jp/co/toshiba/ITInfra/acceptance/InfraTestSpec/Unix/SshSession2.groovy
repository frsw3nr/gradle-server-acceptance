package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.Unix

import com.jcraft.jsch.*
// import ch.ethz.ssh2.Connection
// import ch.ethz.ssh2.Session
import groovy.util.logging.Slf4j
import net.sf.expectit.Expect
import net.sf.expectit.ExpectBuilder

import static net.sf.expectit.matcher.Matchers.regexp

// For ssh session
// import org.hidetake.groovy.ssh.Ssh

@Slf4j
class SshSession2 {

    static String prompt_regexp  = '[%|\$|#] \$'
    static String prompt_command = '$ '
    // String prompt = '.*[%|$|#] $'
    int timeout = 60
    Boolean debug = false
    String current_test_log_dir = "."
    String local_dir = "."
    static int session_interval = 500
    String ip
    String user
    String password
    Boolean change_ascii_shell

    // Connection ssh
    Session session
    ChannelShell channel

    SshSession2(test_spec) {
        this.prompt_regexp = test_spec?.prompt ?: '$ '
        this.timeout       = test_spec?.timeout ?: 60
        this.debug         = test_spec?.debug ?: false
        this.local_dir     = test_spec?.local_dir
        this.current_test_log_dir = test_spec?.current_test_log_dir

        if (debug) {
            println "DEBUG:${this.debug}"
            println "TIMEOUT:${this.timeout}"
            println "PROMPT(REGEXP):${this.prompt_regexp}"
            println "PROMPT(COMMAND):${this.prompt_command}"
        }
    }

    Expect expect_session() {
        ExpectBuilder builder = new ExpectBuilder()
                .withOutput(channel.getOutputStream())
                .withInputs(channel.getInputStream(), channel.getExtInputStream())
                .withExceptionOnFailure()

        if (this.debug) {
            builder.withEchoOutput(System.out)
                   .withEchoInput(System.out)
        }
        return builder.build()
    }

    def init_session(String ip, String user, String password, Boolean change_ascii_shell = true) {
        this.ip = ip
        this.user = user
        this.password = password
        this.change_ascii_shell = change_ascii_shell

        JSch jsch = new JSch();
        session = jsch.getSession(user, ip, 22);
        session.setPassword(password);

        Hashtable<String,String> config = new Hashtable<String,String>();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        Expect expect
        try {
            session.connect(60000);
            channel = (ChannelShell) session.openChannel("shell");
            channel.connect();

            // Get input and output stream references
            expect = expect_session()

            // Unify ascii code shell to avoid multi-byte
            expect.expect(regexp(this.prompt_regexp)); 
            if (change_ascii_shell) {
                expect.sendLine("sh");
                expect.expect(regexp(this.prompt_regexp)); 
                expect.sendLine("LANG=C");
                expect.expect(regexp(this.prompt_regexp)); 
            }

        } catch (Exception e) {
            log.error "[SSH Test] Init test faild.\n" + e
            throw new IllegalArgumentException(e)
        } finally {
            if (expect) {
                expect.close()
            }
        }
    }

    String truncate_first_lines(String message, int limit_row = 0) {
        def lines = message.readLines()
        def row = 0
        // 複数行のスクリプト実行の場合は1行前までを取り除く
        // if (limit_row > 1) {
        //     limit_row = limit_row - 1
        // }
        StringBuffer sb = new StringBuffer();
        lines.each { line ->
            if (row >= limit_row) {
                sb.append("$line\n");
            }
            row ++
        }
        return sb.toString()
    }

    public static String truncate_last_line(String message) {
        String result = message
        int truncate_size = message.length();
        truncate_size = message.lastIndexOf('\n', truncate_size - 1);
        if (truncate_size > 0) {
            result = message.substring(0, truncate_size - 1)
        }
        return result
    }

    def run_command(command, test_id, share = false) {
        Expect expect
        try {
            expect = expect_session()

            def row = 0
            def lines = command.readLines()
            def max_row = lines.size()
            lines.each { line ->
                expect.sendLine(line)
                if (this.debug) {
                    println"SEND: $row: $line"
                }
                row ++
            }
            sleep(session_interval)
            def res = expect.expect(regexp(this.prompt_regexp)); 
            String result = res.getBefore()
            String result_truncated = truncate_last_line(result); 
            String result_truncated2 = truncate_first_lines(result_truncated, max_row); 
            if (this.debug) {
                println"RECV: ${row}: ${res}<EOF>"
                println"RESULT: ${result_truncated2}<EOF>"
            }

            def log_path = (share) ? current_test_log_dir : local_dir
            new File("${log_path}/${test_id}").text = result_truncated2

        } catch (Exception e) {
            log.error "[SSH Test] Command error '$command', skip and reconnect.\n" + e
            init_session(this.ip, this.user, this.password, this.change_ascii_shell)
        } finally {
            if (expect) {
                expect.close()
            }
        }
    }

    def close() {
        if (channel) {
            channel.disconnect()
        }
        if (session) {
            session.disconnect()
        }
    }
}

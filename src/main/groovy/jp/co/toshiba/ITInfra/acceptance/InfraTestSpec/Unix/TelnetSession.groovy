package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.Unix

import groovy.util.logging.Slf4j
import org.apache.commons.net.telnet.EchoOptionHandler
import org.apache.commons.net.telnet.SuppressGAOptionHandler
import org.apache.commons.net.telnet.TelnetClient
import org.apache.commons.net.telnet.TerminalTypeOptionHandler

import net.sf.expectit.Expect
import net.sf.expectit.ExpectBuilder
import static net.sf.expectit.matcher.Matchers.contains
import static net.sf.expectit.matcher.Matchers.regexp

// For telnet session

@Slf4j
class TelnetSession {

    static TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
    static EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
    static SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

    static String prompt_regexp  = '[%|\$|#] \$'
    static String prompt_command = '$ '

    static int telnet_session_interval = 1000
    int timeout = 30
    Boolean debug = false
    String current_test_log_dir
    String local_dir

    TelnetClient telnet

    TelnetSession(test_spec) {
        this.prompt_regexp = test_spec?.prompt ?: '$ '
        this.timeout       = test_spec?.timeout ?: 30
        this.debug         = test_spec?.debug ?: false
        this.local_dir     = test_spec?.local_dir
        this.current_test_log_dir = test_spec?.current_test_log_dir

        if (debug) {
            println "DEBUG:${this.debug}"
            println "PROMPT(REGEXP):${this.prompt_regexp}"
            println "PROMPT(COMMAND):${this.prompt_command}"
        }
    }

    Expect expect_session() {
        ExpectBuilder builder = new ExpectBuilder()
                .withOutput(telnet.getOutputStream())
                .withInputs(telnet.getInputStream())
                .withExceptionOnFailure()

        if (this.debug) {
            builder.withEchoOutput(System.out)
                   .withEchoInput(System.out)
        }
        return builder.build()
    }

    def init_session(String ip, String user, String password, Boolean change_ascii_shell = true) {
        telnet = new TelnetClient();

        try {
            telnet.setDefaultTimeout(1000 * timeout);
            telnet.connect(ip);

            telnet.setSoTimeout(1000 * timeout);
            telnet.setSoLinger(true, 1000 * timeout);
            telnet.addOptionHandler(ttopt);
            telnet.addOptionHandler(echoopt);
            telnet.addOptionHandler(gaopt);

            // Get input and output stream references
            Expect expect = expect_session()

            // Login telnet session
            if (user != '') {
                expect.expect(contains("login: ")); 
                expect.sendLine(user);
            }
            if (password != '') {
                expect.expect(contains("Password: ")); 
                expect.sendLine(password);
            }

            // Unify ascii code shell to avoid multi-byte
            if (change_ascii_shell) {
                expect.sendLine("sh");
                expect.expect(regexp(this.prompt_regexp)); 
                expect.sendLine("LANG=C");
                expect.expect(regexp(this.prompt_regexp)); 
            }

        } catch (Exception e) {
            log.error "[Telnet Test] Init test faild.\n" + e
            throw new IllegalArgumentException(e)
        } finally {
            if (expect) {
                expect.close()
            }
        }
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
        try {
            Expect expect = expect_session()

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
            def res = expect.expect(regexp(this.prompt_regexp))
            String result = res.getBefore()
            String result_truncated = truncate_last_line(result); 
            if (this.debug) {
                println"RECV: ${row}: ${res}<EOF>"
            }

            def log_path = (share) ? current_test_log_dir : local_dir
            new File("${log_path}/${test_id}").text = result_truncated
        } catch (Exception e) {
            log.error "[Telnet Test] Command error '$command' faild, skip.\n" + e
        } finally {
            if (expect) {
                expect.close()
            }
        }
    }

    def close() {
        if (telnet) {
            telnet.disconnect()
        }
    }
}

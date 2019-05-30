package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.Unix

import javax.xml.bind.*
import static groovy.json.JsonOutput.*
import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors

// For telnet session
import org.apache.commons.net.telnet.EchoOptionHandler
import org.apache.commons.net.telnet.SuppressGAOptionHandler
import org.apache.commons.net.telnet.TelnetClient
import org.apache.commons.net.telnet.TerminalTypeOptionHandler
import java.io.InputStream
import java.io.PrintStream

import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
class TelnetSession {

    static java.io.InputStream tin;
    static java.io.PrintStream tout;
    // static String prompt = "XSCF>"

    static String prompt = '$'
    int timeout = 30
    String evidence_log_share_dir
    String local_dir

    TelnetClient telnet

    TelnetSession(test_spec) {
        this.prompt    = test_spec?.prompt ?: '$'
        this.timeout   = test_spec?.timeout ?: 30
        this.local_dir = test_spec?.local_dir
        this.evidence_log_share_dir = test_spec?.evidence_log_share_dir
    }

    def init_session(String ip, String user, String password) {
        telnet = new TelnetClient();

        try {
            telnet.setDefaultTimeout(1000 * timeout);
            telnet.connect(ip);
            telnet.setSoTimeout(1000 * timeout);
            telnet.setSoLinger(true, 1000 * timeout);

            // Get input and output stream references
            tin = telnet.getInputStream();
            tout = new PrintStream(telnet.getOutputStream());
     
            // Login telnet session
            readUntil("login: ");
            write(user);
            // write('console');
            readUntil("Password: ");
            write(password);
            // write('console0');
            readUntil(prompt + " ");
        } catch (Exception e) {
            log.error "[Telnet Test] Init test faild.\n" + e
            throw new IllegalArgumentException(e)
        }
    }

    public static String readUntil(String pattern) {
        try {
            char lastChar = pattern.charAt(pattern.length() - 1);
            StringBuffer sb = new StringBuffer();
            boolean found = false;
            char ch = (char) tin.read();
            while (true) {
                // System.out.print(ch);
                sb.append(ch);
                if (ch == lastChar) {
                    if (sb.toString().endsWith(pattern)) {
                        return sb.toString();
                    }
                }
                ch = (char) tin.read();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
 
    public static void write(String value) {
        try {
            // tout.println(value);
            tout.println(value);
            tout.flush();
            // System.out.println(value);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public static String sendCommand(String command) {
        try {
            write(command);
            return readUntil(prompt + " ");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    def run_command(command, test_id, share = false) {
        try {
            def log_path = (share) ? evidence_log_share_dir : local_dir
            def result = sendCommand(command)
            new File("${log_path}/${test_id}").text = result
        } catch (Exception e) {
            log.error "[Telnet Test] Command error '$command' in ${this.server_name} faild, skip.\n" + e
        }
    }

    def close() {
    }
}

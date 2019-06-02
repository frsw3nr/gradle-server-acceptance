@GrabConfig( systemClassLoader=true )
@Grapes( 
@Grab("commons-net:commons-net:3.3")
)

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
import java.io.Reader

def ses = new TelnetSession()
ses.init_session('192.168.0.254', '', 'console0')
def res0 = ses.run_command('show environment', 'uname')
println "RES0:$res0<EOF>"
def res2 = ses.run_command('', 'null_command1')
println "RES2:$res2<EOF>"
def res3 = ses.run_command("\n\n", 'null_command2')
println "RES3:$res3<EOF>"
def res4 = ses.run_command('show ip route', 'uname')
println "RES4:$res4<EOF>"

class TelnetSession {

    static TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
    static EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
    static SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

    static java.io.InputStream tin;
    static java.io.PrintStream tout;
    static java.io.Reader reader = null;
    // static String prompt = "XSCF>"

    static String prompt = '> '
    static LinkedHashMap<String,String> prompts = ['% ':'user', '$ ':'user', '# ':'root', '> ':'admin']
    static int telnet_session_interval = 1000
    int timeout = 30
    String evidence_log_share_dir
    String local_dir

    TelnetClient telnet

    TelnetSession(test_spec) {
        this.prompt    = test_spec?.prompt ?: '> '
        this.timeout   = test_spec?.timeout ?: 30
        this.local_dir = test_spec?.local_dir ?: '/tmp'
        this.evidence_log_share_dir = test_spec?.evidence_log_share_dir ?: '/tmp'
    }

    def init_session(String ip, String user, String password) {
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
            tin = telnet.getInputStream();
            tout = new PrintStream(telnet.getOutputStream());
            reader = new InputStreamReader(tin);
     
            // Login telnet session
            if (!(user) && user != '') {
                readUntil("login: ");
                write(user);
            }
            readUntil("Password: ");
            write(password);

            // Unify ascii code to avoid multi-byte
            readUntilPrompt();
            write("sh");
            readUntilPrompt();
            write("LANG=C");
            readUntil(prompt);

        } catch (Exception e) {
            println "[Telnet Test] Init test faild.\n" + e
            throw new IllegalArgumentException(e)
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

    public static String readUntil(String pattern) {
        try {
            char lastChar = pattern.charAt(pattern.length() - 1);
            StringBuffer sb = new StringBuffer();
            println "readUntil:$pattern<EOF>"
            boolean found = false;
            while (true) {
                char ch = (char) reader.read();
                if (ch < 0)
                    break;
                sb.append((char) ch);
                // 次のread()が入力をブロックするかも知れない時はmatchチェックする
                if (reader.ready() == false) {
                    if (sb.toString().endsWith(pattern)) {
                        println "BUFFER:$sb<EOF>"
                        String message = sb.toString();
                        return truncate_last_line(message);
                    }
                }
            }
        }
        catch (Exception e) {
            println "[Telnet Test] read session faild.\n" + e
            throw new IllegalArgumentException(e)
        }
        return null;
    }

    public static String readUntilPrompt() {
        try {
            StringBuffer sb = new StringBuffer();
            String message = null;
            int prompt_size = 0;
            while (!message) {
                // System.out.print(ch);
                char ch = (char) tin.read();
                sb.append(ch);
                this.prompts.each { prompt, value ->
                    char lastChar = prompt.charAt(prompt.length() - 1);
                    if (ch == lastChar) {
                        if (sb.toString().endsWith(prompt)) {
                            message = sb.toString();
                            prompt_size = prompt.length()
                        }
                    }
                }
            }
            String result = truncate_last_line(message)
            return result
        }
        catch (Exception e) {
            println "[Telnet Test] read session faild.\n" + e
            throw new IllegalArgumentException(e)
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
            println "[Telnet Test] write session faild.\n" + e
            throw new IllegalArgumentException(e)
        }
    }
 
    public static String sendCommand(String command) {
        try {
            sleep(telnet_session_interval);
            write(command);
            sleep(telnet_session_interval);
            return readUntil(prompt);
            // return readUntilPrompt();
        }
        catch (Exception e) {
            println "[Telnet Test] command '${command}' faild.\n" + e
        }
        return null;
    }

    def run_command(command, test_id, share = false) {
        try {
            def log_path = (share) ? evidence_log_share_dir : local_dir
            def result = sendCommand(command)
            println "COMMAND:$command,RESULT:$result<EOF>"
            new File("${log_path}/${test_id}").text = result
        } catch (Exception e) {
            println "[Telnet Test] Command error '$command' faild, skip.\n" + e
        }
    }

    def close() {
        if (telnet) {
            telnet.disconnect()
        }
    }
}

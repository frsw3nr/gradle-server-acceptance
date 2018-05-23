package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.session.execution.*
import jp.co.toshiba.ITInfra.acceptance.*
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import org.apache.commons.net.telnet.EchoOptionHandler
import org.apache.commons.net.telnet.SuppressGAOptionHandler
import org.apache.commons.net.telnet.TelnetClient
import org.apache.commons.net.telnet.TerminalTypeOptionHandler
import java.io.InputStream
import java.io.PrintStream

import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
@InheritConstructors
class SparcXscfSpecBase extends InfraTestSpec {

    static java.io.InputStream tin;
    static java.io.PrintStream tout;
    static String prompt = "XSCF>"

    String ip
    String os_user
    String os_password
    String work_dir
    int    timeout = 30

    def init() {
        super.init()

        this.ip           = test_platform.test_target.ip
        def os_account    = test_platform.os_account
        this.os_user      = os_account['user']
        this.os_password  = os_account['password']
        this.work_dir     = os_account['work_dir']
        this.timeout      = test_platform.timeout

        println "timeout: ${test_platform.timeout}"
    }

    def init_telnet_session() {
        TelnetClient telnet = new TelnetClient();

        try {
            telnet.setDefaultTimeout(1000 * timeout);
            telnet.connect(this.ip);
            telnet.setSoTimeout(1000 * timeout);
            telnet.setSoLinger(true, 1000 * timeout);

            // Get input and output stream references
            tin = telnet.getInputStream();
            tout = new PrintStream(telnet.getOutputStream());
     
            // Login telnet session
            readUntil("login: ");
            write(this.os_user);
            // write('console');
            readUntil("Password: ");
            write(this.os_password);
            // write('console0');
            readUntil(prompt + " ");
        } catch (Exception e) {
            log.error "[Telnet Test] Init test faild.\n" + e
            throw new IllegalArgumentException(e)
        }
    }

    def setup_exec(TestItem[] test_items) {
    // def setup_exec(LinkedHashMap<String,TestMetric> test_metrics) {
        super.setup_exec()

        init_telnet_session()

        println "test_items:$test_items"
        test_items.each { test_item ->
            def method = this.metaClass.getMetaMethod(test_item.test_id, TestItem)
            if (method) {
                log.debug "Invoke command '${method.name}()'"
                try {
                    long start = System.currentTimeMillis();
                    method.invoke(this, test_item)
                    long elapsed = System.currentTimeMillis() - start
                    log.debug "Finish test method '${method.name}()' in ${this.server_name}, Elapsed : ${elapsed} ms"
                    // test_item.succeed = 1
                } catch (Exception e) {
                    test_item.status(false)
                    log.error "[Telnet Test] Test method '${method.name}()' faild, skip.\n" + e
                }
            }
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

    def run_telnet_command(command, test_id, share = false) {
        try {
            def log_path = (share) ? evidence_log_share_dir : local_dir
            def result = sendCommand(command)
            new File("${log_path}/${test_id}").text = result
        } catch (Exception e) {
            log.error "[Telnet Test] Command error '$command' in ${this.server_name} faild, skip.\n" + e
        }
    }

    def hardconf(test_item) {
        def lines = exec('hardconf') {
            run_telnet_command('showhardconf', 'hardconf')
        }
        println lines
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def cpu_activate(test_item) {
        def lines = exec('cpu_activate') {
            run_telnet_command('showcod -v -s cpu', 'cpu_activate')
        }
        println lines
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def fwversion(test_item) {
        def lines = exec('fwversion') {
            run_telnet_command('version -c xcp -v', 'fwversion')
        }
        println lines
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def network(test_item) {
        def lines = exec('network') {
            run_telnet_command('shownetwork -a', 'network')
        }
        println lines
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def snmp(test_item) {
        def lines = exec('snmp') {
            run_telnet_command('showsnmp', 'snmp')
        }
        println lines
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }
}

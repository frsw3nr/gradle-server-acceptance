package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec

import static groovy.json.JsonOutput.*
import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
// import org.hidetake.groovy.ssh.Ssh
import ch.ethz.ssh2.Connection
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

// import groovy.util.logging.Slf4j
// import groovy.transform.ToString
// import groovy.transform.InheritConstructors
// import org.apache.commons.io.FileUtils.*
// import static groovy.json.JsonOutput.*
// import org.hidetake.groovy.ssh.Ssh
// import org.hidetake.groovy.ssh.session.execution.*
// import jp.co.toshiba.ITInfra.acceptance.*
// import org.apache.commons.net.util.SubnetUtils
// import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import org.apache.commons.net.telnet.EchoOptionHandler
import org.apache.commons.net.telnet.SuppressGAOptionHandler
import org.apache.commons.net.telnet.TelnetClient
import org.apache.commons.net.telnet.TerminalTypeOptionHandler
import java.io.InputStream
import java.io.PrintStream

// import jp.co.toshiba.ITInfra.acceptance.*
// import jp.co.toshiba.ITInfra.acceptance.Document.*
// import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@InheritConstructors
class RouterRTXSpec extends InfraTestSpec {

    static java.io.InputStream tin;
    static java.io.PrintStream tout;
    static String prompt = ">"

    static String mac_vendor_dir = 'template/Router/mac-vendor'
    static String mac_vendor_oui = 'ieee-oui.txt'
    def mac_vendor_db = [:]

    String ip
    String os_user
    String os_password
    String work_dir
    int    timeout = 30

    def init() {
        super.init()

        this.ip           = test_platform.test_target.ip ?: 'unkown'
        def os_account    = test_platform.os_account
        this.os_password  = os_account['password'] ?: 'unkown'
        this.work_dir     = os_account['work_dir'] ?: '/tmp'
        this.timeout      = test_platform.timeout
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
     
            // write('console');
            readUntil("Password: ");
            write(this.os_password);
            readUntil(prompt + " ");
        } catch (Exception e) {
            log.error "[Telnet Test] Init test faild.\n" + e
            throw new IllegalArgumentException(e)
        }
    }

    def init_mac_vendor_oui_db() throws IOException {
        new File("${mac_vendor_dir}/${mac_vendor_oui}").eachLine {
            (it =~ /^([0-9A-F]+)\s+(.+)$/).each { m0, mac, vendor ->
                this.mac_vendor_db[mac] = vendor
            }
        }
        log.info "Read mac vendor file '${mac_vendor_oui}'' : ${this.mac_vendor_db.size()}"
    }

    def get_mac_vendor(String mac) {
        mac = mac.replaceAll(":", "")
        mac = mac.toUpperCase()
        def mac_postfix = mac.take(6)
        return this.mac_vendor_db[mac_postfix] ?: 'unkown'
    }

    def setup_exec(TestItem[] test_items) {
    // def setup_exec(LinkedHashMap<String,TestMetric> test_metrics) {
        super.setup_exec()

        init_mac_vendor_oui_db()
        if (!dry_run) {
            init_telnet_session()
        }

        // println "test_items:$test_items"
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
                    test_item.verify(false)
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

    def trim(str){
        str.replaceAll(/\A[\s:]+/,"").replaceAll(/[\s]+\z/,"")
    }

    def arp(test_item) {
        def lines = exec('arp') {
            run_telnet_command('show arp', 'arp')
        }
        println lines
        def row = 0
        def csv = []
        // Interface      IP address        MAC address       TTL(second)
        // LAN1           192.168.0.27      64:b5:c6:bb:b5:f6  717
        lines.eachLine {
            row ++
            if (row < 3)
                return
            (it =~ /clock_MHz\s+(.+)/).each {m0,m1->
                csv << [m1]
            }
            (it =~ /^(.+?)\s+(.+?)\s+(.+?)\s+(\d+)$/).each {m0,device,ip,mac,ttl->
                def vendor = this.get_mac_vendor(mac)
                csv << [device, ip, mac, vendor]
            }
        }
        println csv
        test_item.results(csv.size())
        def headers = ['interface', 'ip', 'mac', 'vendor']
        test_item.devices(csv, headers)
    }
}

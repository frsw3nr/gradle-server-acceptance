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
class XSCFSpec extends InfraTestSpec {

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

        this.ip           = test_platform.test_target.ip ?: 'unkown'
        def os_account    = test_platform.os_account
        this.os_user      = os_account['user'] ?: 'unkown'
        this.os_password  = os_account['password'] ?: 'unkown'
        this.work_dir     = os_account['work_dir'] ?: '/tmp'
        this.timeout      = test_platform.timeout

        // this.ip          = test_server.ip
        // def os_account   = test_server.os_account
        // this.os_user     = os_account['user']
        // this.os_password = os_account['password']
        // this.work_dir    = os_account['work_dir']
        // this.timeout     = test_server.timeout
    }

    // def init() {
    //     super.init()

    //     this.ip           = test_platform.test_target.ip
    //     def os_account    = test_platform.os_account
    //     this.os_user      = os_account['user']
    //     this.os_password  = os_account['password']
    //     this.work_dir     = os_account['work_dir']
    //     this.timeout      = test_platform.timeout

    //     println "timeout: ${test_platform.timeout}"
    // }

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

    def parse_module(String module, String separator) {
        def module_type = module
        def module_suffix = ''
        (module =~ /^(.+)${separator}(.+)$/).each { m0, _type, _suffix ->
            module_type = _type
            module_suffix = _suffix
        }
        return [module_type, module_suffix]
    }

    def hardconf(test_item) {
        def lines = exec('hardconf') {
            run_telnet_command('showhardconf', 'hardconf')
        }
        // + Serial:TZ11425021; Operator_Panel_Switch:Locked;
        // + System_Power:On; System_Phase:Cabinet Power On;
        // Partition#0 PPAR_Status:Running;
        // MBU Status:Normal; Ver:2351h; Serial:TZ1422A00U  ;
        def module_status = []
        def csvs = []
        def res = [:]
        def moudle_infos = [:].withDefault{[:].withDefault{[]}}
        lines.eachLine {
            // println it
            ( it =~ /^(\w.+?);\s*$/).each {m0, m1 ->
                res['hardconf.system'] = m1
            }
            ( it =~ /^\s+\+\sSerial:(.+?);\s*/).each {m0, m1 ->
                res['hardconf.serial'] = m1
            }
            ( it =~ /(\w.+?) Status:(.+)/).each {m0, module, column_str->
                module = trim(module)
                def module_info = parse_module(module, "#")
                def columns = column_str.split(/;/)
                def stat = columns[0]
                moudle_infos[module_info[0]][stat] << module_info[1]
                if (stat != 'Normal' && stat != 'Running' &&
                    stat != 'ON') {
                    module_status << "$module:$stat" 
                }
                csvs << [module, stat, column_str]
            }
        }
        def headers = ['node', 'status', 'description']
        test_item.devices(csvs, headers)

        moudle_infos.each { module_type, module_statuses -> 
            def result_lines = []
            module_statuses.each { status, modules ->
                if (modules.size() == 1) {
                    result_lines << status
                } else if (modules.size() > 1) {
                    result_lines << "$status:$modules"
                }
            }
            add_new_metric("hardconf.${module_type}", "HWランプ.${module_type}",
                           "${result_lines}", res)
        }
        res['hardconf'] = (module_status.size() > 0) ? "${module_status}" : 'Normal'
        test_item.results(res)
    }

    def cpu_activate(test_item) {
        def lines = exec('cpu_activate') {
            run_telnet_command('showcod -v -s cpu', 'cpu_activate')
        }
        // PROC Permits installed: 16 cores
        // PROC Permits assigned for PPAR 0: 16 [Permanent 16cores]
        def cpu_status = [:].withDefault{0}
        def csvs = []
        def res = [:]
        lines.eachLine {
            ( it =~ /PROC Permits\s+(.+?):\s+(\d+)/).each {m0, module, core->
                def module_info = parse_module(module, ' for ')
                cpu_status[module_info[0]] += core.toInteger()
                add_new_metric("cpu_activate.${module}", "CPU割当て.${module}", core, res)
                csvs << [module, core]
            }
        }
        def headers = ['module', 'core']
        test_item.devices(csvs, headers)

        res['cpu_activate'] = "${cpu_status['assigned']} / ${cpu_status['installed']} Core"
        test_item.results(res)
    }

    def fwversion(test_item) {
        def lines = exec('fwversion') {
            run_telnet_command('version -c xcp -v', 'fwversion')
        }
        def res = [:]
        def module_status = [:]
        def csvs = []
        def xscf = 'unkown'
        // XCP0 (Reserve): 2351
        // CMU           : 02.35.0001
        lines.eachLine {
            ( it =~ /(.+):(.+)/).each {m0, module, version->
                csvs << [module, version]
                module = trim(module)
                version = trim(version)
                version = "'${version}'"
                // Trim "XCP0 (Reserve)"
                (module =~ /^(.+) \(/).each { n0, n1 ->
                    module = n1
                }
                (module =~ /^\#/).each { n0 ->
                    module = ''
                }
                if (module) {
                    module_status[module] = version
                    if (module == 'XSCF') {
                        xscf = version
                    }
                }
            }
        }
        module_status.each { module, status ->
            add_new_metric("fwversion.${module}", "バージョン.${module}", status, res)
        }
        res['fwversion'] = "XSCF : ${xscf}"
        def headers = ['node', 'version']
        test_item.devices(csvs, headers)
        test_item.results(res)
    }

    def network(test_item) {
        // bb#00-lan#0
        //           Link encap:Ethernet  HWaddr B0:99:28:9B:D2:1C
        //           inet addr:10.20.129.20  Bcast:10.20.255.255  Mask:255.255.0.0
        //           UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
        //           RX packets:1848361 errors:0 dropped:0 overruns:0 frame:0
        //           TX packets:24335 errors:0 dropped:0 overruns:0 carrier:0
        //           collisions:0 txqueuelen:1000
        //           RX bytes:141836400 (135.2 MiB)  TX bytes:2422552 (2.3 MiB)
        //           Base address:0xe000

        def lines = exec('network') {
            run_telnet_command('shownetwork -a', 'network')
        }
        def sequence = 0
        def infos = [:].withDefault{[:]}
        def ip_addresses = [:]
        def res = [:]
        lines.eachLine {
            ( it =~ /Link/).each {
                sequence ++
            }
            ( it =~ /inet addr:(.+?)\s/).each {m0, value->
                infos[sequence]['ip'] = value
                ip_addresses[value] = 1
            }
            ( it =~ /HWaddr (.+?)$/).each {m0, value->
                infos[sequence]['mac'] = value
            }
            ( it =~ /Mask:(.+?)$/).each {m0, value->
                infos[sequence]['mask'] = value
            }
        }

        def csv = []
        infos.each { device, items ->
            def columns = [device]
            ['ip', 'mac', 'mask'].each {
                def value = items[it] ?: 'NaN'
                columns.add(value)
            }
            def ip_address = infos[device]['ip']
            if (ip_address && ip_address != '127.0.0.1') {
                test_item.admin_port_list(ip_address, "${device}")
                add_new_metric("network.ip.${device}",   "[${device}] IP", ip_address, res)
                add_new_metric("network.mask.${device}", "[${device}] ネットマスク", infos[device]['mask'], res)
                add_new_metric("network.mac.${device}",  "[${device}] MAC", infos[device]['mac'], res)
            }
            csv << columns
        }
        def headers = ['device', 'ip', 'mac', 'subnet']
        test_item.devices(csv, headers)
        res['network'] = "${ip_addresses}"
        test_item.results(res)
        test_item.verify_text_search('network', ip_addresses.toString())
    }

    def snmp(test_item) {
        def lines = exec('snmp') {
            run_telnet_command('showsnmp', 'snmp')
        }
        // showsnmp

        // Agent Status:       Disabled
        // Agent Port:         161
        // System Location:    Unknown
        // System Contact:     Unknown
        // System Description: Unknown

        // Trap Hosts: None

        // SNMP V1/V2c: None

        // Enabled MIB Modules: None
        // XSCF>
        def infos = [:]
        lines.eachLine {
            ( it =~ /(?i)Agent Status:\s+(.+?)$/).each {m0, value->
                infos['snmp_agent_status'] = value
            }
            ( it =~ /(?i)Agent Port:\s+(.+?)$/).each {m0, value->
                infos['snmp_agent_port'] = "'${value}'"
            }
            ( it =~ /(?i)Trap Hosts:\s+(.+?)$/).each {m0, value->
                infos['snmp_trap_host'] = value
            }
            ( it =~ /SNMP (.+):\s+(.+?)$/).each {m0, m1, value->
                infos['snmp_version'] = "'${value}'"
            }
            // Hostname Port Type Community String Username Auth Encrypt
            // -------- ---- ---- ---------------- -------- ---- ---------
            // host3    162  v3   n/a              yyyyy    SHA  DES
            // host1    62   v1   public           n/a      n/a  n/a
            // host2    1162 v2   public           n/a      n/a  n/a
            ( it =~ /^(\w+?)\s+(\d+?)\s+(.+?)\s+(.+?)\s+(.+?)\s+(.+?)\s+(.+?)$/).each {m0, m1, m2, m3, m4, m5, m6, m7->
                if (m1.size() > 0) {
                    infos['snmp_trap_host'] = m1
                }
                if (m2.size() > 0) {
                    infos['snmp_trap_port'] = "'${m2}'"
                }
                if (m3.size() > 0) {
                    infos['snmp_version'] = "'${m3}'"
                }
                if (m4.size() > 0) {
                    infos['snmp_community'] = m4
                }
            }
        }
        def csv = []
        def columns = []
        def res = [:]
        ['snmp_agent_status', 'snmp_version', 'snmp_agent_port', 'snmp_trap_host', 'snmp_trap_port', 'snmp_community'].each { metric ->
            def value = infos[metric] ?: 'NaN'
            columns.add(value)
            add_new_metric("snmp.${metric}", metric, value, res)
        }
        res['snmp'] = infos['snmp_agent_status'] ?: 'Not found'
        csv << columns
        def headers = ['status', 'version', 'agent_port', 'host', 'host_port', 'community']
        test_item.devices(csv, headers)
        test_item.results(res)
        println res
        test_item.verify_text_search('snmp_status', infos['snmp_agent_status'])
        test_item.verify_text_search_list('snmp_address',   infos['snmp_trap_host'])
        test_item.verify_text_search_list('snmp_community', infos['snmp_community'])
        test_item.verify_text_search_list('snmp_version',   infos['snmp_version'])
    }
}

package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec

import javax.xml.bind.*
import static groovy.json.JsonOutput.*
import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
// import org.hidetake.groovy.ssh.Ssh
import ch.ethz.ssh2.Connection
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*
import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

@Slf4j
@InheritConstructors
class RouterCiscoSpec extends InfraTestSpec {

    static String mac_vendor_dir = 'template/Router/mac-vendor'
    static String mac_vendor_oui = 'ieee-oui.txt'
    def mac_vendor_db = [:]

    String switch_name
    String ip
    String os_user
    String os_password
    String work_dir
    int    timeout = 30

    def init() {
        super.init()

        this.switch_name  = test_platform.test_target.name ?: 'unkown'
        this.ip           = test_platform.test_target.ip ?: 'unkown'
        def os_account    = test_platform.os_account
        this.os_user      = os_account['user'] ?: 'unkown'
        this.os_password  = os_account['password'] ?: 'unkown'
        this.work_dir     = os_account['work_dir'] ?: '/tmp'
        this.timeout      = test_platform.timeout
    }

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()

        def con
        def result
        init_mac_vendor_oui_db()
        if (!dry_run) {
            con = new Connection(this.ip, 22)
            con.connect()
            result = con.authenticateWithPassword(this.os_user, this.os_password)

            if (!result) {
                println "connect failed"
                return
            }
        }
        test_items.each {
            def method = this.metaClass.getMetaMethod(it.test_id, Object, TestItem)
            if (method) {
                log.debug "Invoke command '${method.name}()'"
                try {
                    long start = System.currentTimeMillis();
                    method.invoke(this, con, it)
                    long elapsed = System.currentTimeMillis() - start
                    log.debug "Finish test method '${method.name}()' in ${this.server_name}, Elapsed : ${elapsed} ms"
                    // it.succeed = 1
                } catch (Exception e) {
                    it.verify(false)
                    log.error "[SSH Test] Test method '${method.name}()' faild, skip.\n" + e
                }
            }
        }
        if (!dry_run) {
            con.close()
        }
        test_items.each { test_item ->
            if (test_item.test_id == 'logon_test') {
                _logon_test(test_item)
            }
        }
    }

    def run_ssh_command(con2, command, test_id, share = false) {
        def con
        try {
            if (!dry_run) {
                con = new Connection(this.ip, 22)
                con.connect()
                if (!con.authenticateWithPassword(this.os_user, this.os_password)) {
                    println "connect failed"
                    return
                }
            }
            def log_path = (share) ? evidence_log_share_dir : local_dir

            def session = con.openSession()
            session.execCommand command
            def result = session.stdout.text
            new File("${log_path}/${test_id}").text = result
            session.close()

            if (!dry_run) {
                con.close()
            }
            // println("SLEEP")
            // sleep(20000)
            return result

        } catch (Exception e) {
            log.error "[SSH Test] Command error '$command' in ${this.server_name} faild, skip.\n" + e
        }
    }

    def finish() {
        super.finish()
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
        mac = mac.replaceAll(/[\.:]/, "")
        mac = mac.toUpperCase()
        def mac_postfix = mac.take(6)
        return this.mac_vendor_db[mac_postfix] ?: 'unkown'
    }


// show ntp associations
// %NTP is not enabled.

    def version(session, test_item) {
        def lines = exec('version') {
            run_ssh_command(session, 'show version', 'version')
        }
        def row = 0
        def version_name = 'unkown'
        lines.eachLine {
            row ++
            if (row == 1) {
                version_name = it
            }
        }
        test_item.results(version_name)
    }

    def inventory(session, test_item) {
        def lines = exec('inventory') {
            run_ssh_command(session, 'show inventory', 'inventory')
        }
        println lines
        def row = 0
        def csv = []
        // NAME: "3745 chassis", DESCR: "3745 chassis"
        // PID:                   , VID: 2.0, SN: XXXXXXXXXXXXXX
        def inventory_name
        def serial
        lines.eachLine {
            row ++
            (it =~ /^NAME: "(.+?)"/).each { m0, m1 ->
                inventory_name = m1
            }
            (it =~ /SN: (.+)/).each { m0, m1 ->
                serial = m1
                csv << [this.switch_name, inventory_name, serial]
            }
        }
        test_item.results(csv.size())
        def headers = ['switch_name', 'inventory', 'serial']
        test_item.devices(csv, headers)
    }

    def ip_route(session, test_item) {
        def lines = exec('ip_route') {
            run_ssh_command(session, 'show ip route', 'ip_route')
        }
        def row = 0
        def csv = []
        lines.eachLine {
            println it
            row ++
            csv << [it]
        }
        test_item.results(csv.size())
        def headers = ['body']
        test_item.devices(csv, headers)
    }

    def arp(session, test_item) {
        def lines = exec('arp') {
            run_ssh_command(session, 'show arp', 'arp')
        }
        def row = 0
        def csv = []
        // 0         1                2          3               4      5
        // Protocol  Address          Age (min)  Hardware Addr   Type   Interface
        // Internet  10.1.1.1                -   c401.173c.0001  ARPA   FastEthernet0/1
        // Internet  10.1.1.2              145   c402.07b4.0000  ARPA   FastEthernet0/1
        lines.eachLine {
            row ++
            def values = it.split(/\s+/)
            if (values.size() != 6)
                return
            def device = values[5]
            def ip     = values[1]
            def mac    = values[3]
            def vendor = this.get_mac_vendor(mac)
            csv << [device, ip, mac, vendor]
            test_item.port_list(ip, device, mac, vendor, this.switch_name)
        }
        test_item.results(csv.size())
        def headers = ['interface', 'ip', 'mac', 'vendor']
        test_item.devices(csv, headers)
    }

    def snmp_trap(session, test_item) {
        def lines = exec('snmp_trap') {
            run_ssh_command(session, 'show snmp host', 'snmp_trap')
        }
        println lines
        def status = ''
        lines.eachLine {
            if (it.size() > 0) {
                status += it + ' '
            }
        }
        println status
        test_item.results(status)
    }

    def ntp(session, test_item) {
        def lines = exec('ntp') {
            run_ssh_command(session, 'show ntp associations', 'ntp')
        }
        println lines
        def row = 0
        def ntp_status = 'unkown'
        lines.eachLine {
            (it =~ /^([\*%].+)$/).each { m0, m1 ->
                ntp_status = m1
            }
            row ++
        }
        test_item.results(ntp_status)
    }
}

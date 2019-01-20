package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec

import javax.xml.bind.*
import static groovy.json.JsonOutput.*
import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
// import org.hidetake.groovy.ssh.Ssh
import ch.ethz.ssh2.Connection
import net.sf.expectit.Expect
import net.sf.expectit.ExpectBuilder
import static net.sf.expectit.matcher.Matchers.contains
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
    def subnets = []

    String switch_name
    String ip
    String os_user
    String os_password
    String admin_password
    String work_dir
    int    timeout = 30

    def init() {
        super.init()

        this.switch_name    = test_platform.test_target.name ?: 'unkown'
        this.ip             = test_platform.test_target.ip ?: 'unkown'
        def os_account      = test_platform.os_account
        this.os_user        = os_account['user'] ?: 'unkown'
        this.os_password    = os_account['password'] ?: 'unkown'
        this.admin_password = os_account['admin_password'] ?: 'unkown'
        this.work_dir       = os_account['work_dir'] ?: '/tmp'
        this.timeout        = test_platform.timeout
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
    }

    def run_ssh_command(con2, command, test_id, admin_mode = false, share = false) {
        def con
        def ok_prompt = (admin_mode) ? "#" : ">";
        try {
            con = new Connection(this.ip, 22)
            con.connect()
            if (!con.authenticateWithPassword(this.os_user, this.os_password)) {
                println "connect failed"
                return
            }
            def log_path = (share) ? evidence_log_share_dir : local_dir

            def session = con.openSession()
            session.requestDumbPTY();
            session.startShell();
            Expect expect = new ExpectBuilder()
                    .withOutput(session.getStdin())
                    .withInputs(session.getStdout(), session.getStderr())
                            // .withEchoOutput(System.out)
                            // .withEchoInput(System.out)
                    .build();
            if (admin_mode) {
                expect.sendLine('enable');
                expect.expect(contains('Password:'));
                expect.sendLine(this.admin_password);
            }
            expect.expect(contains(ok_prompt)); 
            expect.sendLine('terminal length 0'); 
            expect.expect(contains(ok_prompt)); 
            expect.sendLine(command); 
            String result = expect.expect(contains(ok_prompt)).getBefore(); 

            // println "COMMAND:${command}\nRESULT:${result}\nLEN:${result.size()}"
            new File("${log_path}/${test_id}").text = result
            session.close()
            con.close()
            expect.close()

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


    def version(session, test_item) {
        def lines = exec('version') {
            run_ssh_command(session, 'show version', 'version')
        }
        // println lines
        def row = 0
        def version_name = 'unkown'
        def infos = [:]
        def disks = [:]
        // Cisco IOS Software, 3700 Software (C3745-ADVENTERPRISEK9_IVS-M), 
        // Version 12.4(15)T8, RELEASE SOFTWARE (fc3)
        // Cisco 3745 (R7000) processor (revision 2.0) with 249856K/12288K bytes of memory.
        // Processor board ID FTX0945W0MY
        lines.eachLine {
            row ++
            (it =~ /^(.+), Version (.+?),/).each {m0, m1, m2 ->
                infos['ios.rom'] = m1
                infos['version'] = m2
            }
            (it =~ /with (.+?) bytes of memory/).each {m0, m1 ->
                infos['ios.memory'] = m1
            }
            (it =~ /^Processor board ID (.+)$/).each {m0, m1 ->
                infos['ios.serial'] = m1
            }
            (it =~ /^(.+?) bytes of (.+?) CompactFlash/).each {m0, m1, m2 ->
                disks[m2] = m1
            }
        }
        infos['ios.disk'] = "${disks}"
        test_item.results(infos)
    }

    def inventory(session, test_item) {
        def lines = exec('inventory') {
            run_ssh_command(session, 'show inventory', 'inventory')
        }
        // println lines
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
        def csv   = []
        def cidrs = []
        lines.eachLine {
            (it =~ /\s([0-9].+?) is (.+)$/).each { m0, m1, m2 ->
                try {
                    SubnetInfo subnet = new SubnetUtils(m1).getInfo()
                    this.subnets << subnet
                    cidrs << m1
                } catch (IllegalArgumentException e) {
                    log.info "[RouterIOS] subnet convert : ${m1}\n" + e
                }
            }
            row ++
            csv << [it]
        }
        test_item.results("${cidrs}")
        def headers = ['body']
        test_item.devices(csv, headers)
    }

    SubnetInfo get_subnet_network(String ip) {
        def subnet_network = null
        subnets.find { subnet ->
            if (subnet.isInRange(ip)) {
                subnet_network = subnet
                return true
            }
        }
        return subnet_network
    }

    def arp(session, test_item) {
        def lines = exec('arp') {
            run_ssh_command(session, 'show arp', 'arp')
        }
        if (this.subnets.size() == 0)
            this.ip_route(session, test_item)
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
            def subnet_network = this.get_subnet_network(ip)
            def netmask = subnet_network?.getNetmask()
            def subnet  = subnet_network?.getNetworkAddress() 
            csv << [device, ip, netmask, subnet, mac, vendor]
            if (ip && ip != '127.0.0.1') {
                test_item.port_list(ip, null, mac, vendor, this.switch_name,
                                    netmask, subnet, device)
            }
        }
        test_item.results(csv.size())
        // println csv
        def headers = ['interface', 'ip', 'netmask', 'subnet', 'mac', 'vendor']
        test_item.devices(csv, headers)
    }

    // Notification host: 192.168.0.20 udp-port: 162   type: trap
    // user: public    security model: v2c
    def snmp_trap(session, test_item) {
        def lines = exec('snmp_trap') {
            run_ssh_command(session, 'show snmp host', 'snmp_trap', true)
        }
        def infos = [:]
        // println lines
        lines.eachLine {
            (it=~/host: (.+)\s+udp-port: (.+)\s+type/).each { m0, m1, m2 ->
                infos["snmp_trap.host"] = m1
                infos["snmp_trap.port"] = m2
            }
            (it=~/user: (.+?)\s+security model: (.+)/).each { m0, m1, m2 ->
                infos["snmp_trap.community"] = m1
                infos["snmp_trap.version"] = m2
            }
        }
        def check_count = infos.size()
        infos['snmp_trap'] = (check_count == 4) ? "OK" : "NG"
        test_item.results(infos)
    }

// c3745#show ip interface
// FastEthernet0/0 is up, line protocol is up
//   Internet address is 192.168.0.33/24

    def ntp(session, test_item) {
        def lines = exec('ntp') {
            run_ssh_command(session, 'show ntp associations', 'ntp')
        }
        // println lines
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

    def ip_interface(session, test_item) {
        def lines = exec('ip_interface') {
            run_ssh_command(session, 'show ip interface', 'ip_interface')
        }
        def row = 0
        def csv = []
        def infos = [:].withDefault{[:]}
        // FastEthernet0/0 is up, line protocol is up
        //   Internet address is 192.168.0.33/24
        //   Broadcast address is 255.255.255.255

        // FastEthernet1/0 is up, line protocol is up
        //   Internet address is 10.1.2.1/24
        //   Broadcast address is 255.255.255.255
        //   Address determined by non-volatile memory
        def port_no = null
        lines.eachLine {
            row ++
            (it =~ /^(\w.+?) (.+?),/).each { m0, m1, m2 ->
                // println "port_no: $m1, $m2"
                port_no = m1
                infos[port_no]['status'] = m2
            }
            (it =~ /^  Internet address is (\w.+?)$/).each { m0, m1 ->
                // println "IP: $m1"
                try {
                    SubnetInfo subnet = new SubnetUtils(m1).getInfo()
                    infos[port_no]['ip']             = subnet?.getAddress()
                    infos[port_no]['netmask']        = subnet?.getNetmask()
                    infos[port_no]['subnet_address'] = subnet?.getNetworkAddress() 
                } catch (IllegalArgumentException e) {
                    log.info "[RouterRTX] subnet convert : ${m2}\n" + e
                }
            }
        }
        infos.each { device, info ->
            def columns = [device]
            ['ip', 'netmask', 'subnet_address', 'status'].each {
                columns.add(info[it] ?: 'NaN')
            }
            csv << columns
            if (info.containsKey('ip')) {
                test_item.lookuped_port_list(info['ip'], device,
                                             null, null, this.switch_name,
                                             info['netmask'], info['subnet_address'], device)
            }
        }
        test_item.results(csv.size())
        def headers = ['device', 'ip', 'netmask', 'subnet_address', 'status']
        test_item.devices(csv, headers)
    }
}

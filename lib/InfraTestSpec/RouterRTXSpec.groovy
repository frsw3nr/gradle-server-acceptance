package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec

import static groovy.json.JsonOutput.*
import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
// import org.hidetake.groovy.ssh.Ssh
import ch.ethz.ssh2.Connection
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.Unix.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.Unix.*
import jp.co.toshiba.ITInfra.acceptance.*
import sun.net.util.IPAddressUtil
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import org.apache.commons.net.telnet.EchoOptionHandler
import org.apache.commons.net.telnet.SuppressGAOptionHandler
import org.apache.commons.net.telnet.TelnetClient
import org.apache.commons.net.telnet.TerminalTypeOptionHandler
import java.io.InputStream
import java.io.PrintStream

@Slf4j
@InheritConstructors
class RouterRTXSpec extends InfraTestSpec {

    static String prompt = "> "

    static String mac_vendor_dir = 'template/Router/mac-vendor'
    static String mac_vendor_oui = 'ieee-oui.txt'
    def mac_vendor_db = [:]

    String switch_name
    String ip
    String os_user
    String os_password
    Boolean use_telnet = true
    String work_dir
    int    timeout = 30
    def    subnets = []

    def init() {
        super.init()

        this.switch_name  = test_platform.test_target.name ?: 'unkown'
        this.ip           = test_platform.test_target.ip ?: 'unkown'
        def os_account    = test_platform.os_account
        this.os_password  = os_account['password'] ?: 'unkown'
        this.work_dir     = os_account['work_dir'] ?: '/tmp'
        this.timeout      = test_platform.timeout
        this.timeout      = test_platform.timeout
        this.prompt       = '> '
        this.use_telnet   = os_account['use_telnet'] ?: true
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
        def con = (this.use_telnet) ? new TelnetSession(this) : new SshSession(this)
        // println "PROMPT:${con.prompt}"
        if (!dry_run) {
            con.init_session(this.ip, '', this.os_password)
        }

        // println "test_items:$test_items"
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
    // test_items.each { test_item ->
    //         def method = this.metaClass.getMetaMethod(test_item.test_id, TestItem)
    //         if (method) {
    //             log.debug "Invoke command '${method.name}()'"
    //             try {
    //                 long start = System.currentTimeMillis();
    //                 method.invoke(this, test_item)
    //                 long elapsed = System.currentTimeMillis() - start
    //                 log.debug "Finish test method '${method.name}()' in ${this.server_name}, Elapsed : ${elapsed} ms"
    //                 // test_item.succeed = 1
    //             } catch (Exception e) {
    //                 test_item.verify(false)
    //                 log.error "[Telnet Test] Test method '${method.name}()' faild, skip.\n" + e
    //             }
    //         }
    //     }
    }

    def trim(str){
        str.replaceAll(/\A[\s:]+/,"").replaceAll(/[\s]+\z/,"")
    }

    def config(session, test_item) {
        def lines = exec('config') {
            session.run_command('show config', 'config')
        }
        def row = 0
        def infos = [:]
        def ntp_hosts = []
        def csv = []
        lines.eachLine {
            row ++
            // # RTX1000 Rev.8.01.29 (Fri Apr 15 11:50:44 2011)
            (it =~ /^#\s+(.+)(Rev.+?) /).each {m0, m1, m2 ->
                infos['config'] = m1
                infos['config.version'] = m2
            }
            (it =~ /^snmp (host|community|trap host) (.+)$/).each {
                m0, m1, m2 ->
                infos["snmp.${m1}"] = m2
            }
            // schedule at 1 * 01:00 * ntpdate ntp.nict.jp
            // schedule at 2 * 13:00 * ntpdate ntp.jst.mfeed.ad.jp
           (it =~ /^schedule (.+?) ntpdate (.+)$/).each {
                m0, m1, m2 ->
                ntp_hosts << m2
            }
            // ip lan1 address 192.168.0.254/24
           (it =~ /^ip (.+?) address (.+)$/).each {
                m0, m1, m2 ->
                try {
                    SubnetInfo subnet = new SubnetUtils(m2).getInfo()
                    def netmask = subnet?.getNetmask()
                    def subnet_address = subnet?.getNetworkAddress() 
                    test_item.lookuped_port_list(subnet.getAddress(), m1,
                                                 null, null, this.switch_name,
                                                 netmask, subnet_address, m1)
                } catch (IllegalArgumentException e) {
                    log.info "[RouterRTX] subnet convert : ${m2}\n" + e
                }
            }

            csv << [it]
        }
        infos['ntpupdate'] = "${ntp_hosts}"
        test_item.results(infos)
        def headers = ['message']
        test_item.devices(csv, headers)
    }

// show environment
// RTX1000 Rev.8.01.29 (Fri Apr 15 11:50:44 2011)
// RTX1000 BootROM Ver. 1.04
//   main:  RTX1000 ver=b0 serial=N14005526 MAC-Address=00:a0:de:27:02:eb MAC-Addr
// ess=00:a0:de:27:02:ec MAC-Address=00:a0:de:27:02:ed
// CPU:   4%(5sec)   3%(1min)   3%(5min)    Memory: 27% used
// Firmware: internal  Config. file: 0
// Boot time: 1984/02/17 03:41:27 +09:00
// Current time: 2019/01/13 04:47:16 +09:00
// Elapsed time from boot: 12749days 01:05:49
// Security Class: 1, Type: ON, TELNET: OFF

    def environment(session, test_item) {
        def lines = exec('environment') {
            session.run_command('show environment', 'environment')
        }
        def row = 0
        def infos = [:]
        def timestamps = []
        lines.eachLine {
            row ++
            //   main:  RTX1000 ver=b0 serial=N14005526
            (it =~ /serial=(.+?)\s/).each { m0, m1 ->
                infos['environment.serial'] = m1 
            }
            (it =~ /^Firmware: (.+)$/).each { m0, m1 ->
                infos['environment.firmware'] = m1 
            }
            (it =~ /^Security Class: (.+)$/).each { m0, m1 ->
                infos['environment.security_class'] = m1 
            }
            (it =~ /^(Boot|Current) time: (.+)$/).each { m0, m1, m2 ->
                timestamps << "${m1}:${m2}"
            }
        }
        infos['environment'] = "${timestamps}"
        // println "$infos"
        test_item.results(infos)
    }

    def ip_route(session, test_item) {
        def lines = exec('ip_route') {
            session.run_command('show ip route', 'ip_route')
        }
        def row = 0
        def csv   = []
        def cidrs = []
        lines.eachLine {
            row ++
            if (row <= 2)
                return
            def values = it.split(/\s+/)
            if (values.size() >= 4) {
                csv << values
                def cidr = values[0]
                try {
                    SubnetInfo subnet = new SubnetUtils(cidr).getInfo()
                    this.subnets << subnet
                    cidrs << cidr
                } catch (IllegalArgumentException e) {
                    log.info "[RouterRTX] subnet convert : ${cidr}\n" + e
                }
            }
        }
        test_item.results("$cidrs")
        def headers = ['Destination', 'Gateway', 'Interface', 'Info']
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
            session.run_command('show arp', 'arp')
        }
        if (subnets.size() == 0)
            this.ip_route(test_item)

        def row = 0
        def csv = []
        // Interface      IP address        MAC address       TTL(second)
        // LAN1           192.168.0.27      64:b5:c6:bb:b5:f6  717
        lines.eachLine {
            row ++
            if (row < 3)
                return
            (it =~ /^(.+?)\s+(.+?)\s+(.+?)\s+(\d+)$/).each {m0,device,ip,mac,ttl->
                def vendor = this.get_mac_vendor(mac)
                def subnet_network = this.get_subnet_network(ip)
                def netmask = subnet_network?.getNetmask()
                def subnet  = subnet_network?.getNetworkAddress() 
                // println "IP:${ip}, ${subnet_network}"
                csv << [device, ip, netmask, subnet,  mac, vendor]

                if (ip && ip != '127.0.0.1') {
                    test_item.port_list(ip, null, mac, vendor, this.switch_name,
                                        netmask, subnet, device)
                }
            }
        }
        test_item.results(csv.size())
        def headers = ['interface', 'ip', 'netmask', 'subnet', 'mac', 'vendor']
        test_item.devices(csv, headers)
    }
}

package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.session.execution.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

@Slf4j
@InheritConstructors
class EternusSpec extends LinuxSpecBase {

    String ip
    String os_user
    String os_password
    int    timeout = 30

    def init() {
        super.init()

        this.ip           = test_platform.test_target.ip ?: 'unkown'
        def os_account    = test_platform.os_account
        this.os_user      = os_account['user'] ?: 'unkown'
        this.os_password  = os_account['password'] ?: 'unkown'
    }

    def finish() {
        super.finish()
    }

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()

        test_items.each { test_item ->
            def ssh = Ssh.newService()
            ssh.remotes {
                ssh_host {
                    host       = this.ip
                    port       = 22
                    user       = this.os_user
                    password   = this.os_password
                    knownHosts = allowAnyHosts
                }
            }
            ssh.settings {
                dryRun     = this.dry_run
                timeoutSec = this.timeout
            }
            ssh.run {
                session(ssh.remotes.ssh_host) {

                    def method = this.metaClass.getMetaMethod(test_item.test_id, Object, TestItem)
                    if (method) {
                        log.debug "Invoke command '${method.name}()'"
                        try {
                            long start = System.currentTimeMillis();
                            method.invoke(this, delegate, test_item)
                            long elapsed = System.currentTimeMillis() - start
                            log.debug "Finish test method '${method.name}()' in ${this.server_name}, Elapsed : ${elapsed} ms"
                            // test_item.succeed = 1
                        } catch (Exception e) {
                            test_item.status(false)
                            log.error "[SSH Test] Test method '${method.name}()' faild, skip.\n" + e
                        }
                    }
                }
            }
        }
    }

    def run_script = { String command, Closure closure ->
        if (mode == RunMode.prepare) {
            // Trim line endings
            command = command.replaceAll(/(\s|\r|\n)*$/, "")
            log.debug "Invoke WMI command : ${command}"
            return command
        } else {
            return closure.call()
        }
    }

    // ToDo: 各メソッドで result 結果のパーサー実装
    def run_eternus_show_command(session, test_item, command) {
        def lines = exec(test_item.test_id) {
            def result = session.execute command
            new File("${local_dir}/${test_item.test_id}").text = result
            return result
        }
        def headers = ['Result']
        def csv = []
        lines.eachLine {
            if (!(it =~ /^CLI>/) && it.size() > 0) {
                csv << [it]
            }
        }
        test_item.devices(csv, headers)
        test_item.results(lines.size().toString())
    }

    def status(session, test_item) {
        def lines = exec('status') {
            def result = session.execute 'show status'
            new File("${local_dir}/status").text = result
            return result
        }
        def result = 'NG'
        lines.eachLine {
            (it =~ /^Summary Status\s+(.+)$/).each { m0, m1->
                result = m1
            }
        }
        test_item.results(result)
    }

    def enclosure_status(session, test_item) {
        def lines = exec('enclosure_status') {
            def result = session.execute 'show enclosure-status'
            new File("${local_dir}/enclosure_status").text = result
            return result
        }
        def results = [:]
        lines.eachLine {
            (it =~ /^(.+)\s+\[(.+)\]$/).each { m0, item, value ->
                item = item.replaceAll(/\s+/,"")
                if (item=='SerialNumber') {
                    value = "'" + value + "'"
                }
                results["enclosure.${item}"] = value
            }
        }
        results['enclosure_status'] = results['enclosure.Status']
        test_item.results(results)
        test_item.verify_text_search('enclosure_status', results['enclosure.Status'])
    }


    def fru_ce(session, test_item) {
        def lines = exec('fru_ce') {
            def result = session.execute 'show fru-ce'
            new File("${local_dir}/fru_ce").text = result
            return result
        }
        def results = [:]
        def port_infos = [:].withDefault{[:]}
        def status = 'Normal'

        lines.eachLine {
            (it =~ /^CM#(\d+) Information/).each {m0, m1 ->
                status = "CPU.${m1}"
            }
            (it =~ /^CM#(\d+) Internal Parts Status/).each {m0, m1 ->
                status = "CPU.${m1}"
            }
            (it =~ /^CM#(\d+) CA#(\d+) Port#(\d+) Information/).each {m0, m1, m2, m3 ->
                status = "Port.${m1}.${m2}.${m3}"
            }
            (it =~ /^(.+)\s+\[(.+)\]$/).each { m0, item, value ->
                item = item.replaceAll(/\s+/,"")
                results["${status}.${item}"] = value
                (status =~ /^Port/).each {
                    port_infos[status][item] = value
                }
            }
        }
        test_item.results(results)

        def headers = [ 'PortType','PortMode','Status','Connection','TransferRate',
                        'LinkStatus','PortWWN','NodeWWN','HostAffinity',
                        'HostResponse','SFPType'];
        def csv = []
        port_infos.each { port, port_info ->
            def values = [port]
            headers.each {
                values << port_info[it] ?: 'Unkown'
            }
            csv << values
        }
        test_item.devices(csv, ['Port'] + headers)
    }

    def disks(session, test_item) {
        def lines = exec('disks') {
            def result = session.execute 'show disks'
            new File("${local_dir}/disks").text = result
            return result
        }

        // Location      Status                        Size    Type                Speed(rpm) Usage               Health(%)
        // ------------- ----------------------------- ------- ------------------- ---------- ------------------- ---------
        // CE-Disk#0     Available                       600GB 2.5 Online               10000 Data                        -
        // CE-Disk#1     Available                       600GB 2.5 Online               10000 Data                        -
        // CE-Disk#2     Available                       600GB 2.5 Online               10000 Data                        -
        def results = [:].withDefault{0}
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?) (-.+?)$/).each {
                m0, m1, m2, m3, m4, m5, m6, m7 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                         m5.size(), m6.size(), m7.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.+)$/).each {
                    m0, m1, m2, m3, m4, m5, m6, m7 ->
                    csv << [m1, m2, m3, m4, m5, m6, m7]*.trim()
                    results[m3.trim()] += 1
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['Location', 'Status', 'Size', 'Type', 'Speed', 'Usage', 'Health']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def hardware_information(session, test_item) {
        run_eternus_show_command(session, test_item, 'show hardware-information')
        def lines = exec('hardware_information') {
            def result = session.execute 'show hardware-information'
            new File("${local_dir}/hardware_information").text = result
            return result
        }

        def results = [:]
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^Controller Enclosure\s+(\d+)\s/).each { m0, m1 ->
                results['Serial'] = m1
            }
            (it =~ /^Component/).each {
                row = 0
            }
            if (row > 0) {
                def values = it.split(/\s+/)
                if (values.size() == 4) {
                    csv << values
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['Component', 'Part', 'Serial', 'Version']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def raid_groups(session, test_item) {
        run_eternus_show_command(session, test_item, 'show raid-groups')
        def lines = exec('raid_groups') {
            def result = session.execute 'show raid-groups'
            new File("${local_dir}/raid_groups").text = result
            return result
        }

        // RAID  Group           RAID     Assigned  Status                          Total        Free
        // No.  Name             Level    CM                                        Capacity(MB) Capacity(MB)
        // ---- ---------------- -------- --------- ------------------------------- ------------ ------------
        //    0 LUN_R#000        RAID5    CM#0      Available                            2236416         4096
        //    1 LUN_R#001        RAID5    CM#1      Available                            2236416         4096
        //    2 LUN_R#002        RAID5    CM#0      Available                            2236416         4096
        //    3 LUN_R#003        RAID5    CM#1      Available                            2236416         4096
        //    4 LUN_R#004        RAID5    CM#0      Available                            1118208         2048
        def results = [:].withDefault{0}
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
                m0, m1, m2, m3, m4, m5, m6, m7 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                         m5.size(), m6.size(), m7.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.+)$/).each {
                    m0, m1, m2, m3, m4, m5, m6, m7 ->
                    csv << [m1, m2, m3, m4, m5, m6, m7]*.trim()
                    results["${m3.trim()} ${m6.trim()}MB"] += 1
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['No', 'Group', 'RAIDLevel', 'CMD', 'Status', 'Total', 'Free']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def raid_groups_detail(session, test_item) {
        def lines = exec('raid_groups_detail') {
            def lun_lists = session.execute 'show raid-groups'
            def raid_groups = []
            lun_lists.eachLine {
                def lun_list = it.trim()
                (lun_list=~/^(\d+) /).each { m0, m1 ->
                    raid_groups << m1
                }
            }
            def command = 'show raid-groups -rg-number ' + raid_groups.join(',')
            def result = session.execute command
            new File("${local_dir}/raid_groups_detail").text = result
            return result
        }
        def headers = ['Result']
        def csv = []
        lines.eachLine {
            if (!(it =~ /^CLI>/) && it.size() > 0) {
                csv << [it]
            }
        }

        test_item.devices(csv, headers)
        test_item.results("${csv.size()} row")
    }

    def volumes(session, test_item) {
        def lines = exec('volumes') {
            def result = session.execute 'show volumes'
            new File("${local_dir}/volumes").text = result
            return result
        }

        // Volume                                 Status                    Type              RG or TPP or FTRP     Size(MB)  Copy
        // No.   Name                                                                         No.  Name                       Protection
        // ----- -------------------------------- ------------------------- ----------------- ---- ---------------- --------- ----------
        //     0 LUN_V#000                        Available                 Standard             0 LUN_R#000          2232320 Disable
        //     1 LUN_V#001                        Available                 Standard             1 LUN_R#001          2232320 Disable
        //     2 LUN_V#002                        Available                 Standard             2 LUN_R#002          2232320 Disable
        //     3 LUN_V#003                        Available                 Standard             3 LUN_R#003          2232320 Disable
        //     4 LUN_V#004                        Available                 Standard             4 LUN_R#004          1116160 Disable
        def results = [:].withDefault{0}
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
                m0, m1, m2, m3, m4, m5, m6, m7, m8 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                         m5.size(), m6.size(), m7.size(), m8.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.{${csize[6]}}) (.+)$/).each {
                    m0, m1, m2, m3, m4, m5, m6, m7, m8 ->
                    csv << [m1, m2, m3, m4, m5, m6, m7, m8]*.trim()
                    results["${m7.trim()}MB"] += 1
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['No', 'Name', 'Status', 'Type', 'RAIDGrNo', 'RAIDGrName', 'Size', 'CopyProtection']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def fc_parameters(session, test_item) {
        def lines = exec('fc_parameters') {
            def result = session.execute 'show fc-parameters'
            new File("${local_dir}/fc_parameters").text = result
            return result
        }
        def results = [:].withDefault{0}
        def csv = []
        def csize = []
        def row   = -1
        // CLI> show fc-parameters
        // Port                          CM#0 CA#0 Port#0       CM#0 CA#0 Port#1
        // Port Mode                     CA                     CA
        // Connection                    FC-AL                  FC-AL
        // Loop ID Assign                Manual(0x00)           Manual(0x10)
        lines.eachLine {
            (it =~ /^(Port.* ?) (CM.* ?) (CM.*?)$/).each {
                m0, m1, m2, m3 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}})(.*)$/).each {
                    m0, m1, m2, m3 ->
                    csv << [m1, m2, m3]*.trim()
                }
            }
            if (row >= 0)
                row ++;

            (it=~/^Connection\s+(.+?)\s+(.+?)\s/).each {m0, m1, m2 ->
                results[m1] += 1
                results[m2] += 1
            }
        }
        def headers = ['Item', 'Value1', 'Value2']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def host_affinity(session, test_item) {
        def lines = exec('host_affinity') {
            def result = session.execute 'show host-affinity'
            new File("${local_dir}/host_affinity").text = result
            return result
        }

        // Port Group           Host Response        LUN Group             LUN Mask
        // No. Name             No. Name             No.  Name             Group No.
        // --- ---------------- --- ---------------- ---- ---------------- ---------
        //   0 rx2530m2-08        0 Default             0 rx2530m2-08              -
        def results = [:]
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
                m0, m1, m2, m3, m4, m5, m6, m7 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                         m5.size(), m6.size(), m7.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.+)$/).each {
                    m0, m1, m2, m3, m4, m5, m6, m7 ->
                    csv << [m1, m2, m3, m4, m5, m6, m7]*.trim()
                    results['GroupName'] = m2.trim()
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['PortNo', 'PortName', 'HostNo', 'Response', 'LUN', 'LUNGroup', 'LUNMaskGroup']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def users(session, test_item) {
        def lines = exec('users') {
            def result = session.execute 'show users'
            new File("${local_dir}/users").text = result
            return result
        }

        // User Name                        User Role        Availability SSH Public Key
        // -------------------------------- ---------------- ------------ ----------------
        // f.ce                             Maintainer       [Enable ]    [Not Registered]
        // root                             Admin            [Enable ]    [Not Registered]
        // snmpuser                         Software         [Enable ]    [Not Registered]
        def results = []
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
                m0, m1, m2, m3, m4 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size(), m4.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.+)$/).each {
                    m0, m1, m2, m3, m4 ->
                    csv << [m1, m2, m3, m4]*.trim()
                    results << m1.trim()
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['UserName', 'UserRole', 'Availability', 'SSHPublicKey']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

// MNT Port
// Master IP Address   [10.20.2.52]
// Slave IP Address    [0.0.0.0]
// Subnet Mask         [255.255.0.0]
// Gateway             [10.20.254.200]
// Primary DNS         [0.0.0.0]
// Secondary DNS       [0.0.0.0]

// <MAC Address>
// CM#0                [B0:AC:FA:A0:81:D4]
// CM#1                [B0:AC:FA:A0:81:CE]

    def network(session, test_item) {
        def lines = exec('network') {
            def result = session.execute 'show network'
            new File("${local_dir}/network").text = result
            return result
        }
        def net_ip     = [:]
        def net_subnet = [:]
        def net_route  = [:]
        def csv = []
        def port    = 'Unkown'
        def ip_info = 'Unkown'
        lines.eachLine {
            (it=~/^(.+) Port$/).each {m0, m1 ->
                port = m1
            }
            (it=~/^<(.+)>$/).each {m0, m1 ->
                ip_info = m1
            }
            (it=~/^(.+?) \[(.+?)\]$/).each {m0, item, value ->
                item = item.trim()
                if (item == 'Master IP Address') {
                    def ip_address = value
                    if (ip_address && ip_address != '127.0.0.1') {
                        test_item.lookuped_port_list(ip_address, port)
                    }
                    net_ip[port]   = ip_address
                }
                if (item == 'Subnet Mask')
                    net_subnet[port]   = value
                if (item == 'Gateway')
                    net_route[port]   = value
                csv << [port, ip_info, item, value]
            }
        }
        def headers = ['Port', 'Information', 'Item', 'Value']
        test_item.devices(csv, headers)
        test_item.results([network: net_ip, net_subnet: net_subnet, net_route: net_route])
        // test_item.verify_text_search('network', results.toString())
        test_item.verify_text_search_list('network', net_ip)
    }

// MNT port
//  http                 [Open]
//  https                [Open]
//  telnet               [Open]
//  SSH                  [Open]
//  Maintenance-Secure   [Open]
//  ICMP                 [Open]
//  SNMP                 [Open]
//  RCIL                 [Closed]
//  ETERNUS DX Discovery [Open]

// RMT port
//  http                 [Open]
//  https                [Open]
//  telnet               [Open]
//  SSH                  [Open]
//  Maintenance-Secure   [Open]
//  ICMP                 [Open]
//  SNMP                 [Open]

    def firewall(session, test_item) {
        def lines = exec('firewall') {
            def result = session.execute 'show firewall'
            new File("${local_dir}/firewall").text = result
            return result
        }
        def results = [:].withDefault{[:].withDefault{[]}}
        def csv = []
        def phase = 'Unkown'
        lines.eachLine {
            // if (!(it =~ /^CLI>/) && it.size() > 0) {
            //     csv << [it]
            // }
            (it=~/^(.+) port$/).each {m0, m1 ->
                phase = m1
            }
            (it=~/^(.+?) \[(.+?)\]$/).each {m0, m1, m2 ->
                csv << [phase, m1.trim(), m2]
                if (m2 == 'Closed') {
                    results['Closed'][phase] << m1.trim()
                }
            }
        }
        def headers = ['Port', 'Protocol', 'Status']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

// SNMP [Enable]
// Port [MNT]
// Authentication Failure [Enable]
// Engine ID [0x800000d380500000e0da1a2200] (Default)
// MIB-II RFC Version [RFC1213]

    def snmp(session, test_item) {
        def lines = exec('snmp') {
            def result = session.execute 'show snmp'
            new File("${local_dir}/snmp").text = result
            return result
        }
        def results = 'NG'
        def csv = []
        lines.eachLine {
            (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
                csv << [m1.trim(), m2]
                if (m1 == 'SNMP') {
                    results = m2
                }
            }
        }
        def headers = ['Item', 'Value']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def snmp_manager(session, test_item) {
        run_eternus_show_command(session, test_item, 'show snmp-manager')
        def lines = exec('snmp_manager') {
            def result = session.execute 'show snmp-manager'
            new File("${local_dir}/snmp_manager").text = result
            return result
        }

        // No.  IP address
        // ---  -------------------------------------------
        //  1   10.20.5.1
        def results = []
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^(-+ *?) (-.+?)$/).each {
                m0, m1, m2 ->
                row = 0
                csize = [m1.size(), m2.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.+)$/).each {
                    m0, m1, m2 ->
                    // csv << [m1, m2]*.trim()
                    results << m2.trim()
                }
            }
            if (row >= 0)
                row ++;
        }
        // def headers = ['No', 'IP']
        // test_item.devices(csv, headers)
        test_item.results(results.toString())
    }


    def snmp_trap(session, test_item) {
        def lines = exec('snmp_trap') {
            def result = session.execute 'show snmp-trap'
            new File("${local_dir}/snmp_trap").text = result
            return result
        }

        // Trap SNMP    Manager IP                                      Community                          User                               Port
        // No.  Version Number  Address                                 Name                               Name                               Number
        // ---- ------- ------- --------------------------------------- ---------------------------------- ---------------------------------- ------
        //    1 v1            1 10.20.5.1                               "public"                                                              162

        def results = []
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
                m0, m1, m2, m3, m4, m5, m6, m7 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                         m5.size(), m6.size(), m7.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.+)$/).each {
                    m0, m1, m2, m3, m4, m5, m6, m7 ->
                    csv << [m1, m2, m3, m4, m5, m6, m7]*.trim()
                    results << "${m2.trim()} ${m4.trim()} ${m5.trim()}"
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['No', 'SNMPVersion', 'Manager', 'IP', 'Community', 'User', 'Port']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
        test_item.verify_text_search_list('snmp_trap', results)
    }

    def snmp_user(session, test_item) {
        def lines = exec("snmp_user") {
            def result = session.execute 'show snmp-user'
            new File("${local_dir}/snmp_user").text = result
            return result
        }
        def headers = ['Result']
        def csv = []
        lines.eachLine {
            if (!(it =~ /^CLI>/) && it.size() > 0) {
                csv << [it]
            }
        }
        test_item.devices(csv, headers)
        test_item.results(lines.size().toString())
    }

    def email_notification(session, test_item) {
        def lines = exec('email_notification') {
            def result = session.execute 'show email-notification'
            new File("${local_dir}/email_notification").text = result
            return result
        }
        def results = 'NG'
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^(Send E-Mail *?) (.+?)$/).each {
                m0, m1, m2 ->
                row = 0
                csize = [m1.size(), m2.size()]
                results = m2.trim()
            }
            if (row >= 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.*)$/).each { m0, m1, m2 ->
                    csv << [m1, m2]*.trim()
                    // results << "${m2.trim()} ${m4.trim()} ${m5.trim()}"
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['Name', 'Value']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }


    def event_notification(session, test_item) {
        def lines = exec('event_notification') {
            def result = session.execute 'show event-notification'
            new File("${local_dir}/event_notification").text = result
            return result
        }

        // [Severity: Error Level]                                E-Mail             SNMP               Host               REMCS              Syslog
        //  ----------------------------------------------------- ------------------ ------------------ ------------------ ------------------ ------------------
        //  Parts Error                                           Notify             Notify             Notify(OPMSG)      -                  Do not notify
        //  Disk Error                                            Notify             Notify             Notify             -                  Do not notify
        //  Disk Error (HDD Shield)                               Do not notify      Do not notify      Do not notify      Do not notify      Do not notify
        def results = [:].withDefault{0}
        def csv   = []
        def csize = []
        def row   = -1
        def severity = 'Unkown'
        lines.eachLine {
            (it =~ /(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
                m0, m1, m2, m3, m4, m5, m6 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                         m5.size(), m6.size()]
            }
            (it =~ /\[Severity: (.+?) Level\]/).each { m0, m1 ->
                severity = m1
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.+)$/).each {
                    m0, m1, m2, m3, m4, m5, m6 ->
                    csv << [severity, m1, m2, m3, m4, m5, m6]*.trim()
                    results[severity] += 1
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['Severity', 'Event', 'EMail', 'SNMP', 'Host', 'REMCS', 'Syslog']
        test_item.devices(csv, headers)
        test_item.results(results.toString())

    }


    def smi_s(session, test_item) {
        def lines = exec('smi_s') {
            def result = session.execute 'show smi-s'
            new File("${local_dir}/smi_s").text = result
            return result
        }
        def results = []
        def csv = []
        lines.eachLine {
            (it=~/^(.+?)\s+\[(.+?)\]$/).each {m0, m1, m2 ->
                csv << [m1.trim(), m2]
                results << m2
            }
        }
        // def headers = ['Item', 'Value']
        // test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def ntp(session, test_item) {
        def lines = exec('ntp') {
            def result = session.execute 'show ntp'
            new File("${local_dir}/ntp").text = result
            return result
        }
        def results = 'NG'
        def csv = []
        lines.eachLine {
            (it=~/^(.+?)\s+\[(.+?)\]$/).each {m0, m1, m2 ->
                csv << [m1.trim(), m2]
                if (m1 == 'NTP') {
                    results = m2
                }
            }
        }
        def headers = ['Item', 'Value']
        test_item.devices(csv, headers)
        test_item.results(results)
    }

    def storage_system_name(session, test_item) {
        def lines = exec('storage_system_name') {
            def result = session.execute 'show storage-system-name'
            new File("${local_dir}/storage_system_name").text = result
            return result
        }
        def results = [:]
        def csv = []
        lines.eachLine {
            (it=~/^(.+?)\s+\[(.*?)\]$/).each {m0, m1, m2 ->
                csv << [m1.trim(), m2 ?: 'Unkown']
                results[m1.trim()] = m2
            }
        }
        def headers = ['Item', 'Value']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def raid_tuning(session, test_item) {
        // run_eternus_show_command(session, test_item, 'show raid-tuning')
        def lines = exec('raid_tuning') {
            def result = session.execute 'show raid-tuning'
            new File("${local_dir}/raid_tuning").text = result
            return result
        }

        // RAID Group            RAID     Status                          DCMF Rebuild  Drive Access Disk    Throttle Ordered
        // No.  Name             Level                                         Priority Priority     Tuning           Cut
        // ---- ---------------- -------- ------------------------------- ---- -------- ------------ ------- -------- -------
        //    0 LUN_R#000        RAID5    Available                          1 Low      Response     Enable      100%     400
        //    1 LUN_R#001        RAID5    Available                          1 Low      Response     Enable      100%     400
        //    2 LUN_R#002        RAID5    Available                          1 Low      Response     Enable      100%     400
        //    3 LUN_R#003        RAID5    Available                          1 Low      Response     Enable      100%     400
        //    4 LUN_R#004        RAID5    Available                          1 Low      Response     Enable      100%     400
        def results = [:].withDefault{0}
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
                m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                         m5.size(), m6.size(), m7.size(), m8.size(),
                         m9.size(), m10.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.{${csize[6]}}) (.{${csize[7]}}) (.{${csize[8]}}) (.{${csize[9]}})/).each {
                    m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10 ->
                    csv << [m1, m2, m3, m4, m5, m6, m7, m8, m9, m10]*.trim()
                    results["${m6.trim()} ${m7.trim()}"] += 1
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['No', 'Name', 'Raid', 'Status', 'DCMF', 'RebuildPriority', 'DriveAccessPriority', 'DiskTuning', 'Throttle', 'OrderedCut']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }


    def cache_parameters(session, test_item) {
        def lines = exec('cache_parameters') {
            def result = session.execute 'show cache-parameters'
            new File("${local_dir}/cache_parameters").text = result
            return result
        }

        // Volume                                 Type      FP  PL   MWC(Range) PSDC SDDC SS   SDS  SPMC Cache Limit
        // No.   Name                                                                                    Size (MB)
        // ----- -------------------------------- --------- --- ---- ---------- ---- ---- ---- ---- ---- -----------
        //     0 LUN_V#000                        Standard  OFF    8   2(1-  8)    5    5  128  128    1           -
        //     1 LUN_V#001                        Standard  OFF    8   2(1-  8)    5    5  128  128    1           -
        //     2 LUN_V#002                        Standard  OFF    8   2(1-  8)    5    5  128  128    1           -
        //     3 LUN_V#003                        Standard  OFF    8   2(1-  8)    5    5  128  128    1           -
        //     4 LUN_V#004                        Standard  OFF    8   4(1-  8)    5    5  128  128    1           -
        def results = [:].withDefault{0}
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
                m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                         m5.size(), m6.size(), m7.size(), m8.size(),
                         m9.size(), m10.size(), m11.size(), m12.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.{${csize[6]}}) (.{${csize[7]}}) (.{${csize[8]}}) (.{${csize[9]}}) (.{${csize[10]}}) (.{${csize[11]}})/).each {
                    m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12 ->
                    csv << [m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12]*.trim()
                    results[m3.trim()] += 1
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['No', 'Name', 'Type', 'FP', 'PL', 'MWC', 'PSDC', 'SDDC', 'SS', 'SDS', 'SPMC', 'CacheLimit']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def firmware_version(session, test_item) {
        def lines = exec('firmware_version') {
            def result = session.execute 'show firmware-version'
            new File("${local_dir}/firmware_version").text = result
            return result
        }

        //    Version      Date
        // 1  V10L60-4000  2017-02-23  Current
        // 2  V10L60-2000  2016-12-14
        def results = []
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^( *?) (Version +?) (Date)$/).each {
                m0, m1, m2, m3 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.+)$/).each {
                    m0, m1, m2, m3 ->
                    csv << [m1, m2, m3]*.trim()
                    results << m2.trim()
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['No', 'Version', 'Date']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def disk_error(session, test_item) {
        def lines = exec('disk_error') {
            def result = session.execute 'show disk-error'
            new File("${local_dir}/disk_error").text = result
            return result
        }
        // E Disk            Status                         Port   Media   Drive   Drive-Recovered SMART   I/O     Link    Check-Code
        //   Location                                              Error   Error   Error           Error   Timeout Error   Error
        // - --------------- ------------------------------ ------ ------- ------- --------------- ------- ------- ------- ----------
        //   CE-Disk#0       Available                      Port#0       0       0               0       0       0       0          0
        //                                                  Port#1       0       0               0       0       0       0          0
        def results = [:].withDefault{0}
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
                m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                         m5.size(), m6.size(), m7.size(), m8.size(),
                         m9.size(), m10.size(), m11.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.{${csize[6]}}) (.{${csize[7]}}) (.{${csize[8]}}) (.{${csize[9]}}) (.{${csize[10]}})/).each {
                    m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11 ->
                    def arr = [m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11]*.trim()
                    csv << arr
                    if (arr[0] == '*') {
                        results[arr[0]] += 1
                    }
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['E', 'Location', 'Status', 'Port', 'MediaError', 'DriverError', 'Drive-RecoveredError',
                       'SMARTError', 'I/O Timeout', 'LinkError', 'Check-CodeError']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def port_error(session, test_item) {
        def lines = exec('port_error') {
            def result = session.execute 'show port-error'
            new File("${local_dir}/port_error").text = result
            return result
        }
        // Expander            Port    PHY    Status     Invalid  Disparity  Loss of Dword    PHY Reset
        //                                               Dword    Error      Synchronization  Problem
        // ------------------- ------- ------ ---------- -------  ---------  ---------------  ---------
        // CM#0 EXP            Port#0  PHY#0  Link Up          0          0                0          0
        //                             PHY#1  Link Up          0          0                0          0
        //                             PHY#2  Link Up          0          0                0          0
        def results = [:].withDefault{0}
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?)$/).each {
                m0, m1, m2, m3, m4, m5, m6, m7, m8 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                         m5.size(), m6.size(), m7.size(), m8.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.{${csize[6]}}) (.{${csize[7]}})/).each {
                    m0, m1, m2, m3, m4, m5, m6, m7, m8 ->
                    def arr = [m1, m2, m3, m4, m5, m6, m7, m8]*.trim()
                    csv << arr
                    results[arr[3]] += 1
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['Expander', 'Port', 'PHY', 'Status', 'InvalidDword', 'DisparityError',
                       'LossOfDwordSynchronization', 'PHYResetProblem']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

// Controller Enclosure
// Panel                   [OFF]
// Controller Module #0    [OFF]
// Controller Module #1    [OFF]
// CE-Disk    #0 [OFF]  #1 [OFF]  #2 [OFF]  #3 [OFF]  #4 [OFF]  #5 [OFF]  #6 [OFF]  #7 [OFF]  #8 [OFF]  #9 [OFF]  #10[OFF]  #11[OFF]
//            #12[OFF]  #13[OFF]  #14[OFF]  #15[OFF]  #16[OFF]  #17[OFF]  #18[OFF]  #19[OFF]  #20[OFF]  #21[OFF]  #22[OFF]  #23[OFF]

    def led(session, test_item) {
        def lines = exec('led') {
            def result = session.execute 'show led'
            new File("${local_dir}/led").text = result
            return result
        }
        def results  = [:]
        def ce_disks = [:].withDefault{0}
        def csv = []
        def no = 0
        def phase = 'Unkown'
        lines.eachLine {
            (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
                phase = m1
                if (!(m1 =~/^CE-Disk/)) {
                    csv << [m1.trim(), m2]
                    results[m1.trim()] = m2
                }
            }
            if (phase =~ /CE-Disk/) {
                it.split(/\s+/).each { disk_onoff ->
                    (disk_onoff =~/\[(.+)\]/).each {m0, m1 ->
                        ce_disks[m1] += 1
                        csv << ["CE-Disk#${no}", m1]
                        no ++
                    }
                }
            }
        }
        results['CE-Disk'] = ce_disks.toString()
        def headers = ['Item', 'Value']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

// Blink Panel Fault LED     [Enable ]
// Redundant Copy Fault LED  [Disable]

    def event_parameters(session, test_item) {
        def lines = exec('event_parameters') {
            def result = session.execute 'show event-parameters'
            new File("${local_dir}/event_parameters").text = result
            return result
        }
        def results = []
        def csv = []
        lines.eachLine {
            (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
                csv << [m1.trim(), m2]
                results << m2
            }
        }
        def headers = ['Item', 'Value']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

// Disk Patrol         [Enable]

    def disk_patrol(session, test_item) {
        def lines = exec('disk_patrol') {
            def result = session.execute 'show disk-patrol'
            new File("${local_dir}/disk_patrol").text = result
            return result
        }
        def results = []
        def csv = []
        lines.eachLine {
            (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
                csv << [m1.trim(), m2]
                results << m2
            }
        }
        def headers = ['Item', 'Value']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def subsystem_parameters(session, test_item) {
        def lines = exec('subsystem_parameters') {
            def result = session.execute 'show subsystem-parameters'
            new File("${local_dir}/subsystem_parameters").text = result
            return result
        }
        def results = []
        def csv = []
        lines.eachLine {
            (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
                csv << [m1.trim(), m2]
                results << m2
            }
        }
        def headers = ['Item', 'Value']
        test_item.devices(csv, headers)
        test_item.results("${results.size()} row")
    }

    def eco_mode(session, test_item) {
        def lines = exec('eco_mode') {
            def result = session.execute 'show eco-mode'
            new File("${local_dir}/echo_mode").text = result
            return result
        }
        def results = []
        def csv = []
        lines.eachLine {
            (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
                csv << [m1.trim(), m2]
                results << m2
            }
        }
        def headers = ['Item', 'Value']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def snmp_view(session, test_item) {
        def lines = exec('snmp_view') {
            def result = session.execute 'show snmp-view'
            new File("${local_dir}/snmp_view").text = result
            return result
        }
        def results = []
        def csv = []
        lines.eachLine {
            (it=~/^"(.+?)"$/).each {m0, m1 ->
                csv << [m1.trim()]
                results << m1
            }
        }
        def headers = ['Value']
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def syslog_notification(session, test_item) {
        def lines = exec('syslog_notification') {
            def result = session.execute 'show syslog-notification'
            new File("${local_dir}/syslog_notification").text = result
            return result
        }
        def results = []
        def csv = []
        lines.eachLine {
            (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
                csv << [m1.trim(), m2]
                results << m2
            }
        }
        def headers = ['Item', 'Value']
        test_item.devices(csv, headers)
        test_item.results("${results.size()} row")
    }

    def role(session, test_item) {
        def lines = exec('role') {
            def result = session.execute 'show role'
            new File("${local_dir}/role").text = result
            return result
        }
        def results = []
        def csv = []
        lines.eachLine {
            (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
                csv << [m1.trim(), m2]
                results << m2
            }
        }
        def headers = ['Item', 'Value']
        test_item.devices(csv, headers)
        test_item.results("${results.size()} row")
    }

    def user_policy(session, test_item) {
        def lines = exec('user_policy') {
            def result = session.execute 'show user-policy'
            new File("${local_dir}/user_policy").text = result
            return result
        }
        def results = []
        def csv = []
        lines.eachLine {
            (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
                csv << [m1.trim(), m2]
                results << m2
            }
        }
        def headers = ['Item', 'Value']
        test_item.devices(csv, headers)
        test_item.results("${results.size()} row")
    }

    def ssl_version(session, test_item) {
        def lines = exec('ssl_version') {
            def result = session.execute 'show ssl-version'
            new File("${local_dir}/ssl_version").text = result
            return result
        }
        def results = [:].withDefault{0}
        def csv   = []
        def csize = []
        def row   = -1
        lines.eachLine {
            (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?)$/).each {
                m0, m1, m2, m3, m4 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size(), m4.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}})(.*)$/).each {
                    m0, m1, m2, m3, m4, m5 ->
                    csv << [m1, m2, m3, m4]*.trim()
                    results[m1.trim()] = m2
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['Protocol', 'TLS1.0', 'TLS1.1', 'TLS1.2']

        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def thin_provisioning(session, test_item) {
        def lines = exec('thin_provisioning') {
            def result = session.execute 'show thin-provisioning'
            new File("${local_dir}/thin_provisioning").text = result
            return result
        }
        def results = []
        def csv = []
        lines.eachLine {
            (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
                csv << [m1.trim(), m2]
                results << m2
            }
        }
        def headers = ['Item', 'Value']
        test_item.devices(csv, headers)
        test_item.results("${results.size()} row")
    }

    def host_wwn_names(session, test_item) {
        def lines = exec('host_wwn_names') {
            def result = session.execute 'show host-wwn-names'
            new File("${local_dir}/host_wwn_names").text = result
            return result
        }
        def results = []
        def csv = []
        lines.eachLine {
            def is_contents = true
            (it=~/^CLI>/).each {
                is_contents = false
            }
            if (is_contents) {
                csv << [it]
                results << it
            }
        }
        def headers = ['Value']
        test_item.devices(csv, headers)
        test_item.results("${results.size()} row")
    }

    def host_groups(session, test_item) {
        def lines = exec('host_groups') {
            def result = session.execute 'show host-groups'
            new File("${local_dir}/host_groups").text = result
            return result
        }
        def results = []
        def csv = []
        lines.eachLine {
            def is_contents = true
            (it=~/^CLI>/).each {
                is_contents = false
            }
            if (is_contents) {
                csv << [it]
                results << it
            }
        }
        def headers = ['Value']
        test_item.devices(csv, headers)
        test_item.results("${results.size()} row")
    }

    def port_groups(session, test_item) {
        def lines = exec('port_groups') {
            def result = session.execute 'show port-groups -all'
            new File("${local_dir}/port_groups").text = result
            return result
        }
        def results = []
        def csv = []
        lines.eachLine {
            def is_contents = true
            (it=~/^CLI>/).each {
                is_contents = false
            }
            if (is_contents) {
                csv << [it]
                results << it
            }
        }
        def headers = ['Value']
        test_item.devices(csv, headers)
        test_item.results("${results.size()} row")
    }

    def lun_groups(session, test_item) {
        def lines = exec('lun_groups') {
            def lun_lists = session.execute 'show lun-groups'
            def luns = []
            lun_lists.eachLine {
                def lun_list = it.trim()
                (lun_list=~/^(\d+) /).each { m0, m1 ->
                    luns << m1
                }
            }
            def command = 'show lun-groups -lg-number ' + luns.join(',')
            def result = session.execute command
            new File("${local_dir}/lun_groups").text = result
            return result
        }
        def results = [:].withDefault{0}
        def csv   = []
        def csize = []
        def row   = -1
        def lun_group = 0
        lines.eachLine {
            (it =~ /^LUN Group No.(\d+)/).each { m0, m1 ->
                lun_group = m1
            }
            (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?)/).each {
                m0, m1, m2, m3, m4, m5, m6, m7 ->
                row = 0
                csize = [m1.size(), m2.size(), m3.size(), m4.size(), m5.size(), m6.size(), m7.size()]
            }
            if (row > 0 && it.size() > 0) {
                (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.{${csize[6]}}).*$/).each {
                    m0, m1, m2, m3, m4, m5, m6, m7 ->
                    csv << [lun_group, m1, m2, m3, m4, m5, m6, m7]*.trim()
                    results[m3.trim()] = m5.trim()
                }
            }
            if (row >= 0)
                row ++;
        }
        def headers = ['LUNGroup', 'LUN', 'Volume', 'Name', 'Status', 'Size', 'OverlapVolumes', 'UID']

        test_item.devices(csv, headers)
        test_item.results("${results.size()} row")
    }
    // def raid_groups(session, test_item) {
    //     // show raid-groups -rg-number 0
    // }

}

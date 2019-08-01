package InfraTestSpec

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
class NetAppDataONTAP extends InfraTestSpec {

    String ip
    String os_user
    String os_password
    String admin_password
    int    timeout = 30

    def init() {
        super.init()

        this.ip           = test_platform.test_target.ip ?: 'unkown'
        def os_account    = test_platform.os_account
        this.os_user      = os_account['user'] ?: 'unkown'
        this.os_password  = os_account['password'] ?: 'unkown'
        this.admin_password = os_account['admin_password'] ?: 'unkown'
    }

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()

        def con
        def result
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

    def run_ssh_command(con, command, test_id, admin_mode = false, share = false) {
        def ok_prompt = (admin_mode) ? "::#" : "::>";
        try {
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
            expect.sendLine('set -showallfields true -rows 0 -showseparator "<|>" -units GB');
            expect.expect(contains(ok_prompt)); 
            expect.sendLine(command); 
            String result = expect.expect(contains(ok_prompt)).getBefore(); 
            new File("${log_path}/${test_id}").text = result
            session.close()
            expect.close()
            return result
        } catch (Exception e) {
            log.error "[SSH Test] Command error '$command' in ${this.server_name} faild, skip.\n" + e
        }
    }

    def finish() {
        super.finish()
    }

    class CSVParseResult {
        def headers = []
        def csv     = []
        def infos   = [:]
    }

    // def parse_csv(TestItem test_item, String lines, String header_key, String header_value) {
    //     def result = new CSVParseResult()
    //     def header_index = [:]
    //     def rows = 0
    //     lines.eachLine {
    //         rows ++
    //         String[] columns = it.split(/<\|>/)
    //         if (rows == 3 && columns.size() > 1) {
    //             result.headers = columns as ArrayList
    //             header_index['key'] = result.headers.findIndexOf { it == header_key }
    //             header_index['val'] = result.headers.findIndexOf { it == header_value }
    //         } else if (rows > 3 && result.headers.size() == columns.size()) {
    //             def cols = 0
    //             if (header_index.containsKey('key') && header_index.containsKey('val')) {
    //                 result.infos[columns[header_index['key']]] = columns[header_index['val']]
    //             }
    //             result.csv << columns
    //         }
    //     }
    //     println "HEADERS: ${result.headers}"
    //     println "CSV: ${result.csv}"
    //     println "INFO: ${result.infos}\n"
    //     return result
    // }

    class CSVInfo {
        def headers = []
        def csv     = []
        def rows    = []
    }

    def parse_csv(String lines) {
        def csv_info = new CSVInfo()
        def header_index = [:]
        def rownum = 0
        def header_number = 0
        lines.eachLine {
            rownum ++
            String[] columns = it.split(/<\|>/)
            if (rownum == 3 && columns.size() > 1) {
                columns.each { header ->
                    header_index[header] = header_number
                    header_number ++
                }
                csv_info.headers = header_index.keySet() as ArrayList
            } else if (rownum > 3 && header_number == columns.size()) {
                def unique_columns = []
                def row = [:]
                header_index.each { header, cols ->
                    row[header] = columns[cols] 
                    unique_columns << columns[cols]
                }
                csv_info.rows << row
                csv_info.csv << unique_columns
            }
        }
        return csv_info
    }

    def subsystem_health(session, test_item) {
        def lines = exec('subsystem_health') {
            run_ssh_command(session, 'system health subsystem show', 'subsystem_health')
        }
        // def csv_result = this.parse_csv(test_item, lines, 'Subsystem', 'Health')
        def csv_info = this.parse_csv(lines)
        def infos = [:].withDefault{[]}
        csv_info.rows.each { row ->
            infos[row['Health']] << row['Subsystem']
        }
        test_item.devices(csv_info.csv, csv_info.headers)
        test_item.results("${infos}")
    }

    def storage_failover(session, test_item) {
        def lines = exec('storage_failover') {
            this.run_ssh_command(session, 'storage failover show', 'storage_failover')
        }
        def csv_info = this.parse_csv(lines)
        def infos = [:].withDefault{[]}
        csv_info.rows.each { row ->
            if (row['Takeover Enabled'] != '-') {
                infos[row['Takeover Enabled']] << row['Node']
            }
        }
        // println "INFOS:$infos"
        test_item.devices(csv_info.csv, csv_info.headers)
        test_item.results((infos.size() == 0) ? 'SingleNode' : "${infos}")
    }

    def network_interface(session, test_item) {
        def lines = exec('network_interface') {
            this.run_ssh_command(session, 'network interface show', 'network_interface')
        }
        def csv_info = this.parse_csv(lines)
        def infos = [:]
        def network_nodes = [:].withDefault{[]}
        def desc = test_platform?.test_metrics['network_interface']?.description
        csv_info.rows.each { row ->
            def ip_address = row['Network Address']
            def device     = row['Logical Interface Name']
            def netmask    = row['Bits in the Netmask']
            def node       = row['Current Node']
            def port       = row['Current Port']
            def ip_cidr    = "${ip_address}/${netmask}"

            if (ip_address == this.ip) {
                network_nodes[node] << ip_cidr
            }
            if (ip_address && ip_address != '127.0.0.1') {
                test_item.lookuped_port_list(ip_address, device)
                add_new_metric("network_interface.ip.${node}", "[${node}] IP", ip_address, infos)
                add_new_metric("network_interface.port.${node}", "[${node}] ポート", port, infos)
                add_new_metric("network_interface.device.${node}", "[${node}] デバイス", device, infos)
                add_new_metric("network_interface.netmask.${node}", "[${node}] サブネット", device, infos)
            }
            // this.test_platform.add_test_metric(device, "${desc} : ${node} : ${port}")
            infos[device] = ip_cidr
        }
        infos['network_interface'] = "${network_nodes}"
        test_item.devices(csv_info.csv, csv_info.headers)
        test_item.results(infos)
    }

    def aggregate_status(session, test_item) {
        def lines = exec('aggregate_status') {
            this.run_ssh_command(session, 'aggr show -owner-name *', 'aggregate_status')
        }
        def csv_info = this.parse_csv(lines)
        def infos = [:].withDefault{[]}
        csv_info.rows.each { row ->
            infos[row['Size']] << row['Aggregate']
        }
        test_item.devices(csv_info.csv, csv_info.headers)
        test_item.results("${infos}")
    }

    def df(session, test_item) {
        def lines = exec('df') {
            this.run_ssh_command(session, 'df', 'df')
        }
        def csv_info = this.parse_csv(lines)
        def volume_infos = [:].withDefault{[]}
        csv_info.rows.each { row ->
            ['Percentage of Used Space', 'Percentage of Inodes Used'].each { metric ->
                (row[metric] =~ /^(\d+)%/).each { m0, m1 ->
                    volume_infos[metric] << m1.toInteger()
                }
            }
        }
        def infos = [
            'df' : "${csv_info.rows.size()} volume",
            'df.max_used_space' : volume_infos['Percentage of Used Space'].max(),
            'df.max_used_inode' : volume_infos['Percentage of Inodes Used'].max(),
        ]
        test_item.verify_number_lower('max_used_space', infos['df.max_used_space'])
        test_item.verify_number_lower('max_used_inode', infos['df.max_used_inode'])

        test_item.devices(csv_info.csv, csv_info.headers)
        test_item.results(infos)
    }

    def version(session, test_item) {
        def lines = exec('version') {
            this.run_ssh_command(session, 'version', 'version')
        }
        def result = 'unknow'
        def row = 1
        lines.eachLine {
            row ++
            (it =~ /^NetApp (.+?)$/).each { m0, m1->
                result = m0
            }
        }
        test_item.results(result)
    }

    def vserver(session, test_item) {
        def lines = exec('vserver') {
            this.run_ssh_command(session, 'vserver show', 'vserver')
        }
        def csv_info = this.parse_csv(lines)
        def infos = [:].withDefault{[]}
        csv_info.rows.each { row ->
            if (row['Vserver Type'] == 'data') {
                infos['vserver'] << row['Vserver']
                infos['vserver.root_volume'] << row['Root Volume']
                infos['vserver.aggregate'] << row['Aggregate']
            }
            // infos[row['Health']] << row['Subsystem']
        }
        test_item.devices(csv_info.csv, csv_info.headers)
        test_item.results(infos)
    }

    def memory(session, test_item) {
        def lines = exec('memory') {
            this.run_ssh_command(session,
                                 'system controller memory dimm show',
                                 'memory')
        }
        def csv_info = this.parse_csv(lines)
        def memory_nodes = [:].withDefault{0}
        csv_info.rows.each { row ->
            def parts = row['Part Number'].replaceAll("[\" ]", "")
            memory_nodes[parts] += 1
        }
        test_item.devices(csv_info.csv, csv_info.headers)
        test_item.results("${memory_nodes}")
    }

    def license(session, test_item) {
        def lines = exec('license') {
            this.run_ssh_command(session,
                                 'system license show',
                                 'license')
        }
        def csv_info = this.parse_csv(lines)
        def licenses      = [:].withDefault{0}
        csv_info.rows.each { row ->
            def license = row['Package']
            licenses[license] += 1
        }
        test_item.devices(csv_info.csv, csv_info.headers)
        test_item.results("${licenses}")
    }

    def processor(session, test_item) {
        def lines = exec('processor') {
            this.run_ssh_command(session,
                                 'system controller show',
                                 'processor')
        }
        def csv_info = this.parse_csv(lines)
        def infos = [:].withDefault{0}
        csv_info.rows.each { row ->
            infos[row['Model Name']] += 1
        }
        test_item.devices(csv_info.csv, csv_info.headers)
        test_item.results("${infos}")
    }

    def volume(session, test_item) {
        def lines = exec('volume') {
            this.run_ssh_command(session,
                                 'volume show',
                                 'volume')
        }
        def csv_info = this.parse_csv(lines)
        def infos = [:].withDefault{0}
        csv_info.rows.each { row ->
            infos[row['Volume Size']] += 1
        }
        test_item.devices(csv_info.csv, csv_info.headers)
        test_item.results("${infos}")
    }

    def snmp(session, test_item) {
        def lines = exec('snmp') {
            this.run_ssh_command(session, 'system snmp show', 'snmp')
        }
        def csv_info = this.parse_csv(lines)
        def infos = [:]
        csv_info.rows.each { row ->
            infos["snmp"]           = row['Enable Value']
            infos["snmp.trap_host"] = row['Trap Hosts']
            infos["snmp.community"] = row['Community']
        }
        test_item.devices(csv_info.csv, csv_info.headers)
        test_item.results(infos)
        test_item.verify_text_search_list('snmp', infos)
    }

    def ntp(session, test_item) {
        def lines = exec('ntp') {
            this.run_ssh_command(session,
                                 'cluster time-service ntp server show',
                                 'ntp')
        }
        def csv_info = this.parse_csv(lines)
        def infos = []
        csv_info.rows.each { row ->
            infos << row['NTP Server Host Name, IPv4 or IPv6 Address']
        }
        test_item.devices(csv_info.csv, csv_info.headers)
        test_item.results(infos)
    }

    def sysconfig_hw(session, test_item) {
        def lines = exec('sysconfig_hw') {
            this.run_ssh_command(session,
                                 'run * sysconfig -a',
                                 'sysconfig_hw')
        }
        def infos = [:].withDefault{[:].withDefault{0}}
        def csv = []
        lines.eachLine {
            (it =~ /^\s*(\w.+?):\s(.+?)$/).each { m0, item, value->
                (item=~/^(Node|System ID|System Serial Number|Processors|Processor type|Memory Size|NVMEM Size)$/).each {
                    infos["hw.${item}"][value.trim()] += 1
                }
            }
            csv << [it]
        }
        def total_cpu_count = 0
        infos["hw.Processors"].each { cpu_count, servers ->
            total_cpu_count += servers * cpu_count.toInteger()
        }
        infos['hw.cpu'] = total_cpu_count
        infos['sysconfig_hw'] = "${infos["hw.Node"].size()} node"
        test_item.results(infos)
        test_item.devices(csv, ['message'])
    }

    def sysconfig_raid(session, test_item) {
        def lines = exec('sysconfig_raid') {
            this.run_ssh_command(session,
                                 'run * sysconfig -r',
                                 'sysconfig_raid')
        }

        def infos = [:]
        def drive_nodes = [:].withDefault{[:].withDefault{0}}
        def raid_groups = [:].withDefault{0}
        def node       = 'unkown'
        def raid_group = 'unkown'
        def csv = []
        lines.eachLine {
            (it =~ /^Node: (.+)$/).each {m0, m1 ->
                node = m1
            }
            (it =~ /^\s+RAID group (.+?)\s/).each {m0, m1 ->
                raid_group = m1
            }
            (it =~ /^\s+(dparity|parity|data)\s.+\s(\d+)\/(\d+)\s*$/).each {m0, m1, m2, m3 ->
                drive_nodes[m1]["${m2}MB"] += 1
                raid_groups["${node}.${raid_group}"] += 1
            }
            csv << [it]
        }
        drive_nodes.each { drive_node, info ->
            infos["drive.${drive_node}"] = "${info}"
        }
        infos['sysconfig_raid'] = "${raid_groups.size()} RAID groups"
        test_item.results(infos)
        test_item.devices(csv, ['message'])
    }
}

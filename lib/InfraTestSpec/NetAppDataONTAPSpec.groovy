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

    def parse_csv(TestItem test_item, String lines, String header_key, String header_value) {
        def result = new CSVParseResult()
        def header_index = [:]
        def rows = 0
        lines.eachLine {
            rows ++
            String[] columns = it.split(/<\|>/)
            if (rows == 3) {
                result.headers = columns as ArrayList
                header_index['key'] = result.headers.findIndexOf { it == header_key }
                header_index['val'] = result.headers.findIndexOf { it == header_value }
            } else if (rows > 3 && result.headers.size() == columns.size()) {
                def cols = 0
                if (header_index.containsKey('key') && header_index.containsKey('val')) {
                    result.infos[columns[header_index['key']]] = columns[header_index['val']]
                }
                result.csv << columns
            }
        }
        println "HEADERS: ${result.headers}"
        println "CSV: ${result.csv}"
        println "INFO: ${result.infos}\n"
        return result
    }

    def subsystem_health(session, test_item) {
        def lines = exec('subsystem_health') {
            run_ssh_command(session, 'system health subsystem show', 'subsystem_health')
        }
        def csv_result = this.parse_csv(test_item, lines, 'Subsystem', 'Health')
        test_item.devices(csv_result.csv, csv_result.headers)
        test_item.results("${csv_result.infos}")
    }

    def storage_failover(session, test_item) {
        def lines = exec('storage_failover') {
            this.run_ssh_command(session, 'storage failover show', 'storage_failover')
        }
        def csv_result = this.parse_csv(test_item, lines, 'Node', 'Takeover Enabled')
        test_item.devices(csv_result.csv, csv_result.headers)
        test_item.results("${csv_result.infos}")
    }

    def network_interface(session, test_item) {
        def lines = exec('network_interface') {
            this.run_ssh_command(session, 'network interface show', 'network_interface')
        }
        def csv_result = this.parse_csv(test_item, lines, 'Logical Interface Name', 'Network Address')
        test_item.devices(csv_result.csv, csv_result.headers)
        test_item.results("${csv_result.infos.size()} IP count")
        csv_result.infos.each { device, ip_address ->
            if (ip_address && ip_address != '127.0.0.1') {
                test_item.lookuped_port_list(ip_address, device)
            }
        }
    }

    def aggregate_status(session, test_item) {
        def lines = exec('aggregate_status') {
            this.run_ssh_command(session, 'aggr show -owner-name *', 'aggregate_status')
        }
        def csv_result = this.parse_csv(test_item, lines, 'Aggregate', 'Size')
        test_item.devices(csv_result.csv, csv_result.headers)
        test_item.results("${csv_result.infos}")
    }

    def df(session, test_item) {
        def lines = exec('df') {
            this.run_ssh_command(session, 'df', 'df')
        }
        def csv_result = this.parse_csv(test_item, lines, 'Volume File System Name', 'Percentage of Used Space')
        test_item.devices(csv_result.csv, csv_result.headers)
        test_item.results("${csv_result.infos}")
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
        def csv_result = this.parse_csv(test_item, lines, 'Vserver', 'Vserver Type')
        test_item.devices(csv_result.csv, csv_result.headers)
        test_item.results("${csv_result.infos}")
    }

    def snmp(session, test_item) {
        def lines = exec('snmp') {
            this.run_ssh_command(session, 'system snmp show', 'snmp')
        }
        def csv_result = this.parse_csv(test_item, lines, 'Trap Hosts', 'Community')
        test_item.devices(csv_result.csv, csv_result.headers)
        test_item.results("${csv_result.infos}")
    }

    def ntp(session, test_item) {
        def lines = exec('ntp') {
            this.run_ssh_command(session,
                                 'cluster time-service ntp server show',
                                 'ntp')
        }
        def csv_result = this.parse_csv(test_item, lines, 
                                        'NTP Server Host Name, IPv4 or IPv6 Address',
                                        'NTP Version for Server (default: auto)')
        test_item.devices(csv_result.csv, csv_result.headers)
        test_item.results("${csv_result.infos}")
    }

    def system_node(session, test_item) {
        def lines = exec('system_node') {
            this.run_ssh_command(session,
                                 'system node run -node local -command sysconfig -a',
                                 'system_node')
        }
        def result = 'OK'
        def infos = [:].withDefault{[:]}
        lines.eachLine {
            println it
            (it =~ /^(\w.+?)\s+(\w+?)$/).each { m0, m1, m2->
                infos[m1]['status'] = m2
                if (m2 != 'ok') {
                    result = 'NG'
                }
            }
        }
        def csv = []
        def headers = ['status']
        infos.each { subsystem, info ->
            def values = [subsystem]
            headers.each {
                values << info[it] ?: 'Unkown'
            }
            csv << values
        }
        println csv
        test_item.devices(csv, headers)
        test_item.results(result)
    }
}

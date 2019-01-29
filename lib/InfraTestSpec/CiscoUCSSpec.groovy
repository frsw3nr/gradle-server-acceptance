package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
// import org.hidetake.groovy.ssh.Ssh
import ch.ethz.ssh2.Connection
import net.sf.expectit.Expect
import net.sf.expectit.ExpectBuilder
import static net.sf.expectit.matcher.Matchers.contains
import org.jvyaml.YAML
import org.yaml.snakeyaml.Yaml
// import org.ho.yaml.Yaml
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*
import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

@Slf4j
@InheritConstructors
class CiscoUCS extends InfraTestSpec {

    String ip
    String os_user
    String os_password
    String admin_password
    Boolean use_ucs_platform_emulator
    int    timeout = 30
    def storage_adapter

    def init() {
        super.init()

        this.ip           = test_platform.test_target.ip ?: 'unkown'
        def os_account    = test_platform.os_account
        this.os_user      = os_account['user'] ?: 'unkown'
        this.os_password  = os_account['password'] ?: 'unkown'
        this.admin_password = os_account['admin_password'] ?: 'unkown'
        this.use_ucs_platform_emulator = os_account['use_emulator'] ?: false
    }

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()

        def con
        def session
        def result
        if (!dry_run) {
            con = new Connection(this.ip, 22)
            con.connect()
            result = con.authenticateWithPassword(this.os_user, this.os_password)
            if (!result) {
                println "connect failed"
                return
            }
            session = login_session(con)
        }
        test_items.each {
            def method = this.metaClass.getMetaMethod(it.test_id, Object, TestItem)
            if (method) {
                log.debug "Invoke command '${method.name}()'"
                try {
                    long start = System.currentTimeMillis();
                    method.invoke(this, session, it)
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
            session.close()
        }
    }

    def login_session(con) {
        try {
            def session = con.openSession()
            session.requestDumbPTY();
            session.startShell();
            if (this.use_ucs_platform_emulator) {
                def prompts = ["ok" : "> ", "admin" : "# "]
                Expect expect = new ExpectBuilder()
                        .withOutput(session.getStdin())
                        .withInputs(session.getStdout(), session.getStderr())
                                .withEchoOutput(System.out)
                                .withEchoInput(System.out)
                        .build();
                expect.expect(contains(prompts['ok'])); 
                expect.sendLine("c");  //  c: Login to  CLI shell
                expect.expect(contains(prompts['admin'])); 
                expect.close()
            }
            return session
        } catch (Exception e) {
            log.error "[SSH Test] login menu in ${this.server_name} faild, skip.\n" + e
        }
    }

    def run_ssh_command(session, commands, test_id, admin_mode = false, share = false) {
        def ok_prompt = (admin_mode) ? "# " : "> ";
        try {
            def log_path = (share) ? evidence_log_share_dir : local_dir
            println "SSH:1"
            Expect expect = new ExpectBuilder()
                    .withOutput(session.getStdin())
                    .withInputs(session.getStdout(), session.getStderr())
                            .withEchoOutput(System.out)
                            .withEchoInput(System.out)
                    .build();

            if (this.use_ucs_platform_emulator) {
                expect.sendLine("set cli table-field-delimiter comma")
                expect.expect(contains(ok_prompt))
                expect.sendLine("set cli suppress-field-spillover on")
                expect.expect(contains(ok_prompt))
                expect.sendLine("terminal length 0")
                expect.expect(contains(ok_prompt))
            } else {
                println "TEST1:$ok_prompt"
                expect.expect(contains(ok_prompt))
                println "TEST2"
                expect.sendLine("set cli output yaml")
                println "TEST3"
                expect.expect(contains(ok_prompt))
                println "TEST4"
            }


            // if (admin_mode) {
            //     expect.sendLine('enable');
            //     expect.expect(contains('Password:'));
            //     expect.sendLine(this.admin_password);
            // }
            // expect.expect(contains(ok_prompt)); 
            // expect.sendLine('set -showallfields true -rows 0 -showseparator "<|>" -units GB');
            // expect.expect(contains(ok_prompt)); 
            String result
            commands.each { command ->
                println "SSH:2:$command"
                expect.sendLine(command); 
                println "SSH:3:$ok_prompt"
                result = expect.expect(contains(ok_prompt)).getBefore(); 
                println "SSH:4:$result"
            }
            new File("${log_path}/${test_id}").text = result
            // session.close()
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

    String[] split_eol_extended(String line) {
        line += 'EOL'
        String[] columns = line.split(/,/)
        columns[-1] = columns[-1].replaceFirst(/EOL/,"")
        return columns
    }

    def parse_csv(TestItem test_item, String lines, String header_key, String header_value) {
        def result = new CSVParseResult()
        def header_index = [:]
        def phase = 'HEADER'
        def rows = 0
        lines.eachLine {
            def columns = split_eol_extended(it)
            if (phase == 'HEADER' && columns.size() > 1) {
                phase = 'SEPARATOR'
                result.headers = columns as ArrayList
                header_index['key'] = result.headers.findIndexOf { it == header_key }
                header_index['val'] = result.headers.findIndexOf { it == header_value }
            } else if (phase == 'SEPARATOR' && columns[0] =~ /---/) {
                phase = 'BODY'
            } else if (phase == 'BODY' && result.headers.size() == columns.size()) {
                rows ++
                def cols = 0
                println "$rows:$columns"
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

    // def parse_csv2 = { TestItem test_item, String lines, Closure closure ->
    //     def result = new CSVParseResult()
    //     def header_index = [:]
    //     def phase = 'HEADER'
    //     def rows = 0
    //     lines.eachLine {
    //         def columns = split_eol_extended(it)
    //         if (phase == 'HEADER' && columns.size() > 1) {
    //             phase = 'SEPARATOR'
    //             result.headers = columns as ArrayList
    //             header_index['key'] = result.headers.findIndexOf { it == header_key }
    //             header_index['val'] = result.headers.findIndexOf { it == header_value }
    //         } else if (phase == 'SEPARATOR' && columns[0] =~ /---/) {
    //             phase = 'BODY'
    //         } else if (phase == 'BODY' && result.headers.size() == columns.size()) {
    //             rows ++
    //             def cols = 0
    //             println "$rows:$columns"
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

    def extract_yaml(String lines) {
        def is_body = false
        def yaml = []
        lines.eachLine {
            it = it.replaceAll(/::/, "'::'")
            (it =~ /^---/).each {
                is_body = true
            }
            if (is_body) {
                yaml << it
            }
            // println "$is_body:$it"
            (it =~ /^\.\.\./).each {
                is_body = false
            }
        }
        // Delete invalid last line
        if (yaml[-1] != '...' && yaml[-1] != '') {
            yaml.pop()
        }
        def yaml_text = yaml.join("\n")
        return yaml_text
    }

    def bios(session, test_item) {
        def lines = exec('bios') {
            def commands = [
                'top',
                'show bios detail',
            ]
            run_ssh_command(session, commands, 'bios', true)
        }
        def infos = [:]
        def yaml_text = this.extract_yaml(lines)
        Yaml yaml_manager = new Yaml()
        Map yaml = (Map) yaml_manager.load(yaml_text)
        infos['bios']         = yaml?.'bios-version' ?: 'unkown'
        infos['secure-boot']  = yaml?.'secure-boot' ?: 'unkown'
        infos['boot-mode']    = yaml?.'boot-mode' ?: 'unkown'
        // println "BIOS : $infos"
        test_item.results(infos)
    }

    def cimc(session, test_item) {
        def lines = exec('cimc') {
            def commands = [
                'top',
                'show cimc detail',
            ]
            run_ssh_command(session, commands, 'cimc', true)
        }
        def infos = [:]
        def yaml_text = this.extract_yaml(lines)
        Yaml yaml_manager = new Yaml()
        Map yaml = (Map) yaml_manager.load(yaml_text)
        infos['cimc']     = yaml?.'version' ?: 'unkown'
        infos['timezone'] = yaml?.'timezone' ?: 'unkown'
        // println "CIMC:$infos"
        test_item.results(infos)
    }

    def cpu(session, test_item) {
        def lines = exec('cpu') {
            def commands = [
                'top',
                'scope chassis',
                'show cpu detail',
            ]
            run_ssh_command(session, commands, 'cpu', true)
        }
        def csv = []
        def infos = [:].withDefault{0}
        def yaml_text = this.extract_yaml(lines)
        Yaml yaml_manager = new Yaml()

        def rows = 0
        def headers = []
        yaml_manager.loadAll(yaml_text).each { info ->
            rows ++
            if (headers.size() == 0) {
                headers = info.keySet() as ArrayList
            }
            def values = []
            headers.each {
                values << info[it] ?: 'Unkown'
            }
            csv << values
            def keys = ['version', 'thread-count', 'core-count']
            def title = info.subMap(keys).values().join('/')
            infos[title] += 1
        }
        test_item.devices(csv, headers)
        test_item.results("$infos")
    }

    def memory(session, test_item) {
        def lines = exec('memory') {
            def commands = [
                'top',
                'scope chassis',
                'show dimm-summary',
           ]
            run_ssh_command(session, commands, 'memory', true)
        }
        def infos = [:]
        def yaml_text = this.extract_yaml(lines)
        Yaml yaml_manager = new Yaml()
        Map yaml = (Map) yaml_manager.load(yaml_text)
        infos['memory']      = yaml?.'totalmemory' ?: 'unkown'
        infos['memoryspeed'] = yaml?.'memoryspeed' ?: 'unkown'
        test_item.results(infos)
    }

    def hdd(session, test_item) {
        def lines = exec('hdd') {
            def commands = [
                'top',
                'scope chassis',
                'show hdd-pid detail',
           ]
            run_ssh_command(session, commands, 'hdd', true)
        }
        def csv = []
        def infos = [:].withDefault{0}
        def yaml_text = this.extract_yaml(lines)
        Yaml yaml_manager = new Yaml()

        def rows = 0
        def headers = []
        yaml_manager.loadAll(yaml_text).each { info ->
            rows ++
            if (headers.size() == 0) {
                headers = info.keySet() as ArrayList
            }
            def values = []
            headers.each {
                values << info[it] ?: 'Unkown'
            }
            csv << values
            // println info
            if (info.containsKey('Description')) {
                infos[info['Description']] += 1
            }
        }
        test_item.devices(csv, headers)
        test_item.results("$infos")
    }

    def storageadapter(session, test_item) {
        def lines = exec('storageadapter') {
            def commands = [
                'top',
                'scope chassis',
                'show storageadapter detail',
           ]
            run_ssh_command(session, commands, 'storageadapter', true)
        }
        def csv = []
        def infos = [:].withDefault{0}
        def yaml_text = this.extract_yaml(lines)
        Yaml yaml_manager = new Yaml()

        def rows = 0
        def headers = []
        yaml_manager.loadAll(yaml_text).each { info ->
            rows ++
            if (headers.size() == 0) {
                headers = info.keySet() as ArrayList
            }
            def values = []
            headers.each {
                values << info[it] ?: 'Unkown'
            }
            csv << values
            if (info.containsKey('product-name')) {
                infos[info['product-name']] += 1
            }
            if (info.containsKey('controller')) {
                storage_adapter = info['controller']
            }
        }
        test_item.devices(csv, headers)
        test_item.results("$infos")
    }

    def physical_drive(session, test_item) {
        if (!this.storage_adapter) {
            this.storageadapter(session, test_item)
        }
        def lines = exec('physical_drive') {
            def commands = [
                'top',
                'scope chassis',
                "scope storageadapter ${this.storage_adapter}",
                'show physical-drive detail',
           ]
            run_ssh_command(session, commands, 'physical_drive', true)
        }
        def csv = []
        def infos = [:].withDefault{0}
        def yaml_text = this.extract_yaml(lines)
        Yaml yaml_manager = new Yaml()

        def rows = 0
        def headers = []
        yaml_manager.loadAll(yaml_text).each { info ->
            rows ++
            if (headers.size() == 0) {
                headers = info.keySet() as ArrayList
            }
            def values = []
            headers.each {
                values << info[it] ?: 'Unkown'
            }
            csv << values
            // println info
            if (info.containsKey('raw-size')) {
                infos[info['raw-size']] += 1
            }
        }
        test_item.devices(csv, headers)
        test_item.results("$infos")
    }

    def virtual_drive(session, test_item) {
        if (!this.storage_adapter) {
            this.storageadapter(session, test_item)
        }
        def lines = exec('virtual_drive') {
            def commands = [
                'top',
                'scope chassis',
                "scope storageadapter ${this.storage_adapter}",
                'show virtual-drive detail',
           ]
            run_ssh_command(session, commands, 'virtual_drive', true)
        }
        def csv = []
        def infos = [:]
        def yaml_text = this.extract_yaml(lines)
        Yaml yaml_manager = new Yaml()

        def rows = 0
        def headers = []
        yaml_manager.loadAll(yaml_text).each { info ->
            rows ++
            if (headers.size() == 0) {
                headers = info.keySet() as ArrayList
            }
            def values = []
            headers.each {
                values << info[it] ?: 'Unkown'
            }
            csv << values
            def keys = ['raid-level', 'physical-drives', 'size']
            infos[info['name']] = info.subMap(keys)
        }
        test_item.devices(csv, headers)
        test_item.results("$infos")
    }

    def network(session, test_item) {
        def lines = exec('network') {
            def commands = [
                'top',
                'show cimc/network detail',
           ]
            run_ssh_command(session, commands, 'network', true)
        }
        def csv = []
        def infos = [:]
        def yaml_text = this.extract_yaml(lines)
        Yaml yaml_manager = new Yaml()

        def rows = 0
        def headers = []
        yaml_manager.loadAll(yaml_text).each { info ->
            rows ++
            if (headers.size() == 0) {
                headers = info.keySet() as ArrayList
            }
            def values = []
            headers.each {
                values << info[it] ?: 'Unkown'
            }
            csv << values
            def ip_address = info['v4-addr']
            if (ip_address && ip_address != '127.0.0.1') {
                test_item.lookuped_port_list(ip_address, "CiscoCIMC${rows}")
            }
            def keys = ['v4-addr', 'v4-netmask', 'v4-gateway']
            infos[rows] = info.subMap(keys).values()
        }
        test_item.devices(csv, headers)
        test_item.results("$infos")
    }

    def snmp(session, test_item) {
        def lines = exec('snmp') {
            def commands = [
                'top',
                'show snmp detail',
           ]
            run_ssh_command(session, commands, 'snmp', true)
        }
        def infos = [:]
        def yaml_text = this.extract_yaml(lines)
        Yaml yaml_manager = new Yaml()
        Map yaml = (Map) yaml_manager.load(yaml_text)
        infos['snmp']             = yaml?.'enabled' ?: 'unkown'
        infos['snmp-port']        = yaml?.'snmp-port' ?: 'unkown'
        infos['sys-contact']      = yaml?.'sys-contact' ?: 'unkown'
        infos['community-str']    = yaml?.'community-str' ?: 'unkown'
        infos['community-access'] = yaml?.'community-access' ?: 'unkown'
        test_item.results(infos)
    }

    def snmp_trap(session, test_item) {
        def lines = exec('snmp_trap') {
            def commands = [
                'top',
                'show snmp/trap-destinations detail',
           ]
            run_ssh_command(session, commands, 'snmp_trap', true)
        }
        def csv = []
        def infos = [:]
        def yaml_text = this.extract_yaml(lines)
        Yaml yaml_manager = new Yaml()

        def rows = 0
        def headers = []
        yaml_manager.loadAll(yaml_text).each { info ->
            rows ++
            if (headers.size() == 0) {
                headers = info.keySet() as ArrayList
            }
            def values = []
            headers.each {
                values << info[it] ?: 'Unkown'
            }
            if (info['enabled'] == true) {
                csv << values
                def keys = ['trap-addr']
                infos[rows] = info.subMap(keys).values()
            }
        }
        test_item.devices(csv, headers)
        test_item.results("$infos")
    }

    def ntp(session, test_item) {
        def lines = exec('ntp') {
            def commands = [
                'top',
                'show /cimc/network/ntp detail',
           ]
            run_ssh_command(session, commands, 'ntp', true)
        }
        def infos = [:]
        def yaml_text = this.extract_yaml(lines)
        Yaml yaml_manager = new Yaml()
        Map yaml = (Map) yaml_manager.load(yaml_text)
        infos['ntp']      = yaml?.'enabled' ?: 'unkown'
        infos['server-1'] = yaml?.'server-1' ?: 'unkown'
        infos['server-2'] = yaml?.'server-2' ?: 'unkown'
        test_item.results(infos)
    }

    def system(session, test_item) {
        def lines = exec('system') {
            def commands = [
                'scope chassis 3',
                'scope server 1',
                'show system detail',
            ]
            run_ssh_command(session, commands, 'system', true)
        }
        println "RESULT:${lines}"
        def ip = 'unkown'
        def infos = [:]
        lines.eachLine {
            (it =~ /\s+(\w.+?):\s+(\w.+?)$/).each { m0, m1, m2->
                infos['system.' + m1] = m2
                if (m1 == 'System IP Address') {
                    ip = m2
                    test_item.admin_port_list(ip, 'CiscoUCS-SYS')
                }
            }
        }
        infos['system'] = ip
        test_item.results(infos)
    }

    def storage(session, test_item) {
        def lines = exec('storage') {
            def commands = [
                'scope chassis 3',
                'scope server 1',
                'show raid-controller detail expand',
            ]
            run_ssh_command(session, commands, 'storage', true)
        }
        def infos = [:].withDefault{[:]}
        def result = 'unkown'
        def id     = 'unkown'
        def phase  = 'unkown'
        lines.eachLine {
            println it
            (it =~ /^\s+(RAID Controller|Local Disk):$/).each { m0, m1 ->
                phase = m1
            }
            (it =~ /^\s+(\w.+?):\s+(\w.+?)$/).each { m0, m1, m2->
                if (m1 == 'ID') {
                    id = m2
                }
                println "PHASE:$phase, ID:$id, METRIC:$m1, VAL:$m2"
                if (phase == 'Local Disk') {
                    infos[id][m1] = m2
                } else if (phase == 'RAID Controller' && 
                           m1 =~ /(Model|Serial|Mode|Type)/) {
                    
                }
            }
        }
        def csv = []
        def headers = ['Operability', 'Connection Protocol', 'Product Name', 
                       'Vendor', 'Model', 'Serial']
        infos.each { subsystem, info ->
            def values = [subsystem]
            println info
            headers.each {
                values << info[it] ?: 'Unkown'
            }
            csv << values
        }
        println headers
        println csv
        test_item.devices(csv, headers)
        test_item.results(result)
    }

    def subsystem_health(session, test_item) {
        def lines = exec('subsystem_health') {
            run_ssh_command(session, 'system health subsystem show', 'subsystem_health')
        }
        def csv_result = this.parse_csv(test_item, lines, 'Subsystem', 'Health')
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

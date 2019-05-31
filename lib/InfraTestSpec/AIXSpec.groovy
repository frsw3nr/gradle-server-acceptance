package InfraTestSpec

import static groovy.json.JsonOutput.*
import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.lang.math.NumberUtils
// import org.hidetake.groovy.ssh.Ssh
import ch.ethz.ssh2.Connection
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

@Slf4j
@InheritConstructors
class AIXSpec extends InfraTestSpec {

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

    def run_ssh_command(con, command, test_id, share = false) {
        try {
            def log_path = (share) ? evidence_log_share_dir : local_dir

            def session = con.openSession()
            session.execCommand command
            def result = session.stdout.text
            new File("${log_path}/${test_id}").text = result
            session.close()
            return result

        } catch (Exception e) {
            log.error "[SSH Test] Command error '$command' in ${this.server_name} faild, skip.\n" + e
        }
    }

    def finish() {
        super.finish()
    }

    def oslevel(session, test_item) {
        def lines = exec('oslevel') {
            run_ssh_command(session, 'oslevel -s', 'oslevel')
        }
        // Example:
        // 6100-06-07-1207 is AIX 6.1 TL 6 SP 7 and was built / released on the 7th week of 2012
        def info = [:]
        lines.eachLine { line ->
            (line =~ /(\d)(\d)(\d)(\d)-(\d+)-(\d+)-(\d+)/).each { m0, v1, v2, v3, v4, tl, sp, build ->
                info['oslevel'] = m0
                info['osname']  = "AIX ${v1}.${v2} TL ${tl} SP ${sp} Build ${build}"
            }
        }
        test_item.results(info)
        test_item.verify_text_search('System', info['osname'])
        test_item.verify_text_search('Release', info['osname'])
    }

    def prtconf(session, test_item) {
        def lines = exec('prtconf') {
            run_ssh_command(session, 'LANG=c prtconf', 'prtconf')
        }

        def info = [:]
        def phase = 1
        def volume
        def volumes = [:]
        def csv = []
        def processor = 'unkown'
        def memory_size = 0
        lines.eachLine { line ->
            (line=~/Network Information/).each {
                phase = 2
            }
            (line=~/Paging Space Information/).each {
                phase = 3
            }
            (line=~/Volume Groups Information/).each {
                phase = 4
            }
            (line=~/INSTALLED RESOURCE LIST/).each {
                phase = 0
            }
            // General information
            if (phase == 1) {
                (line=~/^(.+): (.+)$/).each { m0, name, value ->
                    if (name == 'Memory Size') {
                        (value =~ /^(\d+)/).each { mm0, mm1 ->
                            memory_size = NumberUtils.toDouble(mm1) / 1024
                            value = String.format("%1.1f", memory_size)
                        }
                    } else if (name == 'Machine Serial Number') {
                        value = "'" + value + "'"
                    } else if (name == 'Processor Implementation Mode') {
                        processor = value
                    }
                    info["prtconf.${name}"] = value
                }
            }
            // Volume Groups Information
            // datavg1:
            // PV_NAME           PV STATE          TOTAL PPs   FREE PPs    FREE DISTRIBUTION
            // hdisk0            active            863         313         00..01..00..139..173
            if (phase == 4) {
                (line =~ /^(.*):$/).each { m0, m1 ->
                    volume = m1
                }
                (line =~ /^(.+?)\s+(.+?)\s+(\d+?)\s+(\d+?)\s+(.+\d)$/).each { 
                    m0, pv_name, pv_state, total_pp, free_pp, free_distribution ->
                    csv << [volume, pv_name, pv_state, total_pp, free_pp, free_distribution]
                    volumes[volume] = total_pp
                    add_new_metric("prtconf.disk.${volume}", "ストレージ [${volume}] 容量", total_pp, info)
                }
            }
        }
        // info["prtconf.disk"] = volumes.toString()
        def headers = ['volume', 'pv_name', 'pv_state', 'total_pp[GB]', 'free_pp[GB]', 'free_distribution']
        test_item.devices(csv, headers)
        info['prtconf.disk'] = "$volumes"
        info['prtconf'] = processor
        test_item.results(info)
        test_item.verify_number_equal('cpu_real', info['prtconf.Number Of Processors'])
        test_item.verify_number_equal('memory', memory_size, 0.1)
        test_item.verify_number_equal_map('disk', volumes)
    }

    def network(session, test_item) {
        def lines = exec('network') {
            run_ssh_command(session, 'ifconfig -a', 'network')
        }
        def network = [:].withDefault{[:]}
        def device = ''
        def hw_address = []
        def device_ip = [:]
        def res = [:]
        lines.eachLine {
            (it =~  /^(.+?): (.+)<(.+)>$/).each { m0,m1,m2,m3->
                device = m1
                if (device == 'lo0') {
                    return
                }
            }
            (it =~ /inet\s+(.*?)\s/).each {m0, m1->
                if (device != 'lo0') {
                    network[device]['ip'] = m1
                    device_ip[device] = m1
                    test_item.lookuped_port_list(m1, device)
                    add_new_metric("network.ip.${device}", "[${device}] IP", m1, res)
                }
            }
            (it =~ /netmask\s+0x(.+?)[\s|]/).each {m0, m1->
                if (device != 'lo0') {
                    network[device]['subnet'] = m1
                    add_new_metric("network.subnet.${device}", "[${device}] サブネット", m1, res)
                }
            }
        }

        def csv = []
        def infos = [:].withDefault{[:]}
        network.each { device_id, items ->
            def columns = [device_id]
            ['ip', 'subnet'].each {
                def value = items[it] ?: 'NaN'
                columns.add(value)
                infos[device_id][it] = value
            }
            csv << columns
        }
        def headers = ['device', 'ip', 'subnet']
        test_item.devices(csv, headers)
        // test_item.results(['network': "$infos", 'net_ip': "$device_ip"])
        res['network'] = "${device_ip}"
        test_item.results(res)
        test_item.verify_text_search_list('net_ip', device_ip)
    }

}

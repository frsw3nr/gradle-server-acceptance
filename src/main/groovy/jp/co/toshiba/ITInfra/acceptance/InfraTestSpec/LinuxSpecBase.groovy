package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.session.execution.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
@InheritConstructors
class LinuxSpecBase extends InfraTestSpec {

    String ip
    String os_user
    String os_password
    String work_dir

    def init() {
        super.init()

        this.ip          = test_server.ip
        def os_account   = test_server.os_account
        this.os_user     = os_account['user']
        this.os_password = os_account['password']
        this.work_dir    = os_account['work_dir']
    }

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()

        def ssh = Ssh.newService()
        ssh.remotes {
            ssh_host {
                host = this.ip
                port = 22
                user = this.os_user
                password = this.os_password
                knownHosts = allowAnyHosts
            }
        }
        ssh.settings {
            dryRun = this.dry_run
        }
        ssh.run {
            session(ssh.remotes.ssh_host) {
                try {
                    execute "mkdir -vp ${work_dir}"
                } catch (Exception e) {
                    log.error "[SSH Test] Working directory in ${this.server_name} faild, skip.\n" + e
                    return
                }

                test_items.each {
                    def method = this.metaClass.getMetaMethod(it.test_id, Object, TestItem)
                    if (method) {
                        log.debug "Invoke command '${method.name}()'"
                        try {
                            long start = System.currentTimeMillis();
                            method.invoke(this, delegate, it)
                            long elapsed = System.currentTimeMillis() - start
                            log.info "Finish test method '${method.name}()' in ${this.server_name}, Elapsed : ${elapsed} ms"
                            it.succeed = 1
                        } catch (Exception e) {
                            log.error "[SSH Test] Test method '${method.name}()' faild, skip.\n" + e
                        }
                    }
                }
                remove work_dir
            }
        }
    }

    def run_ssh_command(session, command, test_id) {
        session.execute "${command} > ${work_dir}/${test_id}"
        session.get from: "${work_dir}/${test_id}", into: local_dir
        new File("${local_dir}/${test_id}").text
    }

    def hostname(session, test_item) {
        def lines = exec('hostname') {
            run_ssh_command(session, 'hostname -s', 'hostname')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def hostname_fqdn(session, test_item) {
        def lines = exec('hostname_fqdn') {
            run_ssh_command(session, 'hostname --fqdn', 'hostname_fqdn')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def uname(session, test_item) {
        def lines = exec('uname') {
            run_ssh_command(session, 'uname -a', 'uname')
        }
        def info = ''
        lines.eachLine {
            (it =~ /^(.+?)#/).each {m0,m1->
                info = m1
            }
        }
        test_item.results(info)
    }

    def lsb(session, test_item) {
        def lines = exec('lsb') {
            run_ssh_command(session, 'cat /etc/*-release', 'lsb')
        }
        def info = [:]
        // = を含む行を除いて、重複行を取り除く
        lines.eachLine {
            if (it =~ /=/) {
            } else {
                info[it] = ''
            }
        }
        test_item.results(info.keySet().toString())
    }

    def cpu(session, test_item) {
        def lines = exec('cpu') {
            run_ssh_command(session, 'cat /proc/cpuinfo', 'cpu')
        }

        def cpuinfo    = [:].withDefault{0}
        def real_cpu   = [:].withDefault{0}
        def cpu_number = 0
        lines.eachLine {
            (it =~ /processor\s+:\s(.+)/).each {m0,m1->
                cpu_number += 1
            }
            (it =~ /physical id\s+:\s(.+)/).each {m0,m1->
                real_cpu[m1] = true
            }
            (it =~ /cpu cores\s+:\s(.+)/).each {m0,m1->
                cpuinfo["cores"] = m1
            }
            (it =~ /model name\s+:\s(.+)/).each {m0,m1->
                cpuinfo["model_name"] = m1
            }
            (it =~ /cpu MHz\s+:\s(.+)/).each {m0,m1->
                cpuinfo["mhz"] = m1
            }
            (it =~ /cache size\s+:\s(.+)/).each {m0,m1->
                cpuinfo["cache_size"] = m1
            }
        }
        cpuinfo["cpu_total"] = cpu_number
        cpuinfo["cpu_real"] = real_cpu.size()
        cpuinfo["cpu_cores"] = real_cpu.size() * cpuinfo["cpu_cores"].toInteger()

        test_item.results(cpuinfo)
    }

    def machineid(session, test_item) {
        def lines = exec('machineid') {
            run_ssh_command(session, 'cat /var/lib/dbus/machine-id', 'machineid')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def meminfo(session, test_item) {
        def lines = exec('meminfo') {
            run_ssh_command(session, 'cat /proc/meminfo', 'meminfo')
        }
        Closure norm = { value, unit ->
            if (unit == 'kB') {
                return value
            } else if (unit == 'mB') {
                return value * 1024
            } else {
                return "${value}${unit}"
            }
        }
        def meminfo    = [:].withDefault{0}
        lines.eachLine {
            (it =~ /^MemTotal:\s+(\d+) (.+)$/).each {m0,m1,m2->
                meminfo['mem_total'] = norm(m1, m2)
            }
            (it =~ /^MemFree:\s+(\d+) (.+)$/).each {m0,m1,m2->
                meminfo['mem_free'] = norm(m1, m2)
            }
        }
        test_item.results(meminfo)
    }

    def network(session, test_item) {
        def lines = exec('network') {
            run_ssh_command(session, '/sbin/ip -d -s link', 'network')
        }
        def network = [:].withDefault{[:]}
        def hw_address = []
        lines.eachLine {
            // 2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
            (it =~  /^(\d+): (.+): <(.+)> (.+)$/).each { m0,m1,m2,m3,m4->
                if (m2 == 'lo') {
                    return
                }
                def index = 0
                def name  = ''
                m4.split(/ /).each{ n1->
                    if (index % 2 == 0) {
                        name = n1
                    } else {
                        network[m2][name] = n1
                    }
                    index ++
                }
            }
            // link/ether 00:0c:29:c2:69:4b brd ff:ff:ff:ff:ff:ff promiscuity 0
            (it =~ /link\/ether\s+(.*?)\s/).each {m0, m1->
                hw_address.add(m1)
            }
        }
        test_item.results([
            'network' : network.toString(),
            'hw_address' : hw_address.toString()
            ])
    }

    def net_onboot(session, test_item) {
        def lines = exec('net_onboot') {
            def command = """\
            |cd /etc/sysconfig/network-scripts/
            |grep ONBOOT ifcfg-*
            """.stripMargin()
            session.execute command
            session.get from: "${work_dir}/net_onboot", into: local_dir
            new File("${local_dir}/net_onboot").text
        }
        def net_onboot = [:]
        lines.eachLine {
            (it =~ /^ifcfg-(.+):ONBOOT=(.+)$/).each {m0,m1,m2->
                net_onboot[m1] = m2
            }
        }
        test_item.results(net_onboot.toString())
    }

    def block_device(session, test_item) {
        def lines = exec('block_device') {
            def command = """\
            |egrep '.*' /sys/block/*/size                      >> ${work_dir}/block_device
            |egrep '.*' /sys/block/*/removable                 >> ${work_dir}/block_device
            |egrep '.*' /sys/block/*/device/model              >> ${work_dir}/block_device
            |egrep '.*' /sys/block/*/device/rev                >> ${work_dir}/block_device
            |egrep '.*' /sys/block/*/device/state              >> ${work_dir}/block_device
            |egrep '.*' /sys/block/*/device/timeout            >> ${work_dir}/block_device
            |egrep '.*' /sys/block/*/device/vendor             >> ${work_dir}/block_device
            |egrep '.*' /sys/block/*/device/queue_depth        >> ${work_dir}/block_device
            |egrep '.*' /sys/block/*/queue/rotational          >> ${work_dir}/block_device
            |egrep '.*' /sys/block/*/queue/physical_block_size >> ${work_dir}/block_device
            |egrep '.*' /sys/block/*/queue/logical_block_size  >> ${work_dir}/block_device
            """
            session.execute command.stripMargin()
            session.get from: "${work_dir}/block_device", into: local_dir
            new File("${local_dir}/block_device").text
        }
        def block_device = [:].withDefault{[:]}
        lines.eachLine {
            (it =~  /^\/sys\/block\/(.+?)\/(.+):(.+)$/).each { m0,m1,m2,m3->
                if (m1 =~ /(ram|loop)/) {
                    return
                }
                if (m2 == 'device/timeout') {
                    block_device[m1]['timeout'] = m3
                }
                if (m2 == 'device/queue_depth') {
                    block_device[m1]['queue_depth'] = m3
                }
            }
        }
        test_item.results(block_device.toString())
    }

    def mdadb(session, test_item) {
        def lines = exec('mdadb') {
            run_ssh_command(session, 'cat /proc/mdstat', 'mdadb')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def filesystem(session, test_item) {
        def lines = exec('filesystem') {
            run_ssh_command(session, '/bin/lsblk -i', 'filesystem')
        }
        test_item.results(lines)
    }

    def filesystem_df_ip(session, test_item) {
        def lines = exec('filesystem_df_ip') {
            run_ssh_command(session, 'df -iP', 'filesystem_df_ip')
        }
        test_item.results(lines)
    }

    def fips(session, test_item) {
        def lines = exec('fips') {
            run_ssh_command(session, 'cat /proc/sys/crypto/fips_enabled', 'fips')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        def enabled = 'False'
        if (lines == '0') {
            enabled = 'False'
        } else if (lines == '1') {
            enabled = 'True'
        }
        test_item.results(enabled)
    }

    def virturization(session, test_item) {
        def lines = exec('virturization') {
            run_ssh_command(session, 'cat /proc/cpuinfo', 'virturization')
        }
        def virturization = 'no KVM'
        lines.eachLine {
            if (it =~  /QEMU Virtual CPU|Common KVM processor|Common 32-bit KVM processor/) {
                virturization = 'KVM Guest'
            }
        }
        test_item.results(virturization)
    }

    def packages(session, test_item) {
        def lines = exec('packages') {
            def command = "rpm -qa --qf "
            def argument = '"%{NAME}\t%|EPOCH?{%{EPOCH}}:{0}|\t%{VERSION}\t%{RELEASE}\t%{INSTALLTIME}\t%{ARCH}\n"'
            run_ssh_command(session, "${command} ${argument}", 'packages')
        }
        def packages = [:].withDefault{0}
        def csv = []
        lines.eachLine {
            def arr = it.split(/\t/)
            def packagename = arr[0]
            def release = arr[3]
            def release_label = 'COMMON'
            if (release =~ /el5/) {
                release_label = 'RHEL5'
            } else if (release =~ /el6/) {
                release_label = 'RHEL6'
            }
            def install_time = Long.decode(arr[4]) * 1000L
            arr[4] = new Date(install_time).format("yyyy/MM/dd HH:mm:ss")
            csv << arr
            def arch    = (arr[5] == '(none)') ? 'noarch' : arr[5]
            packages[release_label] ++
        }
        def headers = ['name', 'epoch', 'version', 'release', 'installtime', 'arch']
        test_item.devices(csv, headers)
        test_item.results(packages.toString())
    }

    def mount_iso(session, test_item) {
        def lines = exec('mount_iso') {
            run_ssh_command(session, 'mount', 'mount_iso')
        }
        def mountinfo = [:]
        lines.eachLine {
            ( it =~ /\.iso on (.+?)\s/).each {m0,m1->
                mountinfo[m1] = 'On'
            }
        }
        def result = (mountinfo.size() > 0) ? mountinfo.toString() : 'no mount'
        test_item.results(result)
    }

    def oracle(session, test_item) {
        def lines = exec('oracle') {
            def command = """\
            |ls -d /opt/oracle/app/product/*/*  >> ${work_dir}/oracle
            |ls -d /*/app/oracle/product/*/*    >> ${work_dir}/oracle
            """
            session.execute command.stripMargin()
            session.get from: "${work_dir}/oracle", into: local_dir
            new File("${local_dir}/oracle").text
        }
        def oracleinfo = 'NotFound'
        lines.eachLine {
            ( it =~ /\/product\/(.+?)\/(.+?)$/).each {m0,m1,m2->
                oracleinfo = m1
            }
        }
        test_item.results(oracleinfo)
    }

    def proxy_global(session, test_item) {
        def lines = exec('proxy_global') {
            run_ssh_command(session, 'grep proxy /etc/yum.conf', 'proxy_global')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def kdump(session, test_item) {
        def lines = exec('kdump') {
            run_ssh_command(session, '/sbin/chkconfig --list|grep kdump', 'kdump')
        }
        def kdump = 'off'
        lines.eachLine {
            ( it =~ /\s+3:(.+?)\s+4:(.+?)\s+5:(.+?)\s+/).each {m0,m1,m2,m3->
                if (m1 == 'on' && m2 == 'on' && m3 == 'on') {
                    kdump = 'on'
                }
            }
        }
        test_item.results(kdump)
    }

    def crash_size(session, test_item) {
        def lines = exec('crash_size') {
            run_ssh_command(session, 'cat /sys/kernel/kexec_crash_size', 'crash_size')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def iptables(session, test_item) {
        def lines = exec('iptables') {
            run_ssh_command(session, '/sbin/chkconfig --list|grep iptables', 'iptables')
        }
        def iptables = 'off'
        lines.eachLine {
            ( it =~ /\s+3:(.+?)\s+4:(.+?)\s+5:(.+?)\s+/).each {m0,m1,m2,m3->
                if (m1 == 'on' && m2 == 'on' && m3 == 'on') {
                    iptables = 'on'
                }
            }
        }
        test_item.results(iptables)
    }

    def runlevel(session, test_item) {
        def lines = exec('runlevel') {
            run_ssh_command(session, 'grep :initdefault /etc/inittab', 'runlevel')
        }
        def runlevel = 'unkown'
        lines.eachLine {
            ( it =~ /^id:(\d+):/).each {m0,m1->
                runlevel = m1
            }
        }
        test_item.results(runlevel)
    }

    def resolve_conf(session, test_item) {
        def lines = exec('resolve_conf') {
            run_ssh_command(session, 'grep nameserver /etc/resolv.conf', 'resolve_conf')
        }
        def nameservers = [:]
        def nameserver_number = 1
        lines.eachLine {
            ( it =~ /^nameserver\s+(\w.+)$/).each {m0,m1->
                nameservers["nameserver${nameserver_number}"] = m1
                nameserver_number ++
            }
        }
        test_item.results([
            'resolve_conf' : (nameserver_number == 1) ? 'off' : 'on',
            'nameservers' : nameservers
        ])
    }

    def sestatus(session, test_item) {
        def lines = exec('sestatus') {
            run_ssh_command(session, '/usr/sbin/sestatus', 'sestatus')
        }
        def se_status = [:]
        lines.eachLine {
            ( it =~ /SELinux status:\s+(.+?)$/).each {m0,m1->
                se_status['sestatus'] = m1
            }
            ( it =~ /Current mode:\s+(.+?)$/).each {m0,m1->
                se_status['se_mode'] = m1
            }
        }
        test_item.results(se_status)
    }

}

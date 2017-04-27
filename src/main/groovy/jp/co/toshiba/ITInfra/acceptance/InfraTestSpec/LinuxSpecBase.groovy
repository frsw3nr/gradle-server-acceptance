package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.session.execution.*
import jp.co.toshiba.ITInfra.acceptance.*
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

@Slf4j
@InheritConstructors
class LinuxSpecBase extends InfraTestSpec {

    String ip
    String os_user
    String os_password
    String work_dir
    int    timeout = 30

    def init() {
        super.init()

        this.ip          = test_server.ip
        def os_account   = test_server.os_account
        this.os_user     = os_account['user']
        this.os_password = os_account['password']
        this.work_dir    = os_account['work_dir']
        this.timeout     = test_server.timeout
    }

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()

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
                try {
                    execute "mkdir -p ${work_dir}"
                } catch (Exception e) {
                    log.error "[SSH Test] Failed to create '$work_dir' in ${this.server_name}, skip.\n" + e
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
                            log.debug "Finish test method '${method.name}()' in ${this.server_name}, Elapsed : ${elapsed} ms"
                            it.succeed = 1
                        } catch (Exception e) {
                            it.verify_status(false)
                            log.error "[SSH Test] Test method '${method.name}()' faild, skip.\n" + e
                        }
                    }
                }
                try {
                    remove work_dir
                } catch (Exception e) {
                    log.error "[SSH Test] Failed to remove '$work_dir' in ${this.server_name} faild, skip.\n" + e
                    return
                }
            }
        }
        test_items.each { test_item ->
            if (test_item.test_id == 'logon_test') {
                _logon_test(test_item)
            }
        }
    }

    def _logon_test(TestItem test_item) {
        def results = [:]
        test_server.os_account.logon_test.each { test_user->
            def ssh = Ssh.newService()
            ssh.remotes {
                ssh_host {
                    host       = this.ip
                    port       = 22
                    user       = test_user.user
                    password   = test_user.password
                    knownHosts = allowAnyHosts
                }
            }
            ssh.settings {
                dryRun     = this.dry_run
                timeoutSec = this.timeout
            }
            try {
                ssh.run {
                    session(ssh.remotes.ssh_host) {
                            execute "who"
                            results[test_user.user] = true
                    }
                }
            } catch (Exception e) {
                log.error "[SSH Test] faild logon '${test_user.user}', skip.\n" + e
                results[test_user.user] = false
            }
        }
        test_item.results(results.toString())
    }

    def run_ssh_command(session, command, test_id, share = false) {
        try {
            def log_path = (share) ? evidence_log_share_dir : local_dir
            def result = session.execute command
            new File("${log_path}/${test_id}").text = result
        } catch (Exception e) {
            log.error "[SSH Test] Command error '$command' in ${this.server_name} faild, skip.\n" + e
        }
    }

    def run_ssh_sudo(session, command, test_id, share = false) {
        try {
            def log_path = (share) ? evidence_log_share_dir : local_dir
            def result = session.executeSudo command, pty: true, timeoutSec: timeout
            new File("${log_path}/${test_id}").text = result
        } catch (Exception e) {
            log.error "[SSH Test] Sudo error '$command' in ${this.server_name} faild, skip.\n" + e
        }
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
            def command = """\
            |if [ -f /etc/machine-id ]; then
            |    cat /etc/machine-id > ${work_dir}/machineid
            |elif [ -f /var/lib/dbus/machine-id ]; then
            |    cat /var/lib/dbus/machine-id > ${work_dir}/machineid
            |fi
            """.stripMargin()
            session.execute command
            session.get from: "${work_dir}/machineid", into: local_dir
            new File("${local_dir}/machineid").text
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
            run_ssh_command(session, '/sbin/ip addr', 'network')
        }
        def csv = []
        def network = [:].withDefault{[:]}
        def device = ''
        def hw_address = []
        lines.eachLine {
            // 2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
            (it =~  /^(\d+): (.+): <(.+)> (.+)$/).each { m0,m1,m2,m3,m4->
                device = m2
                if (m2 == 'lo') {
                    return
                }
                def index = 0
                def name  = ''
                m4.split(/ /).each{ n1->
                    if (index % 2 == 0) {
                        name = n1
                    } else {
                        network[device][name] = n1
                    }
                    index ++
                }
            }
            // inet 127.0.0.1/8 scope host lo
            (it =~ /inet\s+(.*?)\s/).each {m0, m1->
                network[device]['ip'] = m1
                try {
                    SubnetInfo subnet = new SubnetUtils(m1).getInfo()
                    network[device]['subnet'] = subnet.getNetmask()
                } catch (IllegalArgumentException e) {
                    log.error "[LinuxTest] subnet convert : m1\n" + e
                }
            }

            // link/ether 00:0c:29:c2:69:4b brd ff:ff:ff:ff:ff:ff promiscuity 0
            (it =~ /link\/ether\s+(.*?)\s/).each {m0, m1->
                network[device]['mac'] = m1
                hw_address.add(m1)
            }
        }
        // mtu:1500, qdisc:noqueue, state:DOWN, ip:172.17.0.1/16
        network.each { device_id, items ->
            def columns = [device_id]
            ['ip', 'mtu', 'state', 'mac', 'subnet'].each {
                columns.add(items[it] ?: 'NaN')
            }
            csv << columns
        }
        def headers = ['device', 'ip', 'mtu', 'state', 'mac', 'subnet']
        test_item.devices(csv, headers)

        test_item.results([
            'network' : network.keySet().toString(),
            'hw_address' : hw_address.toString()
            ])
    }

    def net_onboot(session, test_item) {
        def lines = exec('net_onboot') {
            def command = """\
            |cd /etc/sysconfig/network-scripts/
            |grep ONBOOT ifcfg-* >> ${work_dir}/net_onboot
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

    def net_route(session, test_item) {
        def lines = exec('net_route') {
            run_ssh_command(session, '/sbin/ip route', 'net_route')
        }
        def net_route = [:]
        lines.eachLine {
            // default via 192.168.10.254 dev eth0
            (it =~ /default via (.+?) dev (.+?)\s/).each {m0,m1,m2->
                net_route[m2] = m1
            }
        }
        test_item.results(net_route.toString())
    }

    def net_bond(session, test_item) {
        def lines = exec('net_bond') {
            def command = """\
            |cd /etc/sysconfig/network-scripts/
            |cat *-bond* 2>/dev/null
            |if [ \$? != 0 ]; then
            |   echo 'Not found'
            |fi
            """.stripMargin()
            run_ssh_command(session, command, 'net_bond')
        }
        def results = [:].withDefault{[]}
        lines.eachLine {
            // DEVICE=bond0
            (it =~ /^DEVICE=(.+)$/).each {m0,m1->
                results['net_bond'] << m1
            }
            // BONDING_OPTS="mode=1 miimon=100 updelay=100"
            (it =~ /^BONDING_OPTS="(.+)"$/).each {m0,m1->
                results['net_bond_opts'] << m1
            }
        }
        test_item.results(results)
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

        // NAME                          MAJ:MIN RM  SIZE RO TYPE MOUNTPOINT
        // sr0                            11:0    1 1024M  0 rom
        // sda                             8:0    0   30G  0 disk
        // ├─sda1                          8:1    0  500M  0 part /boot
        // └─sda2                          8:2    0 29.5G  0 part
        //   ├─vg_ostrich-lv_root (dm-0) 253:0    0 26.5G  0 lvm  /
        //   └─vg_ostrich-lv_swap (dm-1) 253:1    0    3G  0 lvm  [SWAP]
        def csv = []
        def filesystems = [:]
        lines.eachLine {
            (it =~  /^(.+?)\s+(\d+:\d+\s.+)$/).each { m0,m1,m2->
                def device = m1
                def arr = [device]
                def columns = m2.split(/\s+/)
                if (columns.size() == 6) {
                    def mount = columns[5]
                    filesystems['filesystem.' + mount] = columns[2]
                }
                arr.addAll(columns)
                csv << arr
            }
        }
        def headers = ['name', 'maj:min', 'rm', 'size', 'ro', 'type', 'mountpoint']
        test_item.devices(csv, headers)
        filesystems['filesystem'] = csv.size()
        test_item.results(filesystems)
    }


    def lvm(session, test_item) {
        def lines = exec('lvm') {
            run_ssh_command(session, 'mount', 'lvm')
        }

        // /dev/mapper/vg_ostrich-lv_root on / type ext4 (rw)
        def csv = []
        def lvms = [:]
        lines.eachLine {
            (it =~  /^\/dev\/mapper\/(.+?)-(.+?) on (.+?) /).each {
                m0, vg_name, lv_name, mount ->
                def columns = [vg_name, lv_name, mount]
                lvms[lv_name] = mount
                csv << columns
            }
        }
        def headers = ['vg_name', 'lv_name', 'mountpoint']
        test_item.devices(csv, headers)
        test_item.results(lvms.toString())
    }

    def filesystem_df_ip(session, test_item) {
        def lines = exec('filesystem_df_ip') {
            run_ssh_command(session, 'df -iP', 'filesystem_df_ip')
        }
        test_item.results(lines)
    }

    def fstab(session, test_item) {
        def lines = exec('fstab') {
            run_ssh_command(session, 'cat /etc/fstab', 'fstab')
        }
        def results = [:]
        lines.eachLine {
            // /dev/mapper/vg_paas-lv_root /  ext4  defaults        1 1
            (it =~ /^(\/.+?)\s+(.+?)\s+(.+?)\s+defaults\s/).each {m0,m1,m2,m3->
                results[m2] = m1
            }
        }
        test_item.results((results.size() == 0) ? 'NotFound' : results.toString())
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
        def package_info = [:].withDefault{0}
        def distributions = [:].withDefault{0}
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
            } else if (release =~ /el7/) {
                release_label = 'RHEL7'
            }
            def install_time = Long.decode(arr[4]) * 1000L
            arr[4] = new Date(install_time).format("yyyy/MM/dd HH:mm:ss")
            csv << arr
            def arch    = (arr[5] == '(none)') ? 'noarch' : arr[5]
            distributions[release_label] ++
            package_info['packages.' + packagename] = arr[2]
        }
        def headers = ['name', 'epoch', 'version', 'release', 'installtime', 'arch']
        package_info['packages'] = distributions.toString()
        test_item.devices(csv, headers)
        test_item.results(package_info)
    }

    def cron(session, test_item) {
        def users = exec('cron') {
            run_ssh_sudo(session, "ls /var/spool/cron/ |cat", 'cron')
        }
        def csv = []
        users.eachLine { user ->
            def id = "cron.$user"
            def command = "crontab -l -u $user"
            def lines = exec(id) {
                run_ssh_sudo(session, command, id)
            }
            lines.eachLine {
                (it =~ /^\s*[^#].+$/).each {m0 ->
                    csv << [user, it]
                }
            }
        }
        def headers = ['user', 'crontab']
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def yum(session, test_item) {
        def lines = exec('yum') {
            def command = "egrep -e '\\[|enabled' /etc/yum.repos.d/*.repo"
            run_ssh_command(session, command, 'yum')
        }
        def yum_info = [:].withDefault{0}
        def repository = 'Unkown'
        def csv = []
        def row = []
        lines.eachLine {
            (it =~/\[(.+)\]/).each {m0, m1 ->
                row << m1
            }
            (it =~/:\s*enabled=(.+)/).each {m0, m1 ->
                row << m1
                if (row.size() == 2) {
                    csv << row
                    row = []
                }
            }
        }
        def headers = ['repository', 'enabled']
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def resource_limits(session, test_item) {
        def lines = exec('resource_limits') {
            def command = "egrep -v '^#' /etc/security/limits.d/*"
            run_ssh_command(session, command, 'resource_limits')
        }
        def csv = []
        def row = []
        lines.eachLine {
            (it =~/limits\.d\/(.+?):(.+)$/).each {m0, m1, m2 ->
                csv << [m1, m2]
            }
        }
        def headers = ['source', 'limits']
        test_item.devices(csv, headers)
        def csv_rows = csv.size()
        def result = (csv_rows == 0) ? 'No limits setting' : "${csv_rows} records found"
        test_item.results(csv.size().toString())
    }

    def user(session, test_item) {
        def lines = exec('user') {
            run_ssh_command(session, "cat /etc/passwd", 'user')
        }
        def group_lines = exec('group') {
            run_ssh_command(session, "cat /etc/group", 'group')
        }
        def groups = [:].withDefault{0}
        // root:x:0:
        group_lines.eachLine {
            ( it =~ /^(.+?):(.+?):(\d+)/).each {m0,m1,m2,m3->
                groups[m3] = m1
            }
        }
        def csv = []
        def user_count = 0
        def users = [:].withDefault{'unkown'}
        lines.eachLine {
            def arr = it.split(/:/)
            if (arr.size() > 4) {
                def username = arr[0]
                def user_id  = arr[2]
                def group_id = arr[3]
                def group    = groups[group_id] ?: 'Unkown'

                csv << [username, user_id, group_id, group]
                user_count ++
                users['user.' + username] = 'OK'
            }
        }
        def headers = ['UserName', 'UserID', 'GroupID', 'Group']
        test_item.devices(csv, headers)
        users['user'] = user_count
        test_item.results(users)
    }

    def crontab(session, test_item) {
        def csv  = []
        def cron_number = [:].withDefault{0}

        def lines = exec('crontab') {
            run_ssh_command(session, 'crontab -l', 'crontab')
        }
        lines.eachLine {
            (it =~ /^\s*\d/).each { m0 ->
                csv << ['user', it]
                cron_number['user'] ++
            }
        }
        def lines2 = exec('crontab2') {
            run_ssh_sudo(session, 'crontab -l', 'crontab2')
        }
        lines2.eachLine {
            (it =~ /^\s*\d/).each { m0 ->
                csv << ['root', it]
                cron_number['root'] ++
            }
        }
        def headers = ['account', 'crontab']
        test_item.devices(csv, headers)
        test_item.results(cron_number.toString())
    }

    def service(session, test_item) {
        def isRHEL7 = session.execute(' test -f /usr/bin/systemctl ; echo $?')
        if (isRHEL7 == '0') {
            def lines = exec('service') {
                run_ssh_command(session, '/usr/bin/systemctl list-units --type service --all', 'service')
            }
            def services = [:].withDefault{'unkown'}
            def csv = []
            def service_count = 0
            lines.eachLine {
                ( it =~ /^\s+(.+?)\.service\s+loaded\s+(\w+)\s+(\w+)\s/).each {m0,m1,m2,m3->
                    def service_name = 'service.' + m1
                    def status = m2 + '.' + m3
                    services[service_name] = status
                    def columns = [m1, status]
                    csv << columns
                    service_count ++
                }
            }
            services['service'] = service_count.toString()
            test_item.devices(csv, ['Name', 'Status'])
            test_item.results(services)

        } else {
            def lines = exec('service') {
                run_ssh_command(session, '/sbin/chkconfig --list', 'service')
            }
            def services = [:].withDefault{'unkown'}
            def csv = []
            def service_count = 0
            lines.eachLine {
                ( it =~ /^(.+?)\s.*\s+3:(.+?)\s+4:(.+?)\s+5:(.+?)\s+/).each {m0,m1,m2,m3,m4->
                    def service_name = 'service.' + m1
                    def status = (m2 == 'on' && m3 == 'on' && m4 == 'on') ? 'On' : 'Off'
                    services[service_name] = status
                    def columns = [m1, status]
                    csv << columns
                    service_count ++
                }
            }
            services['service'] = service_count.toString()
            test_item.devices(csv, ['Name', 'Status'])
            test_item.results(services)
        }
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
            |ls -d /opt/oracle/app/product/*/* /*/app/oracle/product/*/* 2>/dev/null
            |if [ \$? != 0 ]; then
            |   echo 'Not found'
            |fi
            """.stripMargin()
            run_ssh_command(session, command, 'oracle')
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
            def command = """\
            |grep proxy /etc/yum.conf
            |if [ \$? != 0 ]; then
            |    echo 'Not found'
            |fi
            """.stripMargin()
            run_ssh_command(session, command, 'proxy_global')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def kdump(session, test_item) {
        def isRHEL7 = session.execute(' test -f /usr/bin/systemctl ; echo $?')
        if (isRHEL7 == '0') {
            def lines = exec('kdump') {
                run_ssh_command(session, '/usr/bin/systemctl status kdump', 'kdump')
            }
            def kdump = 'inactive'
            lines.eachLine {
                ( it =~ /Active: (.+?)\s/).each {m0,m1->
                     kdump = m1
                }
            }
            test_item.results(kdump)
        } else {
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
    }

    def crash_size(session, test_item) {
        def lines = exec('crash_size') {
            run_ssh_command(session, 'cat /sys/kernel/kexec_crash_size', 'crash_size')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def iptables(session, test_item) {
        def isRHEL7 = session.execute(' test -f /usr/bin/systemctl ; echo $?')
        if (isRHEL7 == '0') {
            def lines = exec('iptables') {
                def command = "/usr/bin/systemctl status iptables firewalld >> ${work_dir}/iptables"
                session.execute command, ignoreError : true
                session.get from: "${work_dir}/iptables", into: local_dir
                new File("${local_dir}/iptables").text
            }
            def services = [:]
            def service = 'iptables'
            lines.eachLine {
                ( it =~ /\s(.+?)\.service\s/).each {m0,m1->
                     service = m1
                }
                ( it =~ /^\s+Active: (.+?)\s/).each {m0,m1->
                     services[service] = m1
                }
            }
            test_item.results(services.toString())
        } else {
            def lines = exec('iptables') {
            // REHL7 verify command : /usr/bin/systemctl status iptables
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
    }

    def runlevel(session, test_item) {
        def isRHEL7 = session.execute('test -f /usr/bin/systemctl ; echo $?')
        if (isRHEL7 == '0') {
            def lines = exec('runlevel') {
                run_ssh_command(session, '/usr/bin/systemctl get-default', 'runlevel')
            }
            test_item.results(lines)
        } else {
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

    def ntp(session, test_item) {
        def lines = exec('ntp') {
            run_ssh_command(session, "egrep -e '^server' /etc/ntp.conf", 'ntp')
        }
        def ntpservers = []
        lines.eachLine {
            ( it =~ /^server\s+(\w.+)$/).each {m0,m1->
                ntpservers.add(m1)
            }
        }
        test_item.results(ntpservers.toString())
    }

    def ntp_slew(session, test_item) {
        def lines = exec('ntp_slew') {
            def command = """\
            |grep -i options /etc/sysconfig/ntpd 2>/dev/null
            |if [ \$? != 0 ]; then
            |   echo 'Not found'
            |fi
            """.stripMargin()
            run_ssh_command(session, command, 'ntp_slew')
        }
        def result = 'NotFound'
        lines.eachLine {
            (it =~ /-u/).each {m0->
                result = 'Disabled'
            }
            (it =~ /-x/).each {m0->
                result = 'Enabled'
            }
        }
        test_item.results(result)
    }

    def snmp_trap(session, test_item) {
        def lines = exec('snmp_trap') {
            def command = "egrep -e '^\\s*trapsink' /etc/snmp/snmpd.conf >> ${work_dir}/snmp_trap; echo \$?"
            try {
                def result = session.executeSudo command, pty: true, timeoutSec: timeout
                log.info result
                session.get from: "${work_dir}/snmp_trap", into: local_dir
                new File("${local_dir}/snmp_trap").text
            } catch (Exception e) {
                log.info "[sudo] Error ${command}" + e
            }
        }
        def result = 'NotFound'
        lines.eachLine {
            (it =~  /trapsink\s+(.*)$/).each { m0, trap_info ->
                result = trap_info
            }
        }
        test_item.results(result)
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

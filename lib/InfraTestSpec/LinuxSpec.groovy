package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import groovy.transform.InheritConstructors
import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.session.execution.*
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@InheritConstructors
class LinuxSpec extends LinuxSpecBase {

    def init() {
        super.init()
    }

    def finish() {
        super.finish()
    }

    def hostname(session, test_item) {
        def lines = exec('hostname') {
            run_ssh_command(session, 'hostname -s', 'hostname')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
        test_item.exclude_compare()
    }

    def hostname_fqdn(session, test_item) {
        def command = """\
        |hostname --fqdn 2>/dev/null
        |if [ \$? != 0 ]; then
        |   echo 'Not Found'
        |fi
        """.stripMargin()

        def lines = exec('hostname_fqdn') {
            run_ssh_command(session, command, 'hostname_fqdn')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        def info = 'NotConfigured'
        lines.eachLine {
            if (it.indexOf('.') != -1)
                info = it
        }
        test_item.results(info)
        test_item.exclude_compare()
    }

    def uname(session, test_item) {
        def lines = exec('uname') {
            run_ssh_command(session, 'uname -a', 'uname')
        }

        // parse 'Linux ostrich 2.6.32-573.12.1.el6.x86_64'
        def infos = [:]
        def oracle_linux_kernel = 'RedHat Compatible'
        lines.eachLine {
            (it =~ /^(.+)\.(.+?)#/).each {m0, kernel, arch ->
                infos['uname']  = "${kernel}.${arch}"
                infos['kernel'] = kernel
                infos['arch']   = arch
            }
            (it =~/uek/).each {
                oracle_linux_kernel = 'UEK'
            }
        }
        infos['oracle_linux_kernel'] = oracle_linux_kernel
        test_item.results(infos)
        test_item.verify_text_search('kernel', infos['kernel'])
        test_item.verify_text_search('arch', infos['arch'])
        test_item.exclude_compare(['uname', 'kernel'])
    }

    def lsb(session, test_item) {
        def lines = exec('lsb') {
            run_ssh_command(session, 'cat /etc/*-release', 'lsb')
        }
        def scan_lines = [:]
        // = を含む行を除いて、重複行を取り除く
        lines.eachLine {
            if (it.indexOf('=') == -1 && it.size() > 0) {
                scan_lines[it] = ''
            }
        }
        def lsb = scan_lines.keySet().toString()

        // parse 'CentOS release 6.7 (Final)'
        def infos = [:]
        infos['lsb'] = lsb
        (lsb =~ /^\[(.+) ([\d\.]+)/).each {m0, os, os_release ->
            infos['os'] = "${os} ${os_release}"
            infos['os_release'] = os_release
        }
        test_item.results(infos)
        test_item.verify_text_search('os', infos['os'])
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
				def mhz = NumberUtils.toDouble(m1)
                if (!cpuinfo.containsKey("mhz") || cpuinfo["mhz"] < mhz) {
                    cpuinfo["mhz"] = mhz
                }
                // cpuinfo["mhz"] = mhz
            }
            (it =~ /cache size\s+:\s(.+)/).each {m0,m1->
                cpuinfo["cache_size"] = m1
            }
        }
        cpuinfo["cpu_total"] = cpu_number
        cpuinfo["cpu_real"] = real_cpu.size()
        cpuinfo["cpu_core"] = real_cpu.size() * cpuinfo["cores"].toInteger()
        def cpu_text = cpuinfo['model_name']
        if (cpuinfo["cpu_real"] > 0)
            cpu_text += " ${cpuinfo['cpu_real']} Socket ${cpuinfo['cpu_core']} Core"
        else
            cpu_text += " ${cpu_number} CPU"
        cpuinfo["cpu"] = cpu_text

        test_item.results(cpuinfo)
        test_item.verify_number_equal('cpu_total', cpuinfo['cpu_total'])
        test_item.verify_number_equal('cpu_real', cpuinfo['cpu_real'])
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
        test_item.exclude_compare()
    }

    def meminfo(session, test_item) {
        def lines = exec('meminfo') {
            run_ssh_command(session, 'cat /proc/meminfo', 'meminfo')
        }
        Closure norm = { value, unit ->
            def value_number = NumberUtils.toDouble(value)
            if (unit == 'kB') {
                return String.format("%1.1f", value_number / (1024 * 1024))
            } else if (unit == 'mB') {
                return String.format("%1.1f", value_number / 1024)
            } else if (unit == 'gB') {
                return value_number
            } else {
                return "${value}${unit}"
            }
        }
        def meminfo    = [:].withDefault{0}
        lines.eachLine {
            (it =~ /^MemTotal:\s+(\d+) (.+)$/).each {m0,m1,m2->
                meminfo['meminfo'] = "${m1} ${m2}"
                meminfo['mem_total'] = norm(m1, m2)
            }
            (it =~ /^MemFree:\s+(\d+) (.+)$/).each {m0,m1,m2->
                meminfo['mem_free'] = norm(m1, m2)
            }
        }
        test_item.results(meminfo)
        test_item.verify_number_equal('mem_total', meminfo['mem_total'], 0.1)
    }

    def network(session, test_item) {
        def lines = exec('network') {
            run_ssh_command(session, '/sbin/ip addr', 'network')
        }
        def network = [:].withDefault{[:]}
        def device  = ''
        def net_ip  = [:]
        def subnets = [:]
        def res     = [:]
        def ipv6    = 'Disable'
        def exclude_compares = []
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
            (it =~ /inet\s+(.+?)\s(.+)/).each {m0, m1, m2->
                def comments = m2.split(" ")
                device = comments.last()
                network[device]['ip'] = m1

                try {
                    SubnetInfo subnet = new SubnetUtils(m1).getInfo()
                    def netmask = subnet.getNetmask()
                    network[device]['subnet'] = netmask
                    // Regist Port List
                    def ip_address = subnet.getAddress()
                    if (ip_address && ip_address != '127.0.0.1') {
                        test_item.lookuped_port_list(ip_address, device)
                        add_new_metric("network.ip.${device}",     "[${device}] IP", ip_address, res)
                        exclude_compares << "network.ip.${device}"
                        add_new_metric("network.subnet.${device}", "[${device}] サブネット", 
                                       netmask, res)
                        subnets[device] = netmask
                        net_ip[device] = ip_address
                    }
                } catch (IllegalArgumentException e) {
                    log.error "[LinuxTest] subnet convert : m1\n" + e
                }
            }

            // link/ether 00:0c:29:c2:69:4b brd ff:ff:ff:ff:ff:ff promiscuity 0
            (it =~ /link\/ether\s+(.*?)\s/).each {m0, m1->
                add_new_metric("network.mac.${device}", "[${device}] MAC", m1, res)
                exclude_compares << "network.mac.${device}"
            }
            (it =~ /inet6/).each { m0 ->
                ipv6 = 'Enabled'
            }
        }
        add_new_metric("network.ipv6_enabled", "ネットワーク.IPv6", ipv6, res)
        // mtu:1500, qdisc:noqueue, state:DOWN, ip:172.17.0.1/16
        def csv        = []
        network.each { device_id, items ->
            def columns = [device_id]
            ['ip', 'mtu', 'state', 'mac', 'subnet'].each {
                columns.add(items[it] ?: 'NaN')
            }
            csv << columns
            net_ip[device_id] = items['ip']
        }
        def headers = ['device', 'ip', 'mtu', 'state', 'mac', 'subnet']
        res['net_ip'] = net_ip.toString()
        res['net_subnet'] = subnets.toString()
        res['network'] = "${subnets.size()} IPs, IPv6:${ipv6}"
        test_item.results(res)
        test_item.devices(csv, headers)
        test_item.verify_text_search_list('net_ip', net_ip)
        test_item.exclude_compare(exclude_compares)
    }

    // def convert_array(element) {
    //     if (element.getClass() == String)
    //         return [element]
    //     else
    //         return element
    // }

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
        def infos = [:]
        lines.eachLine {
            (it =~ /^ifcfg-(.+):ONBOOT=(.+)$/).each {m0,m1,m2->
                infos[m1] = m2
            }
        }
        def result = infos.toString()
        test_item.results(result)
        test_item.verify_text_search_list('net_onboot', infos)
    }

    def net_route(session, test_item) {
        def lines = exec('net_route') {
            run_ssh_command(session, '/sbin/ip route', 'net_route')
        }
        def infos = [:]
        lines.eachLine {
            // default via 192.168.10.254 dev eth0
            (it =~ /default via (.+?) dev (.+?)\s/).each {m0,m1,m2->
                infos[m1] = m2
            }
        }
        test_item.results(infos.toString())
        test_item.verify_text_search_list('net_route', infos)
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
        def configured = 'NotConfigured'
        def devices    = []
        def options    = []
        lines.eachLine {
            // DEVICE=bond0
            (it =~ /^DEVICE=(.+)$/).each {m0,m1->
                devices << m1
                configured = 'Configured'
            }
            // BONDING_OPTS="mode=1 miimon=100 updelay=100"
            (it =~ /^BONDING_OPTS="(.+)"$/).each {m0,m1->
                options << m1
            }
        }
        def results = ['bonding': configured, 'devices': devices, 'options': options]
        test_item.results(results.toString())
    }

    def block_device(session, test_item) {
        def lines = exec('block_device') {
            def command = """\
            |egrep -H '.*' /sys/block/*/size                      >> ${work_dir}/block_device
            |egrep -H '.*' /sys/block/*/removable                 >> ${work_dir}/block_device
            |egrep -H '.*' /sys/block/*/device/model              >> ${work_dir}/block_device
            |egrep -H '.*' /sys/block/*/device/rev                >> ${work_dir}/block_device
            |egrep -H '.*' /sys/block/*/device/state              >> ${work_dir}/block_device
            |egrep -H '.*' /sys/block/*/device/timeout            >> ${work_dir}/block_device
            |egrep -H '.*' /sys/block/*/device/vendor             >> ${work_dir}/block_device
            |egrep -H '.*' /sys/block/*/device/queue_depth        >> ${work_dir}/block_device
            """
            session.execute command.stripMargin()
            session.get from: "${work_dir}/block_device", into: local_dir
            new File("${local_dir}/block_device").text
        }
        int device_count = 0
        def res = [:]
        lines.eachLine {
            (it =~  /^\/sys\/block\/(.+?)\/(.+):(.+)$/).each { m0,m1,m2,m3->
                if (m1 =~ /(ram|loop)/) {
                    return
                }
                if (m2 == 'device/timeout') {
                    add_new_metric("block_device.${m1}.timeout", 
                                   "[${m1}] タイムアウト", m3, res)
                    device_count ++
                }
                if (m2 == 'device/queue_depth') {
                    add_new_metric("block_device.${m1}.queue_depth", 
                                   "[${m1}] キューサイズ", m3, res)
                }
            }
        }
        res['block_device'] = "${device_count} devices"
        test_item.results(res)
    }

    def mdadb(session, test_item) {
        def lines = exec('mdadb') {
            run_ssh_command(session, 'cat /proc/mdstat', 'mdadb')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    // def add_new_metric(String id, String description, value, Map results) {
    //     this.test_platform.add_test_metric(id, description)
    //     results[id] = value
    // }

    def convert_mount_short_name(String path) {
        return (path.length() > 22) ? "${path.substring(0, 22)}..." : path
    }

    def filesystem(session, test_item) {
        def fstabs = exec('fstab') {
            run_ssh_command(session, "cat /etc/fstab", 'fstab')
        }
        def fstypes = [:].withDefault{[]}
        def fstypes2 = [:]
        fstabs.eachLine {
            (it =~/^([^#].+?)\s+(.+?)\s+(.+?)\s/).each {m0, m1, m2, m3 ->
                def filter_fstype = m3 in ['tmpfs', 'devpts', 'sysfs', 'proc', 'swap']
                if (!filter_fstype) {
                    fstypes[m3] << m2
                }
                fstypes2[m2] = m3
            }
        }
        def lines = exec('filesystem') {
            def existLsblk = session.execute('test -f /bin/lsblk ; echo $?')
            def command = (existLsblk == '0') ? '/bin/lsblk -i' : '/bin/df -k'

            run_ssh_command(session, command, 'filesystem')
        }

        // NAME                          MAJ:MIN RM  SIZE RO TYPE MOUNTPOINT
        // sr0                            11:0    1 1024M  0 rom
        // sda                             8:0    0   30G  0 disk
        // ├─sda1                          8:1    0  500M  0 part /boot
        // └─sda2                          8:2    0 29.5G  0 part
        //   ├─vg_ostrich-lv_root (dm-0) 253:0    0 26.5G  0 lvm  /
        //   └─vg_ostrich-lv_swap (dm-1) 253:1    0    3G  0 lvm  [SWAP]
        def csv = []
        // def filesystems = [:]
        def infos = [:]
        def res = [:]
        // println fstypes
        lines.eachLine {
            (it =~  /^(.+?)\s+(\d+:\d+\s.+)$/).each { m0,m1,m2->
                def device = m1
                def device_node = device
                (device_node =~ /([a-zA-Z].+)/).each { n0, n1->
                    device_node = n1
                }
                def arr = [device]
                def columns = m2.split(/\s+/)
                if (columns.size() == 6) {
                    def mount    = columns[5]
                    def capacity = columns[2]
                    add_new_metric("filesystem.capacity.${mount}", "[${mount}] 容量", 
                                   capacity, res)
                    add_new_metric("filesystem.device.${mount}", "[${mount}] デバイス", 
                                   convert_mount_short_name(device_node), res)
                    add_new_metric("filesystem.type.${mount}", "[${mount}] タイプ", 
                                   columns[4], res)
                    add_new_metric("filesystem.fstype.${mount}", "[${mount}] ファイルシステム", 
                                   fstypes2[mount] ?: '', res)
                    // this.test_platform.add_test_metric(id, "ディスク容量.${mount}")
                    // res[id] = capacity

                    // filesystems['filesystem.' + mount] = columns[2]
                    infos[convert_mount_short_name(mount)] = capacity
                }
                arr.addAll(columns)
                csv << arr
            }
            (it =~  /^(.+?)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)%\s+(.+?)$/).each {
                m0, device, capacity, m3, m4, m5, mount->
                def columns = [device, '', '', capacity, '', '', mount]
                // filesystems['filesystem.' + mount] = capacity
                infos[convert_mount_short_name(mount)] = capacity
                // columns << fstypes[mount] ?: ''
                add_new_metric("filesystem.capacity.${mount}", "[${mount}] 容量", capacity, res)
                add_new_metric("filesystem.device.${mount}", "[${mount}] デバイス", device, res)

                csv << columns
            }
        }
        def headers = ['name', 'maj:min', 'rm', 'size', 'ro', 'type', 'mountpoint', 'fstype']
        // println csv
        // println filesystems
        test_item.devices(csv, headers)
        // filesystems['filesystem'] = infos.toString()
        // filesystems['fstype']     = fstypes.toString()
        // println filesystems
        // test_item.results(filesystems)
        // println "${infos}"
        res['filesystem'] = "${infos}"
        test_item.results(res)
        test_item.verify_text_search_map('filesystem', infos)
    }

    def lvm(session, test_item) {
        def lines = exec('lvm') {
            run_ssh_command(session, 'mount', 'lvm')
        }

        // /dev/mapper/vg_ostrich-lv_root on / type ext4 (rw)
        def csv    = []
        // def lvms   = [:]
        def config = 'NotConfigured'
        def res = [:]
        lines.eachLine {
            (it =~  /^\/dev\/mapper\/(.+?)-(.+?) on (.+?) /).each {
                m0, vg_name, lv_name, mount ->
                def columns = [vg_name, lv_name, mount]
                // lvms[lv_name] = mount
                def lv_name_short = convert_mount_short_name(lv_name)
                csv << columns
                config = 'Configured'
                add_new_metric("lvm.${mount}", 
                               "LVM [${mount}]", "${vg_name}:${lv_name_short}", res)
            }
        }
        def headers = ['vg_name', 'lv_name', 'mountpoint']
        test_item.devices(csv, headers)
        // def results = ['lvm': config, 'devices': lvms]
        res['lvm'] = config
        // test_item.results(results.toString())
        test_item.results(res)
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
        def mounts = [:]
        def fstypes = [:].withDefault{[]}
        lines.eachLine {
            // /dev/mapper/vg_paas-lv_root /  ext4  defaults        1 1
            (it =~ /^([^#].+?)\s+(.+?)\s+(.+?)\s+defaults\s/).each {m0,m1,m2,m3->
                mounts[m2] = m1
                def filter_fstype = m3 in ['tmpfs', 'devpts', 'sysfs', 'proc', 'swap']
                if (!filter_fstype) {
                    fstypes[m3] << m2
                }
            }
        }
        // println fstypes
        // println mounts
        def infos = [
            'fstab' : (mounts.size() == 0) ? 'Not Found' : "${mounts.keySet()}",
            'fstypes': "${fstypes}",
        ]
        // println infos
        test_item.results(infos)
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
        def package_info = [:].withDefault{'unkown'}
        def versions = [:]
        // def infos = [:].withDefault{'unkown'}
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
            if (arch == 'i686') {
                packagename += ".i686"
            }
            // package_info['packages.' + packagename] = arr[2]
            versions[packagename] = arr[2]
        }
        def headers = ['name', 'epoch', 'version', 'release', 'installtime', 'arch']
        package_info['packages'] = distributions.toString()

        def package_list = test_item.target_info('packages')
        if (package_list) {
            def template_id = this.test_platform.test_target.template_id
            package_info['packages.requirements'] = "${package_list.keySet()}"
            def verify = true
            package_list.each { package_name, value ->
                def test_id = "packages.${template_id}.${package_name}"
                def version = versions[package_name] ?: 'Not Found'
                if (version == 'Not Found') {
                    verify = false
                }
                add_new_metric(test_id, "${template_id}.${package_name}", "'${version}'", package_info)
            }
            test_item.verify(verify)
        }
        versions.sort().each { package_name, version ->
            if (package_list?."${package_name}") {
                return
            }
            def test_id = "packages.Etc.${package_name}"
            add_new_metric(test_id, package_name, "'${version}'", package_info)
        }
        test_item.devices(csv, headers)
        test_item.results(package_info)
        test_item.verify_text_search_list('packages', package_info)
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
        test_item.results(result)
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
        def general_users = [:]
        def users = [:].withDefault{'unkown'}
        def infos = [:]
        lines.eachLine {
            def arr = it.split(/:/)
            if (arr.size() == 7) {
                def username = arr[0]
                def user_id  = "'${arr[2]}'"
                def group_id = arr[3]
                def home     = arr[5]
                def shell    = arr[6]
                def group    = groups[group_id] ?: 'Unkown'

                csv << [username, user_id, group_id, group, home, shell]
                infos[username] = 1
                // users['user.' + username] = 'OK'
                (shell =~ /sh$/).each {
                    general_users[username] = 'OK'
                    add_new_metric("user.${username}.id",    "[${username}] ID",       user_id, users)
                    add_new_metric("user.${username}.home",  "[${username}] ホーム",   home, users)
                    add_new_metric("user.${username}.group", "[${username}] グループ", group, users)
                    add_new_metric("user.${username}.shell", "[${username}] シェル",   shell, users)
                }
            }
        }
        def headers = ['UserName', 'UserID', 'GroupID', 'Group', 'Home', 'Shell']
        test_item.devices(csv, headers)
        users['user'] = general_users.keySet().toString()
        test_item.results(users)
        test_item.verify_text_search_list('user', infos)
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
        def command = (isRHEL7 == '0') ?
            '/usr/bin/systemctl list-units --type service --all' :
            '/sbin/chkconfig --list'

        def lines = exec('service') {
            run_ssh_command(session, command, 'service')
        }

        def services = [:].withDefault{'Not found'}
        def statuses = [:]
        // def infos = [:]
        def csv = []
        def service_count = 0

        lines.eachLine {
            // For RHEL7
            // abrt-ccpp.service     loaded    active   exited  Install ABRT coredump hook
              // NetworkManager.service   loaded    inactive dead    Network Manager
            ( it =~ /\s+(.+?)\.service\s+loaded\s+(\w+)\s+(\w+)\s/).each {m0,m1,m2,m3->
                def service_name = m1
                (service_name =~/^(.+?)@(.+?)$/).each { n0, n1, n2 ->
                    service_name = n1 + '@' + 'LABEL'
                }
                def status = m2 + '.' + m3
                statuses[service_name] = status
                // infos[service_name] = status
                def columns = [service_name, status]
                csv << columns
                service_count ++
            }
            // For RHEL6
            // ypbind          0:off   1:off   2:off   3:off   4:off   5:off   6:off
            ( it =~ /^(.+?)\s.*\s+3:(.+?)\s+4:(.+?)\s+5:(.+?)\s+/).each {m0,m1,m2,m3,m4->
                def service_name = m1
                def status = (m2 == 'on' && m3 == 'on' && m4 == 'on') ? 'On' : 'Off'
                statuses[service_name] = status
                // infos[service_name] = status
                def columns = [m1, status]
                csv << columns
                service_count ++
            }
        }
        def service_list = test_item.target_info('service')
        if (service_list) {
            def template_id = this.test_platform.test_target.template_id
            service_list.each { service_name, value ->
                def test_id = "service.${template_id}.${service_name}"
                def status = statuses[service_name] ?: 'Not Found'
                add_new_metric(test_id, "${template_id}.${service_name}", status, services)
            }
        }
        statuses.sort().each { service_name, status ->
            if (service_list?."${service_name}") {
                return
            }
            def test_id = "service.Etc.${service_name}"
            add_new_metric(test_id, service_name, status, services)
        }
        services['service'] = "${service_count} services"
        test_item.devices(csv, ['Name', 'Status'])
        test_item.results(services)
        // test_item.verify_text_search_map('service', infos)
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
        def oracleinfo = 'Not Found'
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
            def command = """\
            |cat /sys/kernel/kexec_crash_size 2>/dev/null
            |if [ \$? != 0 ]; then
            |   echo 'Unkown crash_size. kdump:'
            |   cat /sys/kernel/kexec_crash_loaded
            |fi
            """.stripMargin()
            run_ssh_command(session, command, 'crash_size')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def kdump_path(session, test_item) {
        def lines = exec('kdump_path') {
            def command = """\
            |egrep -e '^(path|core_collector)' /etc/kdump.conf 2>/dev/null
            |if [ \$? != 0 ]; then
            |   echo 'Not found'
            |fi
            """.stripMargin()
            run_ssh_command(session, command, 'kdump_path')
        }
        def path = '/var/crash'
        def core_collector = 'unkown'
        lines.eachLine {
            ( it =~ /path\s+(.+?)$/).each {m0, m1->
                path = m1
            }
            ( it =~ /core_collector\s+(.+?)$/).each {m0, m1->
                core_collector = m1
            }
        }
        def infos = [
            'kdump_path' : path,
            'core_collector' : core_collector
        ]
        test_item.results(infos)
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
        def command = (isRHEL7 == '0') ? '/usr/bin/systemctl get-default' : 'grep :initdefault /etc/inittab'
        def lines = exec('runlevel') {
            run_ssh_command(session, command, 'runlevel')
        }
        def runlevel = "$lines"
        def console  = 'CUI'
        lines.eachLine {
            ( it =~ /^id:(\d+):/).each {m0,m1->
                runlevel = m1
            }
        }
        if (runlevel == 'graphical.target' || runlevel == '5')
            console == 'GUI'
        test_item.results(['runlevel':runlevel, 'runlevel.console':console])
    }

    def resolve_conf(session, test_item) {
        def lines = exec('resolve_conf') {
            def command = """\
            |grep nameserver /etc/resolv.conf 2>/dev/null
            |if [ \$? != 0 ]; then
            |   echo 'Not Found'
            |fi
            """.stripMargin()
            run_ssh_command(session, command, 'resolve_conf')
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

    def grub(session, test_item) {
        def lines = exec('ntp') {
            def command = """\
            |grep GRUB_CMDLINE_LINUX /etc/default/grub 2>/dev/null
            |if [ \$? != 0 ]; then
            |   echo 'Not found'
            |fi
            """.stripMargin()
            run_ssh_command(session, command, 'vga')
        }
        def infos = [:].withDefault{'Not Found'}
        lines.eachLine {
            ( it =~ /^GRUB_CMDLINE_LINUX="(.+)"/).each {m0,m1->
                def parameters = m1.split(/\s/)
                parameters.each { parameter ->
                    ( parameter =~ /^(.+)=(.+)$/).each {n0, n1, n2->
                        infos["grub.${n1}"] = n2
                    }
                }
            }
        }
        def key_values = ['ipv6.disable', 'vga'].collect {
            def key = "grub.${it}"
            "$it=${infos[key]}"
        }
        infos['grub'] = key_values.join(",")
        test_item.results(infos)
    }


    def ntp(session, test_item) {
        def lines = exec('ntp') {
            def command = """\
            |egrep -e '^server' /etc/ntp.conf 2>/dev/null
            |if [ \$? != 0 ]; then
            |   echo 'Not found'
            |fi
            """.stripMargin()
            run_ssh_command(session, command, 'ntp')
        }
        def ntpservers = []
        def res = [:]
        lines.eachLine {
            ( it =~ /^server\s+(\w.+)$/).each {m0,ntp_server->
                add_new_metric("ntp.${ntp_server}", 
                               ntp_server, 'Enable', res)
                ntpservers.add(ntp_server)
            }
        }
        res['ntp'] = (ntpservers.size() == 0) ? 'off' : 'on'
        test_item.results(res)
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
        def result = 'Not Found'
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
            // def command = "egrep -e '^\\s*trapsink' /etc/snmp/snmpd.conf >> ${work_dir}/snmp_trap; echo \$?"
            def command = "cat /etc/snmp/snmpd.conf >> ${work_dir}/snmp_trap; echo \$?"
            try {
                def result = session.executeSudo command, pty: true, timeoutSec: timeout
                session.get from: "${work_dir}/snmp_trap", into: local_dir
                new File("${local_dir}/snmp_trap").text
            } catch (Exception e) {
                log.info "[sudo] Error ${command}" + e
            }
        }
        def config = 'NotConfigured'
        def res = [:]
        def trapsink = []
        lines.eachLine {
            (it =~  /(trapsink|trapcommunity|trap2sink|informsink)\s+(.*)$/).each { m0, m1, m2 ->
                config = 'Configured'
                add_new_metric("snmp_trap.${m1}", "SNMPトラップ.${m1}", m2, res)
            }
        }
        res['snmp_trap'] = config
        test_item.results(res)
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
        test_item.verify_text_search('sestatus', se_status['sestatus'])
    }

    def keyboard(session, test_item) {
        def lines = exec('keyboard') {
            def command = """\
            |if [ -f /etc/sysconfig/keyboard ]; then
            |    cat /etc/sysconfig/keyboard > ${work_dir}/keyboard
            |elif [ -f /etc/vconsole.conf ]; then
            |    cat /etc/vconsole.conf > ${work_dir}/keyboard
            |fi
            """.stripMargin()
            session.execute command
            session.get from: "${work_dir}/keyboard", into: local_dir
            new File("${local_dir}/keyboard").text
        }
        lines.eachLine {
            ( it =~ /^(LAYOUT|KEYMAP)="(.+)"$/).each {m0,m1,m2->
                test_item.results(m2)
            }
        }
    }


    def vmwaretool_timesync(session, test_item) {
        def lines = exec('vmwaretool_timesync') {
            def command = """\
            |LANG=c /usr/bin/vmware-toolbox-cmd timesync status 2>/dev/null
            |if [ \$? == 127 ]; then
            |   echo 'Not found'
            |fi
            """.stripMargin()
            run_ssh_command(session, command, 'vmwaretool_timesync')
        }
        test_item.results(lines)
    }

    def vmware_scsi_timeout(session, test_item) {
        def lines = exec('vmware_scsi_timeout') {
            def command = """\
            |cat /etc/udev/rules.d/99-vmware-scsi-udev.rules 2>/dev/null
            |if [ \$? != 0 ]; then
            |   echo 'Not found'
            |fi
            """.stripMargin()
            run_ssh_command(session, command, 'vmware_scsi_timeout')
        }
        // println lines
        def result = ''
        lines.eachLine {
            (it =~/^([^#].+?)$/).each {m0, m1 ->
                result += it
            }
        }
        test_item.results(result)
    }

    def language(session, test_item) {
        def lines = exec('language') {
            run_ssh_command(session, 'cat /proc/cmdline', 'language')
        }
        def lang = 'NotConfigured'
        lines.eachLine {
            def params = it.split(/\s/)
            params.each { param ->
                ( param =~ /LANG=(.+)$/).each {m0,m1->
                    lang = m1
                }
            }
        }
        test_item.results(lang)
    }

    def timezone(session, test_item) {
        def lines = exec('timezone') {
            def command = """\
            |if [ -x /bin/timedatectl ]; then
            |    /bin/timedatectl > ${work_dir}/timezone
            |elif [ -f /etc/sysconfig/clock ]; then
            |    cat /etc/sysconfig/clock > ${work_dir}/timezone
            |fi
            """.stripMargin()
            session.execute command
            session.get from: "${work_dir}/timezone", into: local_dir
            new File("${local_dir}/timezone").text
        }
        lines.eachLine {
            ( it =~ /Time zone: (.+)$/).each {m0,m1->
                test_item.results(m1)
            }
            ( it =~ /ZONE="(.+)"$/).each {m0,m1->
                test_item.results(m1)
            }
        }
    }

    def error_messages(session, test_item) {
        def lines = exec('error_messages') {
            run_ssh_sudo(session, 'egrep -i \'(error|warning|failed)\' /var/log/messages | head -100', 'error_messages')
        }
        def csv = []
        lines.eachLine {
            if (it.size() > 0) {
                csv << [it]
            }
        }
        def headers = ['message']
        test_item.devices(csv, headers)
        test_item.results((csv.size() == 0) ? 'Not found' : 'Message found')
    }

    def oracle_module(session, test_item) {
        def lines = exec('oracle_module') {
            def command = "ls /root/package/* >> ${work_dir}/oracle_module; echo \$?"
            try {
                def result = session.executeSudo command, pty: true, timeoutSec: timeout
                log.debug result
                session.get from: "${work_dir}/oracle_module", into: local_dir
                new File("${local_dir}/oracle_module").text
            } catch (Exception e) {
                log.info "[sudo] Error ${command}" + e
            }
        }
        def isok = false
        if (lines) {
        def n_requiements = 0
            def requiements = [:]
            ['compat-libcap1','compat-libstdc++-33','libstdc++-devel'].each {
                requiements[it] = 1
            }
            lines.each {
                if (requiements[packagename])
                    n_requiements ++
            }
            isok = (requiements.size() == n_requiements)
        }
        test_item.results((isok) ? 'OK' : 'NG')
    }

    def vncserver(session, test_item) {
        def isRHEL7 = session.execute('test -f /usr/bin/systemctl ; echo $?')
        if (isRHEL7 == '0') {
            def lines = exec('vncserver') {
                run_ssh_command(session, '/usr/bin/systemctl status vncserver', 'vncserver')
            }
            def vncserver = 'inactive'
            lines.eachLine {
                ( it =~ /Active: (.+?)\s/).each {m0,m1->
                     vncserver = m1
                }
            }
            test_item.results(vncserver)
        } else {
            def lines = exec('vncserver') {
                run_ssh_command(session, '/sbin/chkconfig --list|grep vncserver', 'vncserver')
            }
            def vncserver = 'off'
            lines.eachLine {
                ( it =~ /\s+3:(.+?)\s+4:(.+?)\s+5:(.+?)\s+/).each {m0,m1,m2,m3->
                    if (m1 == 'on' && m2 == 'on' && m3 == 'on') {
                        vncserver = 'on'
                    }
                }
            }
            test_item.results(vncserver)
        }
    }

}

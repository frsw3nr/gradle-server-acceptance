package InfraTestSpec

import javax.xml.bind.*
import static groovy.json.JsonOutput.*
import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
// import org.hidetake.groovy.ssh.Ssh
// import ch.ethz.ssh2.Connection
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.Unix.*
import jp.co.toshiba.ITInfra.acceptance.*
import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

@Slf4j
@InheritConstructors
class SolarisSpec extends InfraTestSpec {

    static String prompt = '[%|\$|#] \$'
    String ip
    String os_user
    String os_password
    String work_dir
    Boolean use_telnet
    int    timeout = 60

    def init() {
        super.init()
        def test_target = test_platform?.test_target

        this.ip          = test_platform.test_target.ip ?: 'unkown'
        def os_account   = test_platform.os_account
        this.os_user     = os_account['user'] ?: 'unkown'
//        this.os_password = os_account['password'] ?: 'unkown'
        this.os_password = test_target.specific_password ?: os_account['password'] ?: 'unkown'
        this.work_dir    = os_account['work_dir'] ?: '/tmp'
        this.use_telnet  = os_account['use_telnet'] ?: false
        this.timeout     = test_platform.timeout
        // this.prompt = '$ '
        
    }

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()
        // if (this.use_telnet) {
        //     return setup_exec_telnet(test_items)
        // }

//        def con = (this.use_telnet) ? new TelnetSession(this) : new SshSessionCommand(this)
        def con = (this.use_telnet) ? new TelnetSession(this) : new SshSession(this)
        def result
        // println "${this.ip}, ${this.os_user}, ${this.os_password}, ${this.use_telnet}"
        if (!dry_run) {
            con.init_session(this.ip, this.os_user, this.os_password)
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
                    // con.init_session(this.ip, this.os_user, this.os_password)
                }
            }
        }
        if (!dry_run) {
            con.close()
        }
        test_items.each { test_item ->
            if (test_item.test_id == 'logon_test') {
                _logon_test(test_item)
            }
        }
    }

    def _logon_test(TestItem test_item) {
        def results = [:]
        def result = 'Ignored'
        // if (test_server.os_account.logon_test && dry_run == false) {
        //     result = 'OK'
        //     test_server.os_account.logon_test.each { test_user->
        //         try {
        //             def con = new Connection(this.ip, 22)
        //             con.connect()
        //             def isok = con.authenticateWithPassword(test_user.user, test_user.password)
        //             results[test_user.user] = isok
        //             if (!isok)
        //                 result = 'NG'
        //         } catch (Exception e) {
        //             result = 'NG'
        //             log.error "[SSH Test] faild logon '${test_user.user}', skip.\n" + e
        //             results[test_user.user] = false
        //         }
        //     }
        // }
        // results['logon_test'] = result
        // test_item.results(results.toString())
    }

    def finish() {
        super.finish()
    }

    def hostname(session, test_item) {
        def lines = exec('hostname') {
            session.run_command('uname -n', 'hostname')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def hostname_fqdn(session, test_item) {
        def lines = exec('hostname_fqdn') {
            def command = '''\
            |(
            |awk \'/^domain/ {print \$2}\' /etc/resolv.conf 2>/dev/null
            |if [ \$? != 0 ]; then
            |   echo 'Not Found'
            |fi
            |)'''.stripMargin()
            session.run_command(command, 'hostname_fqdn')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        def info = (lines.size() > 0) ? lines : '[NotConfigured]'
        test_item.results(info)
    }

    def kernel(session, test_item) {
        def lines = exec('kernel') {
            session.run_command(' uname -X', 'kernel')
        }
        def info = [:]
        lines.eachLine {
            (it =~ /^(System|Release|KernelID|Machine) = (.+?)$/).each {m0,m1,m2->
                info[m1] = m2
            }
        }
        info['System'] += info['Release']
        info['kernel'] = "${info['System']} [${info['KernelID']}]"
        info['Release'] = "'" + info['Release'] + "'"
        test_item.results(info)

        test_item.verify_text_search('System', info['System'])
        test_item.verify_text_search('Release', info['Release'])
    }

    def cpu(session, test_item) {
        def lines = exec('cpu') {
            session.run_command('kstat -p cpu_info', 'cpu')
        }

        def cpuinfo    = [:].withDefault{0}
        def real_cpu   = [:].withDefault{0}
        def core_cpu   = [:].withDefault{0}
        def cpu_number = 0
        def cpu_number2 = 0
        def core_number = 0
        cpuinfo['cpu_core'] = ''
        lines.eachLine {
            (it =~ /ncpu_per_chip\s+(.+)$/).each {m0,m1->
                cpu_number += Integer.decode(m1)
            }
            (it =~ /chip_id\s+(.+)$/).each {m0,m1->
                real_cpu[m1] = true
            }
            (it =~ /core_id\s+(.+)$/).each {m0,m1->
                core_cpu[m1] = true
            }
            (it =~ /ncore_per_chip\s+(.+)$/).each {m0,m1->
                core_number += Integer.decode(m1)
            }
            (it =~ /brand\s+(.+)$/).each {m0,m1->
                cpuinfo["model_name"] = m1
                cpu_number2 ++
            }
            (it =~ /clock_MHz\s+(.+)/).each {m0,m1->
                cpuinfo["mhz"] = m1
            }
        }
        cpuinfo["cpu_total"] = (cpu_number > 0) ? cpu_number : cpu_number2
        cpuinfo["cpu_core"] = (core_number > 0) ? core_number : core_cpu.size()
        cpuinfo["cpu_real"] = real_cpu.size()
        cpuinfo["cpu"] = "${cpuinfo["model_name"]} ${cpuinfo["cpu_core"]} core"
        test_item.results(cpuinfo)
        test_item.verify_number_equal('cpu_real',  cpuinfo['cpu_real'])
        test_item.verify_number_equal('cpu_core',  cpuinfo['cpu_core'])
        test_item.verify_number_equal('cpu_total', cpuinfo['cpu_total'])
    }

    def psrinfo(session, test_item) {
        def lines = exec('psrinfo') {
            session.run_command('psrinfo', 'psrinfo')
        }
        def total_count = 0
        def psrinfos = [:].withDefault{0}
        lines.eachLine {
            ( it =~ /^\s*(\d+?)\s+(\w.+?)\s/).each {m0, m1, m2->
                psrinfos[m2] ++
                total_count ++
            }
        }
        def res = [:]
        psrinfos.each { status, count ->
            def metric = "psrinfo.${status}"
            add_new_metric(metric, "[プロセッサ状態] ${status}", psrinfos[status],
                           res)
        }
        res['psrinfo'] = total_count
        // add_new_metric("psrinfo.total", "[プロセッサ状態] Total", total_count, res)
        test_item.results(res)
    }

    def machineid(session, test_item) {
        def lines = exec('machineid') {
            session.run_command('hostid', 'machineid')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def memory(session, test_item) {
        def lines = exec('memory') {
            session.run_command('/usr/sbin/prtconf |grep Memory', 'memory')
        }
        double memory = 0
        lines.eachLine {
            // println it
            (it =~ /(\d+)/).each {m0,m1->
                memory += NumberUtils.toDouble(m1) / 1024
                // Integer.decode(m1)
            }
        }
        // println String.format("%1.1f", memory)
        test_item.results(String.format("%1.1f", memory))
        test_item.verify_number_equal('memory', memory, 0.1)
    }

    def swap(session, test_item) {
        def lines = exec('swap') {
            session.run_command('/usr/sbin/swap -s', 'swap')
        }
        def headers = ['alloc', 'reserve', 'used', 'available'] as Queue
        def infos = [:]
        lines.eachLine {
            def columns = it.split(/\s+/)
            if (columns.size() > 0) {
                columns.each { column ->
                    (column=~/(\d+)k/).each { m0,m1 ->
                        def swap_mb = (Integer.decode(m1) / 1024) as Integer
                        infos['swap.' + headers.poll()] = swap_mb
                    }
                }
            }
        }
        infos['swap'] = "${infos['swap.alloc']} / ${infos['swap.reserve']} / ${infos['swap.available']}"
        test_item.results(infos)
    }

    def network(session, test_item) {
        def lines = exec('network') {
            session.run_command('/usr/sbin/ifconfig -a', 'network')
        }
        def network = [:].withDefault{[:]}
        def device = ''
        def hw_address = []
        def device_ip = [:]
        def net_subnet = [:]
        lines.eachLine {
            // e1000g0: flags=1000843<UP,BROADCAST,RUNNING,MULTICAST,IPv4> mtu 1500 index 2
            //         inet 192.168.10.3 netmask ffffff00 broadcast 192.168.10.255
            (it =~  /^(.+?): (.+)<(.+)> (.+)$/).each { m0,m1,m2,m3,m4->
                device = m1
                if (m1 == 'lo0') {
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
            }
            (it =~ /netmask\s+(.+?)[\s|]/).each {m0, m1->
                if (m1 != '0') {
                    try {
                        def subnet = InetAddress.getByAddress(DatatypeConverter.parseHexBinary(m1));
                        net_subnet[device] = "${subnet.getHostAddress()}"
                        network[device]['subnet'] = net_subnet[device]
                        // SubnetInfo subnet = new SubnetUtils(m1).getInfo()
                        // network[device]['subnet'] = subnet.getNetmask()
                        // net_subnet[device] = network[device]['subnet']
                    } catch (IllegalArgumentException e) {
                        log.error "[SolarisTest] subnet convert : '$m1', Skip.\n" + e
                    }
                }
            }

            // ether 8:0:20:0:0:1
            (it =~ /ether\s+(.*?)\s*/).each {m0, m1->
                network[device]['mac'] = m1
                hw_address.add(m1)
            }
        }
        // mtu:1500, qdisc:noqueue, state:DOWN, ip:172.17.0.1/16
        def csv = []
        def infos = [:].withDefault{[:]}
        def res = [:]
        def row = 0
        network.find { dev, items ->
            if (items?.ip  =~ /(127\.0\.0\.1|0\.0\.0\.0)/)
                return
            def columns = [dev]
            ['ip', 'mtu', 'state', 'mac', 'subnet'].each {
                def value = items[it] ?: 'NaN'
                columns.add(value)
                if (it =~ /^(ip|subnet|mtu)$/) {
                    infos[dev][it] = value
                }
            }
            def ip_address = infos[dev]['ip']
            def mtu        = infos[dev]['mtu']
            def subnet     = infos[dev]['subnet']
            if (ip_address) {
                row ++
                test_item.lookuped_port_list(ip_address, dev)
                device_ip[dev] = ip_address
                add_new_metric("network.dev.${row}",    "[${row}] デバイス",  dev, res)
                add_new_metric("network.ip.${row}",     "[${row}] IP",  ip_address, res)
                add_new_metric("network.subnet.${row}", "[${row}] サブネット", subnet, res)
                add_new_metric("network.mtu.${row}",    "[${row}] MTU",  mtu, res)
            }
            csv << columns
        }
        def headers = ['device', 'ip', 'mtu', 'state', 'mac', 'subnet']
        test_item.devices(csv, headers)
        res['network'] = "$device_ip"
        test_item.results(res)
        // test_item.results(['network': "$infos", 'net_ip': "$device_ip", 'net_subnet': "$net_subnet"])
        // test_item.verify_text_search_map('network', device_ip)
        test_item.verify_text_search_list('net_ip', device_ip)
    }

   def ipadm(session, test_item) {
        def lines = exec('ipadm') {
//            session.run_command('ipadm', 'ipadm')
            session.run_command('ipadm', 'ipadm')
        }
        def total_count = 0
        def ipadms = [:].withDefault{[:]}
        lines.eachLine {
            ( it =~ /^\s*(\w.+?)\s+(\w.+?)\s+(\w.+?)\s.+\s([\d|\.]+\/\d+)$/).each {
                m0, device, mode, status, ip->
                ipadms[device]['mode']   = mode
                ipadms[device]['status'] = status
                ipadms[device]['ip']     = ip
                (ip =~/^(.+?)\/(\d+)$/).each { n0, ip_address, subnet ->
                    if (ip_address != '127.0.0.1') {
                        test_item.lookuped_port_list(ip_address, device)
                    }
                }
            }
        }
        // println lines
        def res = [:]
        def ip_count = 0
        ipadms.each { device, ipadm ->
            def metric = "ipadm.${device}"
            add_new_metric("${metric}.mode",   "[${device}] モード", ipadm['mode'], res)
            add_new_metric("${metric}.status", "[${device}] ステータス", ipadm['status'], res)
            add_new_metric("${metric}.ip",     "[${device}] IP", ipadm['ip'], res)
            ip_count ++
        }
        def status = (ip_count == 0) ? 'Not Found' : "${ip_count} ips"
        res['ipadm'] = status

        test_item.results(res)
    }

    def net_route(session, test_item) {
        def lines = exec('net_route') {
            session.run_command('/usr/sbin/route -v -n get default', 'net_route')
        }
        def net_route  = [:]
        def interfaces = []
        // gateway: 192.168.10.254
        // interface: e1000g0 index 2 address 00 0c 29 6f 38 cf
        // flags: <UP,GATEWAY,DONE,STATIC>
        def res = [:]
        def interface_number = 1
        lines.eachLine {
            (it =~ /gateway: (.+)$/).each {m0,m1->
                add_new_metric("net_route.default_gateway", "デフォルトGW", m1, res)
            }
            (it =~ /interface: .+ address (.+?)\s*$/).each {m0,m1->
                def mac = trim(m1)
                interfaces << mac
                add_new_metric("net_route.default_gateway.MAC${interface_number}", 
                               "デフォルトGW MAC${interface_number}", 
                               mac, res)
                interface_number ++
            }
        }
        test_item.results(res)
        test_item.make_summary_text('net_route.default_gateway':'Default')
        test_item.verify_text_search_list('net_route', net_route)
    }

    def ndd(session, test_item) {
        def lines = exec('ndd') {
            session.run_command('/usr/sbin/ndd -get /dev/tcp tcp_rexmit_interval_max tcp_ip_abort_interval tcp_keepalive_interval', 'ndd')
        }
        def res = [:]
        def params = lines.split(/(\r|\n|\s)+/)
        def results = (params.size() == 3) ? params : [0, 0, 0]
        add_new_metric("ndd.tcp_rexmit_interval_max", 
                       "TCPパラメータ tcp_rexmit_interval_max", 
                       results[0], res)
        add_new_metric("ndd.tcp_ip_abort_interval", 
                       "TCPパラメータ tcp_ip_abort_interval", 
                       results[1], res)
        add_new_metric("ndd.tcp_keepalive_interval", 
                       "TCPパラメータ tcp_keepalive_interval", 
                       results[2], res)

        test_item.results(res)
    }

    def trim(str){
        str.replaceAll(/\A[\s]+/,"").replaceAll(/[\s]+\z/,"")
    }

    def disk(session, test_item) {
        def lines = exec('disk') {
            session.run_command('/usr/sbin/prtpicl -v', 'disk')
        }
        def disks = [:].withDefault{[:]}
        def disk_seq = 0
        def disk_indent = 0
        def disk_end = false
        lines.eachLine {
            (it =~/(\s{10}\s+)disk /).each { m0, m1 ->
                disk_indent = m1.size()
                disk_seq ++
            }
            if (disk_seq > 0 && disk_end == false) {
                def indent = 0
                (it =~ /(\s+)/).find { m0, m1 ->
                    indent = m1.size()
                    if (indent < disk_indent)
                        disk_end = true
                    return true
                }
                (it =~ /inquiry-vendor-id\s(.+)/).each {m0,m1->
                    disks[disk_seq]['vendor-id'] = trim(m1)
                }
                (it =~ /inquiry-product-id\s(.+)$/).each {m0,m1->
                    disks[disk_seq]['product-id'] = trim(m1)
                }
                (it =~ /inquiry-serial-no\s(.+)$/).each {m0,m1->
                    disks[disk_seq]['serial-no'] = trim(m1)
                }
                // :devfs-path    /scsi_vhci/disk@g50000394083214e0 
                (it =~ /devid\s+(.+)$/).each {m0,m1->
                    disks[disk_seq]['devid'] = trim(m1)
                }
            }
        }
        // println prettyPrint(toJson(disks))

        // mtu:1500, qdisc:noqueue, state:DOWN, ip:172.17.0.1/16
        def csv = []
        def infos = [:]
        def res = [:]
        disks.each { device_id, items ->
            def columns = [device_id]
            ['vendor-id':'ベンダー', 'product-id':'モデルNo', 
             'devid':'デバイスID', 'serial-no':'S/N'].each { header,label ->
                def value = items[header] ?: 'NaN'
                columns.add(value)
                if (header == 'product-id') {
                    infos[device_id] = value
                }
                if (header == 'serial-no') {
                    value = "'${value}'"
                }
                add_new_metric("disk.${header}.${device_id}", 
                               "[${device_id}] ${label}",
                               value, res)
            }
            csv << columns
        }
        def headers = ['#', 'vendor', 'product', 'devid', 'serial']
        test_item.devices(csv, headers)
        res['disk'] = "${infos}"

        test_item.results(res)
    }

    def metastat(session, test_item) {
        def lines = exec('metastat') {
            session.run_command('/usr/sbin/metastat', 'metastat')
        }
        def infos = 'NotConfigured'
        if (lines.size() > 0) {
            infos = '[ToDo] Parse metastat'
            // println "${infos} : ${lines}"
        }
        test_item.results(infos)
    }

    def filesystem(session, test_item) {
        def lines = exec('filesystem') {
            session.run_command('df -ha', 'filesystem')
        }

        // Filesystem            Size  Used Avail Use% Mounted on
        // /dev/mapper/vg_ostrich-lv_root
        //                        26G   25G  184M 100% /
        // proc                     0     0     0    - /proc
        // sysfs                    0     0     0    - /sys
        // devpts                   0     0     0    - /dev/pts
        // tmpfs                 939M     0  939M   0% /dev/shm
        // /dev/sda1             477M   69M  383M  16% /boot
        // none                     0     0     0    - /proc/sys/fs/binfmt_misc
        def csv = []
        def res = [:]
        def infos = [:]
        lines.eachLine {
            (it =~  /\s+(\d.+)$/).each { m0,m1->
                def columns = m1.split(/\s+/)
                if (columns.size() == 5) {
                    def size  = columns[0]
                    def mount = columns[4]
                    (size =~ /^[1-9]/).each { row ->
                        // res['filesystem.' + mount] = size
                        if (!(mount =~ /^\/(etc|var|platform|system)\//)) {
                            infos[mount] = size
                            add_new_metric("filesystem.capacity.${mount}", 
                                           "ディスク容量 ${mount}", size, res)
                        }
                        csv << columns
                    }
                }
            }
        }
        def headers = ['size', 'used', 'avail', 'use%', 'mountpoint']
        test_item.devices(csv, headers)
        res['filesystem'] = "${infos}"
        test_item.results(res)
        test_item.verify_text_search_map('filesystem', infos)
    }

    def zpool(session, test_item) {
        def lines = exec('zpool') {
            session.run_command('/usr/sbin/zpool status', 'zpool')
        }

        def csvs = []
        def filesystems = [:]
        def state = 'unkown'
        def config_phase = false
        lines.eachLine {
            (it =~  / state: (.+)/).each { m0,m1->
                state = m1
            }
            (it =~  /config:/).each { 
                config_phase = true
            }
            (it =~  /errors:/).each { 
                config_phase = false
            }
            if (config_phase) {
                def csv = it.split(/\s+/)
                // println csv.size()
                if (csv.size() > 2 && csv[1] != 'NAME') {
                    csv[0] = csvs.size() + 1
                    // println csv
                    csvs << csv
                    // println csvs
                }
            }
        }
        def headers = ['#', 'name', 'state', 'read', 'write', 'cksum']
        test_item.devices(csvs, headers)
        test_item.results(state)
    }

    // NAME    SIZE  ALLOC   FREE  CAP  DEDUP  HEALTH  ALTROOT
    // dppool  556G  64.0G   492G  11%  1.00x  ONLINE  -
    // rpool   556G  66.9G   489G  12%  1.00x  ONLINE  -
    // swpool  556G   530G  26.0G  95%  1.00x  ONLINE  -
    def zpool_list(session, test_item) {
        def lines = exec('zpool_list') {
            session.run_command('/usr/sbin/zpool list', 'zpool_list')
        }
        def csvs = []
        def filesystems = [:]
        def state = 'unkown'
        def config_phase = false
        def row = 0
        lines.eachLine {
            row ++
            def csv = it.split(/\s+/)
            if (csv.size() == 8 && csv[0] != 'NAME') {
                // csv[0] = csvs.size() + 1
                filesystems[csv[0]] = csv[4]
                csvs << csv
                // println csvs
            }
        }
        def headers = ['name', 'size', 'alloc', 'free', 'cap', 'dedup', 'health', 'altroot']
        test_item.devices(csvs, headers)
        test_item.results("${filesystems}")
    }

    def patches(session, test_item) {
        def lines = exec('patches') {
            session.run_command('ls /var/sadm/patch 2>/dev/null', 'patches')
        }

        def csvs = []
        lines.eachLine {
            csvs << [it]
        }
        def headers = ['id']
        test_item.devices(csvs, headers)
        // println csvs
        def state = (csvs.size() > 0) ? "${csvs.size()} patches" : 'Not found'
        test_item.results(state)
    }

    def solaris11_build(session, test_item) {
        def lines = exec('solaris11_build') {
            session.run_command(
                            'sh -c "LANG=C; /usr/bin/pkg info entire"', 
                            'solaris11_build')
        }

        def csvs = []
        def info = ['solaris11_build' : 'Not found']
        lines.eachLine {
            (it =~  /Build Release:\s+(.+)/).each { m0,m1->
                info['solaris11_build'] = "'${m1}'"
            }
            (it =~  /Version:\s+(.+)/).each { m0,m1->
                info['solaris11_build.version'] = m1
            }
            (it =~  /Summary:\s+(.+)/).each { m0,m1->
                info['solaris11_build.sru'] = m1
            }
            csvs << [it]
        }
        test_item.results(info)
    }

    def virturization(session, test_item) {
        def lines = exec('virturization') {
            session.run_command('/usr/bin/zonename', 'virturization')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        def virturization = (lines.size() > 0) ? lines : 'Unkown'
        test_item.results(virturization)
    }

    def packages(session, test_item) {
        def lines = exec('packages') {
            session.run_command("/usr/bin/pkginfo -l", 'packages')
        }
        def pkginst
        def csv = []
        def row = []
        def package_infos = [:]
        def versions = [:]
        lines.eachLine {
            (it =~ /(PKGINST|NAME|CATEGORY|ARCH|VERSION|VENDOR|INSTDATE):\s+(.+)$/).each {m0,m1,m2->
                row << m2
                if (m1 == 'PKGINST') {
                    pkginst = m2
                }
                if (m1 == 'VERSION') {
                    package_infos['packages.' + pkginst] = m2
                    versions[pkginst] = m2
                }
                if (m1 == 'INSTDATE') {
                    csv << row
                    row = []
                }
            }
        }
        def headers = ['pkginst', 'name', 'category', 'arch', 'version', 'vendor', 'instdate']
        test_item.devices(csv, headers)
        package_infos['packages'] = "${csv.size()} packages"
        def package_list = test_item.target_info('packages')
        if (package_list) {
            def template_id = this.test_platform.test_target.template_id
            package_infos['packages.requirements'] = "${package_list.keySet()}"
            def verify = true
            package_list.each { package_name, value ->
                def test_id = "packages.${template_id}.${package_name}"
                def version = versions[package_name] ?: 'Not Found'
                if (version == 'Not Found') {
                    verify = false
                }
                add_new_metric(test_id, "${template_id}.${package_name}", "'${version}'", package_infos)
            }
            test_item.verify(verify)
        }
        versions.sort().each { package_name, version ->
            if (package_list?."${package_name}") {
                return
            }
            def test_id = "packages.Etc.${package_name}"
            add_new_metric(test_id, package_name, "'${version}'", package_infos)
        }
        test_item.results(package_infos)
        test_item.verify_text_search_list('packages', package_infos)
    }

    def user(session, test_item) {
        def lines = exec('user') {
            session.run_command("cat /etc/passwd", 'user')
        }
        def group_lines = exec('group') {
            session.run_command("cat /etc/group", 'group')
        }
        def groups = [:].withDefault{0}
        // root:x:0:
        group_lines.eachLine {
            ( it =~ /^(.+?):(.*?):(\d+)/).each {m0,m1,m2,m3->
                groups[m3] = m1
            }
        }
        def csv = []
        def general_users = [:]
        def user_count = 0
        def users = [:].withDefault{'unkown'}
        def homes = [:]
        lines.eachLine {
            def arr = it.split(/:/)
            if (arr.size() == 7) {
                def username = arr[0]
                def user_id  = arr[2]
                def group_id = arr[3]
                def home     = arr[5]
                def shell    = arr[6]
                def group    = groups[group_id] ?: 'Unkown'

                csv << [username, user_id, group_id, group, home, shell]
                user_count ++
                homes[username] = home
                // users['user.' + username] = 'OK'
                (shell =~ /sh$/).each {
                    general_users[username] = 'OK'
                    add_new_metric("user.${username}.id",    "[${username}] ID",          "'${user_id}'", users)
                    add_new_metric("user.${username}.home",  "[${username}] ホーム",    home, users)
                    add_new_metric("user.${username}.group", "[${username}] グループ", group, users)
                    add_new_metric("user.${username}.shell", "[${username}] シェル",   shell, users)
                }

            }
        }
        def headers = ['UserName', 'UserID', 'GroupID', 'Group', 'Home', 'Shell']
        test_item.devices(csv, headers)
        users['user'] = general_users.keySet().toString()
        // users['user'] = homes.toString()
        test_item.results(users)
        test_item.verify_text_search_list('user', users)
    }

    def service(session, test_item) {
        def lines = exec('service') {
            // TODO: Avoid grep online filter
            session.run_command('sh -c "LANG=C /usr/bin/svcs -a"', 'service')
        }
        def services = [:].withDefault{'unkown'}
        def statuses = [:]
        def service_names = [:].withDefault{'unkown'}
        def csv = []
        def service_count = 0
        lines.eachLine {
            ( it =~ /^(.+?)\s.*svc:(.+?):/).each {m0,m1,m2->
                def status = m1
                def service_name = m2
                statuses[service_name] = status
                def columns = [service_name, status]
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
    }

   def zoneadm(session, test_item) {
        def lines = exec('zoneadm') {
            session.run_command('zoneadm list -vc', 'zoneadm')
        }
        def total_count = 0
        def zoneadms = [:].withDefault{[:]}
        lines.eachLine {
            ( it =~ /^\s*(\d+?)\s+(\w.+?)\s+(\w.+?)\s+(\/.+?)\s+(\w.+?)\s+(\w.+?)$/).each {
                m0, id, name, status, path, brand, ip->
                // println "ZONE:$id, $name, $status, $path, $brand, $ip"
                zoneadms[name]['status'] = status
                zoneadms[name]['path']   = path
                zoneadms[name]['brand']  = brand
                zoneadms[name]['ip']     = ip
            }
        }
        def res = [:]
        def zone_count = 0
        zoneadms.each { zone_name, zoneadm ->
            def metric = "zoneadm.${zone_name}"
            add_new_metric("${metric}.path",  "[${zone_name}] パス", zoneadm['path'], res)
            add_new_metric("${metric}.brand", "[${zone_name}] ブランド", zoneadm['brand'], res)
            add_new_metric("${metric}.ip",    "[${zone_name}] IPインスタンス", zoneadm['ip'], res)
            zone_count ++
        }
        def status = (zone_count == 0) ? 'Not Found' : "${zone_count} zones"
        res['zoneadm'] = status
        test_item.results(res)
    }

   def poolstat(session, test_item) {
        def lines = exec('poolstat') {
            session.run_command('poolstat -r all', 'poolstat')
        }
        def total_count = 0
        def poolstats = [:].withDefault{[:]}
        lines.eachLine {
            // id pool                 type rid rset                  min  max size used load
            // 1 pool_db              pset   1 pset_db               144  144  144 0.00 0.42
            ( it =~ /^\s*(\d+?)\s+(\w.+?)\s+(\w.+?)\s+(\d+?)\s+(\w.+?)\s+(\d+?)\s+(\d+?)\s+(\d+?)\s+(.+?)\s+(.+?)$/).each {
                m0, id, pool, type, rid, rset, cpu_min, cpu_max, cpu_size, used, load->
                poolstats[pool]['type'] = type
                poolstats[pool]['rset'] = rset
                poolstats[pool]['min']  = cpu_min
                poolstats[pool]['max']  = cpu_max
                poolstats[pool]['size'] = cpu_size
            }
        }
        def res = [:]
        def pool_count = 0
        poolstats.each { pool_name, poolstat ->
            def metric = "poolstat.${pool_name}"
            add_new_metric("${metric}.type", "[${pool_name}] プールタイプ", poolstat['type'], res)
            add_new_metric("${metric}.rset", "[${pool_name}] プールリソース", poolstat['rset'], res)
            add_new_metric("${metric}.min",  "[${pool_name}] CPU最小", poolstat['min'], res)
            add_new_metric("${metric}.max",  "[${pool_name}] CPU最大", poolstat['max'], res)
            add_new_metric("${metric}.size", "[${pool_name}] CPUサイズ", poolstat['size'], res)
            pool_count ++
        }
        def status = (pool_count == 0) ? 'Not Found' : "${pool_count} pools"
        res['poolstat'] = status
        test_item.results(res)
    }

   def system_etc(session, test_item) {
        def lines = exec('system_etc') {
            session.run_command('cat /etc/system', 'system_etc')
        }
        // println lines
        def total_count = 0
        def system_etcs = [:].withDefault{[:]}

        def res = [:]
        lines.eachLine {
            // *       To set a variable named 'debug' in the module named 'test_module'
            // *
            // *               set test_module:debug = 0x13

            // * Begin FJSVssf (do not edit)
            // set ftrace_atboot = 1
            // set kmem_flags = 0x100
            // set kmem_lite_maxalign = 8192

            ( it =~ /^\s*set\s+(\w.+?)\s*=\s*(\w.+?)$/).each { m0, name, value ->
                def metric = "system_etc.${name}"
                add_new_metric(metric, "[システム設定] ${name}", value, res)
            }
        }
        def status = (res.size() == 0) ? 'Not Found' : "${res.size()} parameters"
        res['system_etc'] = status
        test_item.results(res)
    }

    // def oracle(session, test_item) {
    //     def lines = exec('oracle') {
    //         def command = """\
    //         |ls -d /opt/oracle/app/product/*/*  >> ${work_dir}/oracle
    //         |ls -d /*/app/oracle/product/*/*    >> ${work_dir}/oracle
    //         """
    //         session.execute command.stripMargin()
    //         session.get from: "${work_dir}/oracle", into: local_dir
    //         new File("${local_dir}/oracle").text
    //     }
    //     def oracleinfo = 'Not Found'
    //     lines.eachLine {
    //         ( it =~ /\/product\/(.+?)\/(.+?)$/).each {m0,m1,m2->
    //             oracleinfo = m1
    //         }
    //     }
    //     test_item.results(oracleinfo)
    // }

    // def proxy_global(session, test_item) {
    //     def lines = exec('proxy_global') {
    //         session.run_command('grep proxy /etc/yum.conf', 'proxy_global')
    //     }
    //     lines = lines.replaceAll(/(\r|\n)/, "")
    //     test_item.results(lines)
    // }

    def resolve_conf(session, test_item) {
        def lines = exec('resolve_conf') {
            session.run_command('grep nameserver /etc/resolv.conf', 'resolve_conf')
        }
        def nameservers = [:]
        def res = [:]
        def nameserver_number = 1
        lines.eachLine {
            ( it =~ /^nameserver\s+(\w.+)$/).each {m0, dns->
                def name_server = "nameserver${nameserver_number}"
                add_new_metric("resolve_conf.${name_server}", 
                               name_server, dns, res)
                nameserver_number ++
            }
        }
        res['resolve_conf'] = (nameserver_number == 1) ? 'off' : 'on'
        test_item.results(res)
    }

//      global core file pattern:
// kernel zone core file pattern:
//        init core file pattern: /var/corefiles/core.%f.%p
//      global core file content: default
//        init core file content: default-shm-ism-dism-osm
//             global core dumps: disabled
//        kernel zone core dumps: disabled
//        per-process core dumps: enabled
//       global setid core dumps: disabled
//  per-process setid core dumps: disabled
//      global core dump logging: disabled

    def coreadm(session, test_item) {
        def lines = exec('coreadm') {
            session.run_command('coreadm', 'coreadm')
        }

        def core_file_patterns = [:]
        def core_file_contents = [:]
        def core_dumps = [:].withDefault{[]}
        lines.eachLine {
            ( it =~ /^\s*(\w.*?) core file pattern: (.*)$/).each {m0, m1, m2->
                core_file_patterns[m1] = "'${m2}'"
            }
            ( it =~ /^\s*(\w.*?) core file content: (.*)$/).each {m0, m1, m2->
                core_file_contents[m1] = "'${m2}'"
            }
            ( it =~ /^\s*(\w.*?) core dumps: (.*)$/).each {m0, m1, m2->
                core_dumps[m2] << m1
            }
            ( it =~ /^\s*global core dump logging: (.*)$/).each {m0, m1->
                core_dumps[m1] << 'logging'
            }
        }
        def res = [:]
        add_new_metric("coreadm.file_pattern", "[コアダンプ] ファイル名", 
                       "${core_file_patterns}", res)
        add_new_metric("coreadm.file_contents", "[コアダンプ] コンテンツ", 
                       "${core_file_contents}", res)
        add_new_metric("coreadm.mode", "[コアダンプ] 無効化設定", 
                       "${core_dumps['disabled']}", res)
        def enabled_core_dumps = core_dumps['enabled']
        res['coreadm'] = (enabled_core_dumps.size() == 0) ? 'AllDisable' : "$enabled_core_dumps"
        test_item.results(res)
    }

    def ntp(session, test_item) {
        def lines = exec('ntp') {
            session.run_command("egrep -e '^server' /etc/inet/ntp.conf", 'ntp')
        }
        def ntpservers = []
        def res = [:]
        lines.eachLine {
            ( it =~ /^server\s+(\w.+)$/).each {m0, ntp_server->
                add_new_metric("ntp.${ntp_server}", 
                               ntp_server, 'Enable', res)
                ntpservers.add(ntp_server)
            }
        }
        res['ntp'] = (ntpservers.size() == 0) ? 'off' : 'on'
        test_item.results(res)
    }

    def snmp_trap(session, test_item) {
        def lines = exec('snmp_trap') {
            session.run_command("egrep -e '^\\s*trapsink' /etc/snmp/snmpd.conf", 'snmp_trap')
        }
        def config = 'NotConfigured'
        def res = [:]
        def trapsink = []
        lines.eachLine {
            (it =~  /(trapsink|trapcommunity|trap2sink|informsink)\s+(.*)$/).each { m0, m1, m2 ->
                config = 'Configured'
                add_new_metric("snmp_trap.${m1}", "SNMPトラップ.${m1}", m2, res)
            }

            (it =~  /trapsink\s+(.*)$/).each { m0, trap_info ->
                config = 'Configured'
                trapsink << trap_info
            }
        }
        res['snmp_trap'] = config
        test_item.results(res)
    }

    def tnsnames(session, test_item) {
        def lines = exec('tnsnames') {
            if (server_info.containsKey('tnsnames')) {
                def tns_source = server_info.'tnsnames'
                println "TNS_SOURCE:$tns_source"
                session.run_command("cat ${tns_source}", 'tnsnames')
            }

        }
        // SOL80 =
        //   (DESCRIPTION =
        //     (ENABLE=BROKEN)
        //     (ADDRESS_LIST =
        def tns = [:]
        def tnsname = ''
        lines.eachLine {
            (it =~  /^(\w.+?)\s*=\s*$/).each { m0, m1 ->
                tnsname = m1
                tns[tnsname] = []
            }
            (it =~  /(\(ENABLE=BROKEN\))/).each { m0, m1 ->
                tns[tnsname] << m1
            }
        }

        def csv = []
        def info = [:]
        def summary = [:].withDefault{[]}
        tns.each { key, values ->
            if (values.isEmpty()) {
                summary["None"] << key
            } else {
                summary["${values}"] << key
            }
            add_new_metric("tnsnames.${key}", "TNS [${key}]", values, info)
            csv << [key, "${values}"]
        }

        def headers = ['tns', 'config']
        info['tnsnames'] = "${summary}"
        test_item.results(info)
        test_item.devices(csv, headers)
    }
}

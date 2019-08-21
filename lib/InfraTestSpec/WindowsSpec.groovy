package InfraTestSpec

import java.text.SimpleDateFormat
import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
import org.apache.commons.lang.math.NumberUtils
import static groovy.json.JsonOutput.*
import org.hidetake.groovy.ssh.Ssh
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
@InheritConstructors
class WindowsSpec extends WindowsSpecBase {
    def init() {
        super.init()
    }

    def finish() {
        super.finish()
    }

    def system(TestItem test_item) {
        run_script('Get-WmiObject -Class Win32_ComputerSystem') {
            def lines = exec('system') {
                new File("${local_dir}/system")
            }
            def systeminfo = [:]
            lines.eachLine {
                (it =~ /^(Domain|Manufacturer|Model|Name|PrimaryOwnerName)\s*:\s+(.+?)$/).each {m0,m1,m2->
                    systeminfo[m1] = m2
                }
            }
            test_item.results(systeminfo)
            test_item.make_summary_text('Model':'Model', 'PrimaryOwnerName' : 'Owner')
            test_item.exclude_compare('Name')
        }
    }

    def os_conf(TestItem test_item) {
        def command = '''\
            |Get-ItemProperty "HKLM:\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion" | `
            |    Format-List
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('os_conf') {
                new File("${local_dir}/os_conf")
            }
            def osinfo    = [:].withDefault{0}
            lines.eachLine {
                (it =~ /^CurrentVersion\s*:\s+(.+)$/).each {m0,m1->
                    osinfo['os_conf.version'] = "'${m1}'"
                }
                (it =~ /^InstallDate\s*:\s+(.+)$/).each {m0,m1->
                    def sec = Integer.parseInt(m1)
                    Date date = new Date(sec * 1000L)
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    osinfo['os_conf.install_date'] = sdf.format(date)
                }
                (it =~ /^CurrentBuild\s*:\s+(.+)$/).each {m0,m1->
                    osinfo['os_conf.build'] = "'${m1}'"
                }
                (it =~ /^ProductId\s*:\s+(.+)$/).each {m0,m1->
                    osinfo['os_conf.product_id'] = m1
                }
                (it =~ /^BuildLab\s*:\s+(.+)$/).each {m0,m1->
                    osinfo['os_conf'] = m1
                }
            }
            test_item.results(osinfo)
            test_item.make_summary_text('version' : 'Version', 'build' : 'Build')
        }
    }

    def os(TestItem test_item) {
        // def setting_os = test_item.target_info('ip2')
        // println "OS:${setting_os},${this.ip}"

        // test_item.lookuped_port_list(this.ip, 'IP')
        // test_item.results([os_caption:setting_os])


        def command = '''\
            |Get-WmiObject Win32_OperatingSystem | `
            |    Format-List Caption,CSDVersion,ProductType,OSArchitecture
            |'''.stripMargin()

        def product_types = ['1':'workstation','2':'domaincontroller', '3':'server']

        run_script(command) {
            def lines = exec('os') {
                new File("${local_dir}/os")
            }
            def osinfo    = [:].withDefault{0}
            osinfo['os_csd_version'] = ''
            lines.eachLine {
                (it =~ /^Caption\s*:\s+(.+)$/).each {m0,m1->
                    osinfo['os_caption'] = m1
                }
                (it =~ /^CSDVersion\s*:\s+(.+)$/).each {m0,m1->
                    osinfo['os_csd_version'] = m1
                }
                (it =~ /^ProductType\s*:\s+(.+)$/).each {m0,m1->
                    osinfo['os_product_type'] = product_types[m1] ?: 'unkown'
                }
                (it =~ /^OSArchitecture\s*:\s+(.+)$/).each {m0,m1->
                    osinfo['os_architecture'] = m1
                }
            }
            // def os_name = test_item.target_info('ip2')
            // println "OS:${os_name}"
            osinfo['os'] = "${osinfo['os_caption']} ${osinfo['os_architecture']}"
            test_item.results(osinfo)
            test_item.verify_text_search('os_caption', osinfo['os_caption'])
            test_item.verify_text_search('os_architecture', osinfo['os_architecture'])
        }
    }

    def driver(TestItem test_item) {
        run_script('Get-WmiObject Win32_PnPSignedDriver') {
            def lines = exec('driver') {
                new File("${local_dir}/driver")
            }
            def device_number = 0
            def driverinfo = [:].withDefault{[:]}
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    driverinfo[device_number][m1] = m2
                }
                if (it.size() == 0 && driverinfo[device_number].size() > 0)
                    device_number ++
            }
            device_number --
            def headers = ['DeviceClass', 'DeviceID', 'DeviceName', 'DriverDate',
                           'DriverProviderName', 'DriverVersion']
            def csv = []
            (0..device_number).each { row ->
                def columns = []
                headers.each { header ->
                    columns.add( driverinfo[row][header] ?: '')
                }
                csv << columns
            }
            test_item.devices(csv, headers)
            test_item.results("${device_number} drivers")
        }
    }

    def fips(TestItem test_item) {
        run_script('Get-Item "HKLM:System\\CurrentControlSet\\Control\\Lsa\\FIPSAlgorithmPolicy"') {
            def lines = exec('fips') {
                new File("${local_dir}/fips")
            }
            def fips_info = ''
            lines.eachLine {
                (it =~ /^FIPSAlgorithmPolicy\s+Enabled\s+: (.+?)\s+/).each {m0,m1->
                    fips_info = (m1 == '0') ? 'Disabled' : 'Enabled'
                }
            }
            test_item.results(fips_info)
        }
    }

    def virturalization(TestItem test_item) {
        run_script('Get-WmiObject -Class Win32_ComputerSystem | Select Model | FL') {
            def lines = exec('virturalization') {
                new File("${local_dir}/virturalization")
            }
            def config = 'NotVM'
            lines.eachLine {
                (it =~ /^Model\s*:\s+(.*Virtual.*?)$/).each {m0,m1->
                    config = m1
                }
            }
            test_item.results(config)
        }
    }

    def cpu(TestItem test_item) {
        //run_script('Get-WmiObject Win32_Processor') {
        run_script('Get-WmiObject -Class Win32_Processor | Format-List DeviceID, Name, MaxClockSpeed, SocketDesignation, NumberOfCores, NumberOfLogicalProcessors') {
            def lines = exec('cpu') {
                new File("${local_dir}/cpu")
            }
            def cpuinfo    = [:].withDefault{0}
            def cpu_number = 0
            def sockets    = [:]
            lines.eachLine {
                (it =~ /^DeviceID\s+:\s(.+)$/).each {m0, m1->
                    cpu_number += 1
                }
                (it =~ /^Name\s+:\s(.+)$/).each {m0, m1->
                    cpuinfo["model_name"] = m1
                }
                (it =~ /^NumberOfCores\s+:\s(.+)$/).each {m0, m1->
                    cpuinfo["cpu_core"] = m1
                }
                (it =~ /^NumberOfLogicalProcessors\s+:\s(.+)$/).each {m0, m1->
                    cpuinfo["cpu_total"] = m1
                }
                (it =~ /^MaxClockSpeed\s+:\s(.+)$/).each {m0, m1->
                    cpuinfo["mhz"] = m1
                }
                (it =~ /^SocketDesignation\s+:\s(.+)$/).each {m0, m1->
                    sockets[m1] = 1
                }
            }
            cpuinfo["cpu"] = [cpuinfo["model_name"], cpuinfo["cpu_total"]].join("/")
            cpuinfo["cpu_socket"] = sockets.size()
            test_item.results(cpuinfo)
            test_item.verify_number_equal('cpu_total', cpuinfo['cpu_total'])
        }
    }


    def memory(TestItem test_item) {
        def command = '''\
            |Get-WmiObject Win32_OperatingSystem | `
            |    select TotalVirtualMemorySize,TotalVisibleMemorySize, `
            |        FreePhysicalMemory,FreeVirtualMemory,FreeSpaceInPagingFiles
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('memory') {
                new File("${local_dir}/memory")
            }
            def meminfo    = [:].withDefault{0}
            lines.eachLine {
                (it =~ /^TotalVirtualMemorySize\s*:\s+(\d+)$/).each {m0,m1->
                    meminfo['virtual_memory'] = NumberUtils.toDouble(m1) / (1024 * 1024)
                }
                (it =~ /^TotalVisibleMemorySize\s*:\s+(\d+)$/).each {m0,m1->
                    meminfo['visible_memory'] = NumberUtils.toDouble(m1) / (1024 * 1024)
                }
                (it =~ /^FreePhysicalMemory\s*:\s+(\d+)$/).each {m0,m1->
                    meminfo['free_memory'] = NumberUtils.toDouble(m1) / (1024 * 1024)
                }
                (it =~ /^FreeVirtualMemory\s*:\s+(\d+)$/).each {m0,m1->
                    meminfo['free_virtual'] = NumberUtils.toDouble(m1) / (1024 * 1024)
                }
                (it =~ /^FreeSpaceInPagingFiles\s*:\s+(\d+)$/).each {m0,m1->
                    meminfo['free_space'] = NumberUtils.toDouble(m1) / (1024 * 1024)
                }
            }
            meminfo['memory'] = meminfo['visible_memory']
            test_item.results(meminfo)
            test_item.verify_number_equal('visible_memory',
                                          meminfo['visible_memory'],
                                          0.1)
        }
    }

    String parse_ip( String text ) {
        String address = null
        (text =~ /([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+)/).each {m0, ip ->
            address = ip
        }
        return address
    }

    def network(TestItem test_item) {
        def command = '''\
            |Get-WmiObject Win32_NetworkAdapterConfiguration | `
            | Where{$_.IpEnabled -Match "True"} | `
            | Select ServiceName, MacAddress, IPAddress, DefaultIPGateway, Description, IPSubnet | `
            | Format-List `
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('network') {
                new File("${local_dir}/network")
            }
            def device_number = 0
            def network_info  = [:].withDefault{[:]}
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    network_info[device_number][m1] = m2
                }
                if (it.size() == 0 && network_info[device_number].size() > 0)
                    device_number ++
            }
            device_number --
            def headers = ['ServiceName', 'MacAddress', 'IPAddress',
                           'DefaultIPGateway', 'Description', 'IPSubnet']
            def csv          = []
            // def ip_configs   = [:]
            // def gateways     = []
            // def subnets      = []

            def ip_addresses = []
            def res = [:]
            def exclude_compares = []
            (0..device_number).each { row ->
                def columns = []
                headers.each { header ->
                    columns.add( network_info[row][header] ?: '')
                }
                csv << columns
                def device     = network_info[row]['ServiceName']
                def ip_address = parse_ip(network_info[row]['IPAddress'])
                def gateway    = parse_ip(network_info[row]['DefaultIPGateway'])
                def subnet     = parse_ip(network_info[row]['IPSubnet'])
                // ip_configs[ip_address] = "${gateway},${subnet}"
                ip_addresses << ip_address
                // gateways     << gateway
                // subnets      << subnet

                if (ip_address && ip_address != '127.0.0.1') {
                    test_item.lookuped_port_list(ip_address, device)
                    add_new_metric("network.dev.${row}",     "[${row}] デバイス", device, res)
                    add_new_metric("network.ip.${row}",      "[${row}] IP", ip_address, res)
                    exclude_compares << "network.ip.${row}"
                    add_new_metric("network.subnet.${row}",  "[${row}] サブネット", subnet, res)
                    add_new_metric("network.gateway.${row}", "[${row}] ゲートウェイ", gateway, res)
                }

            }

            test_item.devices(csv, headers)
            // def infos = [
            //     'network'        : ip_configs.keySet().toString(),
            //     'network.ip'     : ip_addresses.toString(),
            //     'network.gw'     : gateways.toString(),
            //     'network.subnet' : subnets.toString(),
            // ]
            // test_item.results(infos)
            res['network'] = "${ip_addresses}"
            test_item.results(res)
            test_item.verify_text_search_list('network', ip_addresses)
            test_item.exclude_compare(exclude_compares)
            // test_item.verify_text_search_map('network', ip_configs)
        }
    }

    def nic_teaming(TestItem test_item) {
        run_script('Get-NetLbfoTeamNic') {
            def lines = exec('nic_teaming') {
                new File("${local_dir}/nic_teaming")
            }

            def devices = []
            def teaming = 'NotConfigured'
            lines.eachLine {
                (it =~ /^Name\s+:\s(.+)/).each {m0, m1->
                    devices << m1
                    teaming = 'Configured'
                }
            }
            def res = [:]
            if (devices) {
                add_new_metric("nic_teaming.device", "NICチーミング構成", "${devices}", res)
            }
            res['nic_teaming'] = teaming
            test_item.results(res)
        }
    }

    def nic_teaming_config(TestItem test_item) {
        run_script('Get-NetLbfoTeamNic') {
            def lines = exec('nic_teaming_config') {
                new File("${local_dir}/nic_teaming_config")
            }

            def teaming = 'NotConfigured'
            def alias = ''
            def res = [:]
            lines.eachLine {
                (it =~ /^Name\s+:\s(.+)/).each {m0, m1->
                    teaming = 'Configured'
                    alias = m1
                }
                (it =~ /^Members\s+:\s(.+)/).each {m0, m1->
                    add_new_metric("nic_teaming_config.${alias}.members", "チーミング[${alias}] メンバー", m1, res)
                }
                (it =~ /^TeamingMode\s+:\s(.+)/).each {m0, m1->
                    add_new_metric("nic_teaming_config.${alias}.mode", "チーミング[${alias}] モード", m1, res)
                }
                (it =~ /^LoadBalancingAlgorithm\s+:\s(.+)/).each {m0, m1->
                    add_new_metric("nic_teaming_config.${alias}.algorithm", "チーミング[${alias}] 負荷分散モード", m1, res)
                }
            }
            res['nic_teaming_config'] = teaming
            test_item.results(res)
        }
    }

    def network_profile(TestItem test_item) {
        def connectivitys = ['0' : 'Internet Disconnected', '1' : 'NoTraffic', '2' : 'Subnet', 
                             '3' : 'LocalNetwork', '4' : 'Internet']
        def net_categorys = ['0' : 'Public', '1' : 'Private', '2' : 'DomainAuthenticated']
        run_script('Get-NetConnectionProfile') {
            def lines = exec('network_profile') {
                new File("${local_dir}/network_profile")
            }
            def instance_number = 0
            def network_info = [:].withDefault{[:]}
            def network_categorys = [:]
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, item_name, value->
                    (item_name =~ /IP.+Connectivity/).each { temp ->
                        if (connectivitys[value]) {
                            value = connectivitys[value]
                        }
                    }
                    (item_name =~/NetworkCategory/).each { temp ->
                        if (net_categorys[value]) {
                            value = net_categorys[value]
                        }
                    }
                    network_info[instance_number][item_name] = value
                }
                if (it.size() == 0 && network_info[instance_number].size() > 0) {
                    instance_number ++
                }
            }
            def headers = ['Name','InterfaceAlias', 'InterfaceIndex', 'NetworkCategory',
                           'IPv4Connectivity','IPv6Connectivity']
            def csv = []
            def res = [:]
            (0..instance_number).each { row ->
                network_info[row].with {
                    if ( it.size() > 0 ) {
                        def columns = []
                        headers.each { header ->
                            columns.add( it[header] ?: '')
                        }
                        csv << columns
                        def device = it['InterfaceAlias']
                        add_new_metric("network_profile.Category.${device}", 
                                       "[${device}] カテゴリ", 
                                       it['NetworkCategory'], res)
                        add_new_metric("network_profile.IPv4.${device}", 
                                       "[${device}] IPv4", 
                                       it['IPv4Connectivity'], res)
                        add_new_metric("network_profile.IPv6.${device}", 
                                       "[${device}] IPv6", 
                                       it['IPv6Connectivity'], res)

                        network_categorys[device] = it['NetworkCategory']
                    }
                }
            }
            res['network_profile'] = "${network_categorys}"
            test_item.devices(csv, headers)
            test_item.results(res)
        }
    }

    def net_bind(TestItem test_item) {
        run_script('Get-NetAdapterBinding | FL') {
            def lines = exec('net_bind') {
                new File("${local_dir}/net_bind")
            }
            def instance_number = 0
            def bind_info = [:].withDefault{[:]}
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    bind_info[instance_number][m1] = m2
                }
                if (it.size() == 0 && bind_info[instance_number].size() > 0)
                    instance_number ++
            }
            instance_number --
            def headers = ['Name', 'ifDesc', 'Description', 'ComponentID', 'Enabled']

            def csv = []
            def bind_components = [:].withDefault{[:]}
            def infos = [:]
            (0..instance_number).each { row ->
                def columns = []
                headers.each { header ->
                    columns.add( bind_info[row][header] ?: '')
                }
                csv << columns
                def name         = bind_info[row]['Name']
                def if_desc      = bind_info[row]['ifDesc']
                (if_desc =~/(.+) #\d+/).each { m0, m1 ->
                    if_desc = m1
                }
                def component_id = bind_info[row]['ComponentID']
                def display_name = bind_info[row]['DisplayName']
                def enabled      = bind_info[row]['Enabled']
                add_new_metric("net_bind.${name}", name, if_desc, infos)
                add_new_metric("net_bind.${name}.${component_id}", "[${name}] ${display_name}", 
                               enabled, infos)
                if (enabled == 'True') {
                    bind_components[name][component_id] = 'True'
                }
            }
            // println bind_components.keySet().sort()
            infos['net_bind'] = "${bind_components.keySet().sort()}"
            test_item.devices(csv, headers)
            test_item.results(infos)
        }
    }

    def net_ip(TestItem test_item) {
        run_script('Get-NetIPInterface | FL') {
            def lines = exec('net_ip') {
                new File("${local_dir}/net_ip")
            }
            def instance_number = 0
            def ip_info = [:].withDefault{[:]}
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    ip_info[instance_number][m1] = m2
                }
                if (it.size() == 0 && ip_info[instance_number].size() > 0)
                    instance_number ++
            }
            instance_number --
            def headers = ['InterfaceAlias', 'AddressFamily', 'NlMtu(Bytes)', 'AutomaticMetric',
                           'InterfaceMetric', 'Dhcp', 'ConnectionState', 'PolicyStore']

            def csv = []
            def connect_if = [:]
            def infos = [:]
            (0..instance_number).each { row ->
                def columns = []
                headers.each { header ->
                    columns.add( ip_info[row][header] ?: '')
                }
                csv << columns
                def alias       = ip_info[row]['InterfaceAlias']
                def auto_metric = ip_info[row]['AutomaticMetric']
                def int_metric  = ip_info[row]['InterfaceMetric']
                def dhcp        = ip_info[row]['Dhcp']
                def status      = ip_info[row]['ConnectionState']
                (alias =~ /Ethernet/).each {
                    add_new_metric("net_ip.${alias}.auto_metric", "[${alias}] auto_metric", auto_metric, infos)
                    add_new_metric("net_ip.${alias}.int_metric",  "[${alias}] int_metric", int_metric, infos)
                    add_new_metric("net_ip.${alias}.dhcp",        "[${alias}] dhcp", dhcp, infos)
                    add_new_metric("net_ip.${alias}.status",      "[${alias}] status", status, infos)
                    if (status == 'Connected') {
                        connect_if[alias] = status
                    }
                }
            }
            infos['net_ip'] = "${connect_if}"
            test_item.devices(csv, headers)
            test_item.results(infos)
        }
    }

    def tcp(TestItem test_item) {
        def command = '''\
            |Get-ItemProperty "HKLM:SYSTEM\\CurrentControlSet\\Services\\Tcpip\\Parameters" | `
            |    Format-List
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('tcp') {
                new File("${local_dir}/tcp")
            }
            def tcpinfo    = [:]
            lines.eachLine {
                (it =~ /^KeepAliveInterval\s*:\s+(.+)$/).each {m0,m1->
                    add_new_metric("tcp.keepalive_interval", "TCP.KeepAlive間隔", m1, tcpinfo)
                }
                (it =~ /^KeepAliveTime\s*:\s+(.+)$/).each {m0,m1->
                    add_new_metric("tcp.keepalive_time", "TCP.KeepAliveタイム", m1, tcpinfo)
                }
                (it =~ /^TcpMaxDataRetransmissions\s*:\s+(.+)$/).each {m0,m1->
                    add_new_metric("tcp.max_data_retran", "TCP.MaxDataRetran", m1, tcpinfo)
                }
            }
            def setting = (tcpinfo.size() > 0) ? 'Configured' : 'NotConfigured'
            tcpinfo['tcp'] = setting
            test_item.results(tcpinfo)
        }
    }

    def firewall(TestItem test_item) {
        run_script('Get-NetFirewallRule -Direction Inbound -Enabled True') {
            def lines = exec('firewall') {
                new File("${local_dir}/firewall")
            }
            def instance_number = 0
            def firewall_info = [:].withDefault{[:]}
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    firewall_info[instance_number][m1] = m2
                }
                if (it.size() == 0 && firewall_info[instance_number].size() > 0)
                    instance_number ++
            }
            instance_number --
            def headers = ['Name', 'DisplayGroup', 'DisplayName', 'Status']

            def groups = [:]
            def csv = []
            (0..instance_number).each { row ->
                def columns = []
                headers.each { header ->
                    columns.add( firewall_info[row][header] ?: '')
                }
                csv << columns
                def group_key = firewall_info[row]['DisplayGroup']
                if (group_key) {
                    groups[group_key] = 1
                }
            }
            test_item.devices(csv, headers)

            def res = [:]
            groups.each { group_key, value ->
                add_new_metric("firewall.${group_key}", "[${group_key}]", "Enable", res)
            }
            res['firewall'] = "${groups.keySet()}"
            test_item.results(res)
        }
    }

    def filesystem(TestItem test_item) {
        run_script('Get-WmiObject Win32_LogicalDisk | Format-List *') {
            def lines = exec('filesystem') {
                new File("${local_dir}/filesystem")
            }
            def csv = []
            def filesystems = [:]
            def drive_letter = 'unkown'
            def infos = [:]
            lines.eachLine {
                (it =~ /^DeviceID\s*:\s+(.+):$/).each {m0,m1->
                    drive_letter = m1
                }
                (it =~ /^Size\s*:\s+(\d+)$/).each {m0,m1->
                    def size_gb = Math.ceil(m1.toDouble()/(1024*1024*1024)) as Integer
                    add_new_metric("filesystem.${drive_letter}.size_gb", "ドライブ[${drive_letter}] 容量GB", size_gb, infos)
                    csv << [drive_letter, size_gb]
                    filesystems[drive_letter] = size_gb
                }
                (it =~ /^Description\s*:\s+(.+)$/).each {m0,m1->
                    add_new_metric("filesystem.${drive_letter}.desc", "ドライブ[${drive_letter}] 種別", m1, infos)
                }
                (it =~ /^FileSystem\s*:\s+(.+)$/).each {m0,m1->
                    add_new_metric("filesystem.${drive_letter}.filesystem", "ドライブ[${drive_letter}] ファイルシステム", m1, infos)
                }
            }
            def headers = ['device_id', 'size_gb']
            test_item.devices(csv, headers)
            infos['filesystem'] = "${filesystems}"
            test_item.results(infos)
            test_item.verify_number_equal_map('filesystem', filesystems)
        }
    }


    def service(TestItem test_item) {
        run_script('Get-Service | FL') {
            def lines = exec('service') {
                new File("${local_dir}/service")
            }
            def instance_number = 0
            def service_info = [:].withDefault{[:]}
            def statuses = [:]
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    service_info[instance_number][m1] = m2
                }
                if (it.size() == 0 && service_info[instance_number].size() > 0)
                    instance_number ++
            }
            instance_number --
            def headers = ['Name', 'DisplayName', 'Status']

            def csv      = []
            def services = [:]
            def infos = [:]
            (0..instance_number).each { row ->
                def columns = []
                headers.each { header ->
                    columns.add( service_info[row][header] ?: '')
                }
                def service_id = service_info[row]['Name']
                (service_id=~/^(.+)_([a-z0-9]+?)$/).each { m0, m1, m2 ->
                    service_id = "${m1}_XXXXXX"
                }
                // println service_id
                def status = service_info[row]['Status']
                infos[service_id] = status
                statuses[service_id] = status
                csv << columns
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
            services['service'] = "${instance_number} services"
            test_item.devices(csv, headers)
            test_item.results(services)
            test_item.verify_text_search_map('service', infos)
        }
    }

    def user(TestItem test_item) {

        def command = '''
            |$result = @()
            |$accountObjList =  Get-CimInstance -ClassName Win32_Account
            |$userObjList = Get-CimInstance -ClassName Win32_UserAccount
            |foreach($userObj in $userObjList)
            |{
            |    $IsLocalAccount = ($userObjList | ?{$_.SID -eq $userObj.SID}).LocalAccount
            |    if($IsLocalAccount)
            |    {
            |        $query = "WinNT://{0}/{1},user" -F $env:COMPUTERNAME,$userObj.Name
            |        $dirObj = New-Object -TypeName System.DirectoryServices.DirectoryEntry -ArgumentList $query
            |        $UserFlags = $dirObj.InvokeGet("UserFlags")
            |        $DontExpirePasswd = [boolean]($UserFlags -band 0x10000)
            |        $AccountDisable   = [boolean]($UserFlags -band 0x2)
            |        $obj = New-Object -TypeName PsObject
            |        Add-Member -InputObject $obj -MemberType NoteProperty -Name "UserName" -Value $userObj.Name
            |        Add-Member -InputObject $obj -MemberType NoteProperty -Name "DontExpirePasswd" -Value $DontExpirePasswd
            |        Add-Member -InputObject $obj -MemberType NoteProperty -Name "AccountDisable" -Value $AccountDisable
            |        Add-Member -InputObject $obj -MemberType NoteProperty -Name "SID" -Value $userObj.SID
            |        $result += $obj
            |    }
            |}
            |$result | Format-List
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('user') {
                new File("${local_dir}/user")
            }
            def account_number = 0
            def account_info   = [:].withDefault{[:]}
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    account_info[account_number][m1] = m2
                }
                if (it.size() == 0 && account_info[account_number].size() > 0)
                    account_number ++
            }
            account_number --
            def headers = ['UserName', 'DontExpirePasswd', 'AccountDisable', 'SID']

            def csv   = []
            def res   = [:]
            def users = [:]
            (0..account_number).each { row ->
                def columns = []
                headers.each { header ->
                    columns.add( account_info[row][header] ?: '')
                }
                def user_name        = account_info[row]['UserName']
                def dont_expire_pass = account_info[row]['DontExpirePasswd']
                def account_disable  = account_info[row]['AccountDisable']

                add_new_metric("user.${user_name}.DontExpirePasswd", 
                               "[${user_name}] パスワード無期限", 
                               dont_expire_pass, res)
                add_new_metric("user.${user_name}.AccountDisable", 
                               "[${user_name}] アカウント失効", 
                               account_disable, res)
                users[user_name] = account_disable
                csv << columns
            }
            res['user'] = "${users}"
            test_item.devices(csv, headers)
            test_item.results(res)
        }
    }

    def packages(TestItem test_item) {
        def package_info = [:].withDefault{0}
        def versions = [:]
        def csv = []
        def packagename
        def vendor

        def command = '''\
            |Get-WmiObject Win32_Product | `
            |    Select-Object Name, Vendor, Version | `
            |    Format-List
            |Get-ChildItem -Path( `
            |  'HKLM:SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall', `
            |  'HKCU:SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall') | `
            |  % { Get-ItemProperty $_.PsPath | Select-Object DisplayName, Publisher, DisplayVersion } | `
            |  Format-List
            |'''.stripMargin()
        run_script(command) {
            def lines = exec('packages') {
                new File("${local_dir}/packages")
            }
            lines.eachLine {
                (it =~ /Name\s+:\s(.+)/).each {m0, m1->
                    packagename = m1
                }
                (it =~ /Vendor\s+:\s(.+)/).each {m0, m1->
                    vendor = m1
                }
                (it =~ /Publisher\s+:\s(.+)/).each {m0, m1->
                    vendor = m1
                }
                (it =~ /Version\s+:\s(.+)/).each {m0, m1->
                    def version = "'${m1}'"
                    versions[packagename] = version
                    // package_info['packages.' + packagename] = version
                    // add_new_metric("packages.${packagename}", "${packagename}", version, package_info)
                    csv << [packagename, vendor, version]
                }
            }
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
            // println package_info
            def headers = ['name', 'vendor', 'version']
            test_item.devices(csv, headers)
            package_info['packages'] = "${csv.size()} packages"
            test_item.results(package_info)
        }
    }

    def user_account_control(TestItem test_item) {
        run_script('Get-ItemProperty "HKLM:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Policies\\System"') {
            def lines = exec('user_account_control') {
                new File("${local_dir}/user_account_control").text
            }
            def setting = 'Disable'
            def res = [:].withDefault{'0'}
            lines.eachLine {
                (it =~ /^EnableLUA\s*:\s+(.+)$/).each {m0,m1->
                    if (m1 == '1')
                        setting = 'Enable'
                }
                (it =~ /^ConsentPromptBehaviorAdmin\s*:\s+(.+)$/).each {m0,m1->
                    add_new_metric("user_account_control.ConsentPromptBehaviorAdmin", "ユーザアカウント制御 ConsentPromptBehaviorAdmin", "'${m1}'", res)
                }
                (it =~ /^ConsentPromptBehaviorUser\s*:\s+(.+)$/).each {m0,m1->
                    add_new_metric("user_account_control.ConsentPromptBehaviorUser", "ユーザアカウント制御 ConsentPromptBehaviorUser", "'${m1}'", res)
                }
                (it =~ /^EnableInstallerDetection\s*:\s+(.+)$/).each {m0,m1->
                    add_new_metric("user_account_control.EnableInstallerDetection", "ユーザアカウント制御 EnableInstallerDetection", "'${m1}'", res)
                }
            }
            res['user_account_control'] = setting
             test_item.results(res)
        }
    }


    def remote_desktop(TestItem test_item) {
        run_script('(Get-Item "HKLM:System\\CurrentControlSet\\Control\\Terminal Server").GetValue("fDenyTSConnections")') {
            def value = exec('remote_desktop') {
                new File("${local_dir}/remote_desktop").text
            }
            def remote_desktop_flags = ['0' : 'Enable', '1' : 'Disable']
            def result = remote_desktop_flags[value.trim()] ?: 'unkown'

            test_item.results(result)
        }
    }

    def dns(TestItem test_item) {
        run_script('Get-DnsClientServerAddress|FL') {
            def lines = exec('dns') {
                new File("${local_dir}/dns").text
            }
            def adresses = [:]
            lines.eachLine {
                // ServerAddresses : {192.168.0.254}
                (it =~ /ServerAddresses\s+:\s+\{(.+)\}$/).each {m0,m1->
                    adresses[m1] = 1
                }
            }
            def value = adresses.keySet().toString()
            test_item.results(value)
        }
    }

    def ntp(TestItem test_item) {
        run_script('(Get-Item "HKLM:System\\CurrentControlSet\\Services\\W32Time\\Parameters").GetValue("NtpServer")') {
            def lines = exec('ntp') {
                new File("${local_dir}/ntp").text
            }
            test_item.results(lines)
        }
    }

   def whoami(TestItem test_item) {
        run_script('whoami /user') {
            def lines = exec('whoami') {
                new File("${local_dir}/whoami")
            }
            def row = 0
            lines.eachLine {
                // ヘッダのセパレータ '===' の次の行の値を抽出する
                if (row == 1) {
                    // 空白区切りで'ユーザ名'と'SID'を抽出
                    def results = it.split(/ +/)
                    def infos = [whoami_user: results[0], whoami_sid: results[1]]
                    // 結果登録。Excelシートに 検査ID 'whoami_user' と 'whoami_sid'
                    // を追加する必要あり
                    test_item.results(infos)
                }
                (it =~ /^====/).each {
                    row++
                }
            }
        }
    }

    def task_scheduler(TestItem test_item) {
        def command = '''\
            |Get-ScheduledTask | `
            | ? {$_.State -eq "Ready"} | `
            | Get-ScheduledTaskInfo | `
            | ? {$_.NextRunTime -ne $null}| `
            | Format-List
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('task_scheduler') {
                new File("${local_dir}/task_scheduler")
            }

            def schedule_info = [:].withDefault{0}
            def csv = []
            def last_result
            def task_name
            def task_path
            def missed_runs
            def res = [:]
            lines.eachLine {
                (it =~ /LastTaskResult\s+:\s(.+)$/).each {m0, m1->
                    last_result = m1
                }
                (it =~ /NumberOfMissedRuns\s+:\s(.+)$/).each {m0, m1->
                    missed_runs = m1
                }
                (it =~ /TaskName\s+:\s(.+)$/).each {m0, m1->
                    task_name = m1
                }
                (it =~ /TaskPath\s+:\s(.+)$/).each {m0, m1->
                    task_path = m1
                    add_new_metric("task_scheduler.${task_name}", task_name, 'Ready', res)
                    csv << [task_name, last_result, missed_runs, task_path]
                }
            }
            def headers = ['task_name', 'last_result', 'missed_runs', 'task_path']
            test_item.devices(csv, headers)
            res['task_scheduler'] = "${csv.size()} tasks"
            test_item.results(res)
        }
    }

    def etc_hosts(TestItem test_item) {
        run_script('Get-Content "$($env:windir)\\system32\\Drivers\\etc\\hosts"') {
            def lines = exec('etc_hosts') {
                new File("${local_dir}/etc_hosts")
            }

            def csv = []
            def res = [:]
            lines.eachLine {
                (it =~ /^\s*([\d|\.]+?)\s+(.+)$/).each {m0, ip,hostname->
                    csv << [ip, hostname]
                    add_new_metric("etc_hosts.${ip}", 
                               ip, hostname, res)
                }
            }
            def headers = ['ip', 'host_name']
            test_item.devices(csv, headers)
            res["etc_hosts"] = "${csv.size()} hosts"
            test_item.results(res)
        }
    }

    def net_accounts(TestItem test_item) {
        run_script('net accounts') {
            def lines = exec('net_accounts') {
                new File("${local_dir}/net_accounts")
            }
            def row = 0
            def csv = []
            def policy_number = 0
            lines.eachLine {
                (it =~ /^(.+):\s+(.+?)$/).each {m0, item_name, value ->
                    csv << [item_name, value]
                    policy_number ++
                }
            }
            def headers = ['item_name', 'value']
            test_item.devices(csv, headers)
            test_item.results((policy_number > 0) ? 'Policy found' : 'NG')
        }
    }

    def monitor(TestItem test_item) {
        run_script('Get-WmiObject Win32_DesktopMonitor | FL') {
            def lines = exec('monitor') {
                new File("${local_dir}/monitor")
            }
            def instance_number = 0
            def monitor_info = [:].withDefault{[:]}
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    monitor_info[instance_number][m1] = m2
                }
                if (it.size() == 0 && monitor_info[instance_number].size() > 0)
                    instance_number ++
            }
            instance_number --
            def monitor_names = [:]
            def infos = [:].withDefault{[]}
            (0..instance_number).each { row ->
                def alias    = monitor_info[row]['Name']
                def screen_y = monitor_info[row]['ScreenHeight'] ?: 'Unspecified'
                def screen_x = monitor_info[row]['ScreenWidth'] ?: 'Unspecified'
                monitor_names[alias] = 1
                infos["monitor.height"] << screen_y
                infos["monitor.width"]  << screen_x
            }
            infos['monitor'] = "${monitor_names.keySet()}"
            test_item.results(infos)
        }
    }

    def patch_lists(TestItem test_item) {
        run_script('wmic qfe') {
            def lines = exec('patch_lists') {
                new File("${local_dir}/patch_lists")
            }
            def row = 0
            def csv = []
            def res = [:]
            lines.eachLine {
                (it =~ /\s(KB\d+)\s/).each {m0, knowledge_base ->
                    csv << [knowledge_base]
                    add_new_metric("patch_lists.${knowledge_base}", 
                                   knowledge_base, 'Enable', res)
                }
            }
            def headers = ['knowledge_base']
            test_item.devices(csv, headers)
            res['patch_lists'] = "${csv.size()} patches"
            test_item.results(res)
        }
    }

    def ie_version(TestItem test_item) {
        run_script('Get-ItemProperty "HKLM:SOFTWARE\\Microsoft\\Internet Explorer"') {
            def lines = exec('ie_version') {
                new File("${local_dir}/ie_version")
            }
            def res = [:]
            lines.eachLine {
                (it =~ /^(svcVersion|svcUpdateVersion|RunspaceId)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    add_new_metric("ie_version.${m1}", "IEバージョン.${m1}", m2, res)
                    // res["ie_version.$m1"] = m2
                 }
            }
            test_item.results(res)
            test_item.make_summary_text('svcVersion':'Version', 'svcUpdateVersion':'UpdateVersion')
        }
    }


    def feature(TestItem test_item) {
        run_script('Get-WindowsFeature | ?{$_.InstallState -eq [Microsoft.Windows.ServerManager.Commands.InstallState]::Installed} | FL') {
            def lines = exec('feature') {
                new File("${local_dir}/feature")
            }
            def instance_number = 0
            def feature_info = [:].withDefault{[:]}
            def res = [:]
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    feature_info[instance_number][m1] = m2
                }
                if (it.size() == 0 && feature_info[instance_number].size() > 0) {
                    instance_number ++
                }
            }
            def headers = ['Name','DisplayName', 'Path', 'Depth']
            def csv = []
            def feature_name = ''
            (0..instance_number).each { row ->
                feature_info[row].with {
                    if ( it.size() > 0 ) {
                        def columns = []
                        headers.each { header ->
                            columns.add( it[header] ?: '')
                        }
                        csv << columns
                        def name = it['Name']
                        def display_name = it['DisplayName']
                        add_new_metric("feature.${name}", display_name, "True", res)
                    }
                }
            }
            test_item.devices(csv, headers)
            res['feature'] = csv.size().toString() + ' installed'
            test_item.results(res)
        }
    }

    def system_log(TestItem test_item) {
        run_script('Get-EventLog system | Where-Object { $_.EntryType -eq "Error" } | FL') {
            def lines = exec('system_log') {
                new File("${local_dir}/system_log")
            }
            def max_row = 0
            def log_info = [:].withDefault{[:]}
            def log_categorys = [:]
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    log_info[max_row][m1] = m2
                }
                if (it.size() == 0 && log_info[max_row].size() > 0) {
                    max_row ++
                }
            }
            def headers = ['Index','TimeGenerated','InstanceId', 'Source', 'Message']
            def csv = []
            if (max_row > 100) {
                max_row = 100
            }
            def log_name = ''
            (0..max_row).each { row ->
                log_info[row].with {
                    if ( it.size() > 0 ) {
                        def columns = []
                        headers.each { header ->
                            columns.add( it[header] ?: '')
                        }
                        csv << columns
                    }
                }
            }
            test_item.devices(csv, headers)
            test_item.results("${csv.size().toString()} events")
        }
    }

    def apps_log(TestItem test_item) {
        run_script('Get-EventLog application | Where-Object { $_.EntryType -eq "Error" } | FL') {
            def lines = exec('apps_log') {
                new File("${local_dir}/apps_log")
            }
            def max_row = 0
            def log_info = [:].withDefault{[:]}
            def log_categorys = [:]
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    log_info[max_row][m1] = m2
                }
                if (it.size() == 0 && log_info[max_row].size() > 0) {
                    max_row ++
                }
            }
            def headers = ['Index','TimeGenerated','InstanceId', 'Source', 'Message']
            def csv = []
            if (max_row > 100) {
                max_row = 100
            }
            def log_name = ''
            (0..max_row).each { row ->
                log_info[row].with {
                    if ( it.size() > 0 ) {
                        def columns = []
                        headers.each { header ->
                            columns.add( it[header] ?: '')
                        }
                        csv << columns
                    }
                }
            }
            test_item.devices(csv, headers)
            test_item.results("${csv.size().toString()} events")
        }
    }
}

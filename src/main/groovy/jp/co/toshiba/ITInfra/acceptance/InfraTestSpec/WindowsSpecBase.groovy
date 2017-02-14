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
class WindowsSpecBase extends InfraTestSpec {

    String ip
    String os_user
    String os_password
    String script_path
    int    timeout

    def init() {
        super.init()

        this.ip          = test_server.ip
        def os_account   = test_server.os_account
        this.os_user     = os_account['user']
        this.os_password = os_account['password']
        this.script_path = local_dir + '/get_windows_spec.ps1'
        this.timeout     = test_server.timeout
    }

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()

        def cmd = """\
            |powershell -NonInteractive ${script_path}
            |-log_dir '${local_dir}'
            |-ip '${ip}' -server '${server_name}'
            |-user '${os_user}' -password '${os_password}'
        """.stripMargin()

        runPowerShellTest('lib/template', 'Windows', cmd, test_items)

        test_items.each { test_item ->
            if (test_item.test_id == 'logon_test') {
                _logon_test(test_item)
            }
        }
    }

    def _logon_test(TestItem test_item) {
        def results = [:]
        def script_path = new File("lib/template/test_logon_Windows.ps1").getAbsolutePath()
        test_server.os_account.logon_test.each { test_user->
            def cmd = """\
                |powershell -NonInteractive ${script_path}
                |-ip '${this.ip}'
                |-user '${test_user.user}' -password '${test_user.password}'
            """.stripMargin()
            try {
                execPowerShell(script_path, cmd)
                results[test_user.user] = true
            } catch (IOException e) {
                log.error "[PowershellTest] Powershell script faild.\n" + e
                results[test_user.user] = false
            }
        }
        test_item.results(results.toString())
    }

    def cpu(TestItem test_item) {
        run_script('Get-WmiObject Win32_Processor') {
            def lines = exec('cpu') {
                new File("${local_dir}/cpu")
            }
            def cpuinfo    = [:].withDefault{0}
            def cpu_number = 0
            lines.eachLine {
                (it =~ /^DeviceID\s+:\s(.+)$/).each {m0, m1->
                    cpu_number += 1
                }
                (it =~ /^Name\s+:\s(.+)$/).each {m0, m1->
                    cpuinfo["model_name"] = m1
                }
                (it =~ /^MaxClockSpeed\s+:\s(.+)$/).each {m0, m1->
                    cpuinfo["mhz"] = m1
                }
            }
            cpuinfo["cpu_total"] = cpu_number
            test_item.results(cpuinfo)
        }
    }

    def os(TestItem test_item) {
        def command = '''\
            |Get-WmiObject Win32_OperatingSystem | `
            |    Format-List Caption,CSDVersion,ProductType,OSArchitecture
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('os') {
                new File("${local_dir}/os")
            }
            def osinfo    = [:].withDefault{0}
            lines.eachLine {
                (it =~ /^Caption\s*:\s+(.+)$/).each {m0,m1->
                    osinfo['os_caption'] = m1
                }
                (it =~ /^CSDVersion\s*:\s+(.+)$/).each {m0,m1->
                    osinfo['os_csd_version'] = m1
                }
                (it =~ /^ProductType\s*:\s+(.+)$/).each {m0,m1->
                    osinfo['os_product_type'] = m1
                }
                (it =~ /^OSArchitecture\s*:\s+(.+)$/).each {m0,m1->
                    osinfo['os_architecture'] = m1
                }
            }
            test_item.results(osinfo)
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
                    meminfo['total_virtual'] = m1
                }
                (it =~ /^TotalVisibleMemorySize\s*:\s+(\d+)$/).each {m0,m1->
                    meminfo['total_visible'] = m1
                }
                (it =~ /^FreePhysicalMemory\s*:\s+(\d+)$/).each {m0,m1->
                    meminfo['free_physical'] = m1
                }
                (it =~ /^FreeVirtualMemory\s*:\s+(\d+)$/).each {m0,m1->
                    meminfo['free_virtual'] = m1
                }
                (it =~ /^FreeSpaceInPagingFiles\s*:\s+(\d+)$/).each {m0,m1->
                    meminfo['free_space'] = m1
                }
            }
            test_item.results(meminfo)
        }
    }

    def system(TestItem test_item) {
        run_script('Get-WmiObject -Class Win32_ComputerSystem') {
            def lines = exec('system') {
                new File("${local_dir}/system")
            }
            def systeminfo = [:]
            lines.eachLine {
                (it =~ /^(Domain|Manufacturer|Model|Name)\s*:\s+(.+?)$/).each {m0,m1,m2->
                    systeminfo[m1] = m2
                }
            }
            test_item.results(systeminfo)
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
            test_item.results(device_number.toString())
        }
    }

    def filesystem(TestItem test_item) {
        run_script('Get-WmiObject Win32_LogicalDisk') {
            def lines = exec('filesystem') {
                new File("${local_dir}/filesystem")
            }
            def csv = []
            def filesystems = [:]
            def device_id
            lines.eachLine {
                (it =~ /^DeviceID\s*:\s+(.+)$/).each {m0,m1->
                    device_id = m1
                }
                (it =~ /^Size\s*:\s+(\d+)$/).each {m0,m1->
                    def size_gb = m1.toDouble()/(1000*1000*1000)
                    filesystems['filesystem.' + device_id] = size_gb
                    csv << [device_id, size_gb]
                }
            }
            def headers = ['device_id', 'size_gb']
            test_item.devices(csv, headers)
            filesystems['filesystem'] = csv.size()
            test_item.results(filesystems)
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

    def storage_timeout(TestItem test_item) {
        run_script('Get-ItemProperty "HKLM:SYSTEM\\CurrentControlSet\\Services\\disk"') {
            def lines = exec('storage_timeout') {
                new File("${local_dir}/storage_timeout")
            }
            def value = ''
            lines.eachLine {
                (it =~ /(TimeOutValue|TimeoutValue)\s+: (\d+)/).each {m0,m1,m2->
                    value = m2
                }
            }
            test_item.results(value)
        }
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

            def csv      = []
            def networks = [:]
            def devices  = []
            (0..device_number).each { row ->
                def columns = []
                headers.each { header ->
                    columns.add( network_info[row][header] ?: '')
                }
                csv << columns
                def service_name = network_info[row]['ServiceName']
                def ip_address   = network_info[row]['IPAddress']
                networks['network.' + service_name] = ip_address
                devices.add(service_name)
            }
            networks['network'] = devices.toString()
            test_item.devices(csv, headers)
            test_item.results(networks)
        }
    }

    def nic_teaming(TestItem test_item) {
        run_script('Get-NetLbfoTeamNic') {
            def lines = exec('nic_teaming') {
                new File("${local_dir}/nic_teaming")
            }

            def results = [:].withDefault{[]}
            lines.eachLine {
                (it =~ /Name\s+:\s(.+)/).each {m0, m1->
                    results["nic_teaming"] << m1
                }
            }
            test_item.results(results)
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

            def csv = []
            (0..instance_number).each { row ->
                def columns = []
                headers.each { header ->
                    columns.add( firewall_info[row][header] ?: '')
                }
                csv << columns
            }
            test_item.devices(csv, headers)
            test_item.results(instance_number.toString())
        }
    }

    def service(TestItem test_item) {
        run_script('Get-Service | FL') {
            def lines = exec('service') {
                new File("${local_dir}/service")
            }
            def instance_number = 0
            def service_info = [:].withDefault{[:]}
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
            (0..instance_number).each { row ->
                def columns = []
                headers.each { header ->
                    columns.add( service_info[row][header] ?: '')
                }
                def service_id = 'service.' + service_info[row]['Name']
                services[service_id] = service_info[row]['Status']
                csv << columns
            }
            services['service'] = instance_number.toString()
            test_item.devices(csv, headers)
            test_item.results(services)
        }
    }

    def user(TestItem test_item) {
        run_script('Get-WmiObject Win32_UserAccount | FL') {
            def lines = exec('user') {
                new File("${local_dir}/user")
            }
            def account_number = 0
            def account_info   = [:].withDefault{[:]}
            def user_names     = []
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    account_info[account_number][m1] = m2
                }
                if (it.size() == 0 && account_info[account_number].size() > 0)
                    account_number ++
            }
            account_number --
            def headers = ['Name', 'FullName', 'Caption', 'Domain']

            def csv = []
            (0..account_number).each { row ->
                def columns = []
                headers.each { header ->
                    columns.add( account_info[row][header] ?: '')
                }
                user_names.add(account_info[row]['Name'])
                csv << columns
            }
            test_item.devices(csv, headers)
            test_item.results(user_names.toString())
        }
    }

    def remote_desktop(TestItem test_item) {
        run_script('(Get-Item "HKLM:System\\CurrentControlSet\\Control\\Terminal Server").GetValue("fDenyTSConnections")') {
            def result = exec('remote_desktop') {
                new File("${local_dir}/remote_desktop").text
            }
            test_item.results(result.toString())
        }
    }

    def dns(TestItem test_item) {
        run_script('Get-DnsClientServerAddress|FL') {
            def lines = exec('dns') {
                new File("${local_dir}/dns")
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
        run_script('w32tm /query /status') {
            def lines = exec('ntp') {
                new File("${local_dir}/ntp")
            }
            def adress = 'NG'
            lines.eachLine {
                (it =~ /^(NtpServer|ソース)\s*:\s*(.+?)$/).each {m0, m1, m2->
                    adress = m2
                }
            }
            test_item.results(adress)
        }
    }
}

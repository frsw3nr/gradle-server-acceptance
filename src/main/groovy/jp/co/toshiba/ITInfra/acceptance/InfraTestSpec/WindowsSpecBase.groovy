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

        this.ip          = test_platform.test_target.ip
        def os_account   = test_platform.os_account
        this.os_user     = os_account['user']
        this.os_password = os_account['password']
        this.script_path = local_dir + '/get_windows_spec.ps1'
        this.timeout     = test_platform.timeout
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

        // test_items.each { test_item ->
        //     if (test_item.test_id == 'logon_test') {
        //         _logon_test(test_item)
        //     }
        // }
    }

    def _logon_test(TestItem test_item) {
        def results = [:]
        def script_path = new File("lib/template/test_logon_Windows.ps1").getAbsolutePath()
        def result = 'Ignored'
        if (test_server.os_account.logon_test) {
            result = 'OK'
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
                   result = 'NG'
                   log.error "[PowershellTest] Powershell script faild.\n" + e
                   results[test_user.user] = false
                }
            }
        }
        results['logon_test'] = result
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

            // Verify 'cpu_total' with equal number
            test_item.verify(verify_data_equal_number(cpuinfo))
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
            osinfo['os_csd_version'] = ''
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
            osinfo['os'] = "${osinfo['os_caption']} ${osinfo['os_architecture']}"
            test_item.results(osinfo)
            // Verify 'os_caption' and 'os_architecture' with intermediate match
            test_item.verify(verify_data_match(osinfo))
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
            meminfo['pyhis_mem'] = meminfo['total_visible']
            test_item.results(meminfo)
            // Verify 'mem_total' with error range
            test_item.verify(verify_data_error_range(meminfo, 0.1))
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
            systeminfo['system'] = "${systeminfo['Domain']}\\${systeminfo['Name']}"
            test_item.results(systeminfo)
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
            def drive_letter = 'unkown'
            def infos = [:]
            lines.eachLine {
                (it =~ /^DeviceID\s*:\s+(.+):$/).each {m0,m1->
                    drive_letter = m1
                }
                (it =~ /^Size\s*:\s+(\d+)$/).each {m0,m1->
                    def size_gb = m1.toDouble()/(1024*1024*1024)
                    filesystems['filesystem.' + drive_letter] = size_gb
                    csv << [drive_letter, size_gb]
                    infos[drive_letter] = Math.ceil(size_gb) as Integer
                }
            }
            def headers = ['device_id', 'size_gb']
            test_item.devices(csv, headers)
            filesystems['filesystem'] = infos.toString()
            test_item.results(filesystems)

            // Verify targets include in the result of list
            def target_checks = target_info('filesystem')
            if (target_checks) {
                test_item.verify(verify_map(target_checks, infos))
            }
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
            def ip_configs   = [:]
            (0..device_number).each { row ->
                def columns = []
                headers.each { header ->
                    columns.add( network_info[row][header] ?: '')
                }
                csv << columns
                def ip_address   = parse_ip(network_info[row]['IPAddress'])
                def gateway      = parse_ip(network_info[row]['DefaultIPGateway'])
                def subnet       = parse_ip(network_info[row]['IPSubnet'])
                ip_configs[ip_address] = "${gateway},${subnet}"
            }

            test_item.devices(csv, headers)
            test_item.results(ip_configs.keySet().toString())

            // Verify targets include in the result of list
            def target_checks = target_info('net_config')
            println "target_checks:$target_checks"
            println "ip_configs:$ip_configs"
            if (target_checks) {
                test_item.verify(verify_map(target_checks, ip_configs))
            }
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
                (it =~ /Name\s+:\s(.+)/).each {m0, m1->
                    devices << m1
                    teaming = 'Configured'
                }
            }
            def results = ['teaming': teaming, 'devices': devices]
            test_item.results(results.toString())
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

            // Verify targets include in the result of map
            def target_checks = target_info('service')
            if (target_checks) {
                test_item.verify(verify_map(target_checks, services, 'service'))
            }

        }
    }

    def packages(TestItem test_item) {
        def package_info = [:].withDefault{0}
        def csv = []
        def packagename
        def vendor
        def version
        def package_count = 0

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
                    version = m1
                    package_info['packages.' + packagename] = m1
                    package_count ++
                    csv << [packagename, vendor, version]
                }
            }
            def headers = ['name', 'vendor', 'version']
            test_item.devices(csv, headers)
            package_info['packages'] = package_count.toString()
            test_item.results(package_info)
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
            def user_names     = [:]
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    account_info[account_number][m1] = m2
                }
                if (it.size() == 0 && account_info[account_number].size() > 0)
                    account_number ++
            }
            account_number --
            def headers = ['UserName', 'DontExpirePasswd', 'AccountDisable', 'SID']

            def csv = []
            (0..account_number).each { row ->
                def columns = []
                headers.each { header ->
                    columns.add( account_info[row][header] ?: '')
                }
                user_names[account_info[row]['UserName']] = account_info[row]['AccountDisable']
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
            def schedule_count = 0
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
                    schedule_info['task_scheduler.' + task_name] = last_result
                    schedule_count ++
                    csv << [task_name, last_result, missed_runs, task_path]
                }
            }
            def headers = ['task_name', 'last_result', 'missed_runs', 'task_path']
            test_item.devices(csv, headers)
            schedule_info['task_scheduler'] = (schedule_count == 0) ? 'Not found' : "${schedule_count} schedule found."
            test_item.results(schedule_info)
        }
    }

    def etc_hosts(TestItem test_item) {
        run_script('Get-Content "$($env:windir)\\system32\\Drivers\\etc\\hosts"') {
            def lines = exec('etc_hosts') {
                new File("${local_dir}/etc_hosts")
            }

            def hostsinfo    = [:].withDefault{0}
            def csv = []
            def hosts_number = 0
            lines.eachLine {
                (it =~ /^\s*([\d|\.]+?)\s+(.+)$/).each {m0, ip,hostname->
                    csv << [ip, hostname]
                    hostsinfo['hostsinfo.' + hostname] = ip
                    hosts_number ++
                }
            }
            def headers = ['ip', 'host_name']
            test_item.devices(csv, headers)
            hostsinfo["etc_hosts"] = hosts_number
            test_item.results(hostsinfo)
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

    def patch_lists(TestItem test_item) {
        run_script('wmic qfe') {
            def lines = exec('patch_lists') {
                new File("${local_dir}/patch_lists")
            }
            def row = 0
            def csv = []
            def patch_number = 0
            lines.eachLine {
                (it =~ /\s(KB\d+)\s/).each {m0, knowledge_base ->
                    csv << [knowledge_base]
                    patch_number ++
                }
            }
            def headers = ['knowledge_base']
            test_item.devices(csv, headers)
            test_item.results(patch_number.toString())
        }
    }

    def ie_version(TestItem test_item) {
        run_script('Get-ItemProperty "HKLM:SOFTWARE\\Microsoft\\Internet Explorer"') {
            def lines = exec('ie_version') {
                new File("${local_dir}/ie_version")
            }
            def ie_info = [:]
            lines.eachLine {
                (it =~ /^(Version|svcVersion)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    ie_info[m1] = m2
                 }
            }
            test_item.results(ie_info.toString())
        }
    }

    def network_profile(TestItem test_item) {
        run_script('Get-NetConnectionProfile') {
            def lines = exec('network_profile') {
                new File("${local_dir}/network_profile")
            }
            def instance_number = 0
            def network_info = [:].withDefault{[:]}
            def network_categorys = [:]
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    network_info[instance_number][m1] = m2
                }
                if (it.size() == 0 && network_info[instance_number].size() > 0) {
                    instance_number ++
                }
            }
            def headers = ['Name','InterfaceAlias', 'InterfaceIndex', 'NetworkCategory',
                           'IPv4Connectivity','IPv6Connectivity']

            def csv = []
            def network_name = ''
            (0..instance_number).each { row ->
                network_info[row].with {
                    if ( it.size() > 0 ) {
                        def columns = []
                        headers.each { header ->
                            columns.add( it[header] ?: '')
                        }
                        csv << columns
                        network_categorys[it['InterfaceAlias']] = it['NetworkCategory']
                    }
                }
            }
            test_item.devices(csv, headers)
            test_item.results(network_categorys.toString())
        }
    }

    def feature(TestItem test_item) {
        run_script('Get-WindowsFeature | ?{$_.InstallState -eq [Microsoft.Windows.ServerManager.Commands.InstallState]::Installed} | FL') {
            def lines = exec('feature') {
                new File("${local_dir}/feature")
            }
            def instance_number = 0
            def feature_info = [:].withDefault{[:]}
            def feature_categorys = [:]
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
                    }
                }
            }
            test_item.devices(csv, headers)
            test_item.results(csv.size().toString() + ' installed')
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
            test_item.results(csv.size().toString() + ' event found')
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
            test_item.results(csv.size().toString() + ' event found')
        }
    }
}

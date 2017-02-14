package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
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

    def setup_exec(TestItem[] test_items) {
        super.setup_exec(test_items)
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

    // def cpu(TestItem test_item) {
    //     run_script('Get-WmiObject Win32_Processor') {
    //         def lines = exec('cpu') {
    //             new File("${local_dir}/cpu")
    //         }

    //         def cpuinfo    = [:].withDefault{0}
    //         def cpu_number = 0
    //         lines.eachLine {
    //             (it =~ /DeviceID\s+:\s(.+)/).each {m0, m1->
    //                 cpu_number += 1
    //             }
    //             (it =~ /Name\s+:\s(.+)/).each {m0, m1->
    //                 cpuinfo["model_name"] = m1
    //             }
    //             (it =~ /MaxClockSpeed\s+:\s(.+)/).each {m0, m1->
    //                 cpuinfo["mhz"] = m1
    //             }
    //         }
    //         cpuinfo["cpu_total"] = cpu_number
    //         test_item.results(cpuinfo)
    //     }
    // }

    // def memory(TestItem test_item) {
    //     def command = '''\
    //         |Get-WmiObject Win32_OperatingSystem | `
    //         |    select TotalVirtualMemorySize,TotalVisibleMemorySize, `
    //         |        FreePhysicalMemory,FreeVirtualMemory,FreeSpaceInPagingFiles
    //         |'''.stripMargin()

    //     run_script(command) {
    //         def lines = exec('memory') {
    //             new File("${local_dir}/memory")
    //         }
    //         def meminfo    = [:].withDefault{0}
    //         lines.eachLine {
    //             (it =~ /^TotalVirtualMemorySize\s*:\s+(\d+)$/).each {m0,m1->
    //                 meminfo['total_virtual'] = m1
    //             }
    //             (it =~ /^TotalVisibleMemorySize\s*:\s+(\d+)$/).each {m0,m1->
    //                 meminfo['total_visible'] = m1
    //             }
    //             (it =~ /^FreePhysicalMemory\s*:\s+(\d+)$/).each {m0,m1->
    //                 meminfo['free_physical'] = m1
    //             }
    //             (it =~ /^FreeVirtualMemory\s*:\s+(\d+)$/).each {m0,m1->
    //                 meminfo['free_virtual'] = m1
    //             }
    //             (it =~ /^FreeSpaceInPagingFiles\s*:\s+(\d+)$/).each {m0,m1->
    //                 meminfo['free_space'] = m1
    //             }
    //         }
    //         test_item.results(meminfo)
    //     }
    // }

    // def driver(TestItem test_item) {
    //     run_script('Get-WmiObject Win32_PnPSignedDriver') {
    //         def lines = exec('driver') {
    //             new File("${local_dir}/driver")
    //         }
    //         def driverinfo = [:]
    //         lines.eachLine {
    //             (it =~ /^DeviceName\s*:\s+(.*Network.*?)$/).each {m0,m1->
    //                 driverinfo[m1] = 1
    //             }
    //         }
    //         test_item.results(driverinfo.keySet().toString())
    //     }
    // }


    // def filesystem(TestItem test_item) {
    //     run_script('Get-WmiObject Win32_LogicalDisk') {
    //         def lines = exec('filesystem') {
    //             new File("${local_dir}/filesystem")
    //         }
    //         def csv = []
    //         def filesystem_info = [:]
    //         def device_id
    //         lines.eachLine {
    //             (it =~ /^DeviceID\s*:\s+(.+)$/).each {m0,m1->
    //                 device_id = m1
    //             }
    //             (it =~ /^Size\s*:\s+(\d+)$/).each {m0,m1->
    //                 def size_gb = m1.toDouble()/(1000*1000*1000)
    //                 filesystem_info[device_id] = size_gb
    //                 csv << [device_id, size_gb]
    //             }
    //         }
    //         def headers = ['device_id', 'size_gb']
    //         test_item.devices(csv, headers)
    //         test_item.results(filesystem_info.toString())
    //     }
    // }

    // def fips(TestItem test_item) {
    //     run_script('Get-Item "HKLM:System\\CurrentControlSet\\Control\\Lsa\\FIPSAlgorithmPolicy"') {
    //         def lines = exec('fips') {
    //             new File("${local_dir}/fips")
    //         }
    //         def fips_info = ''
    //         lines.eachLine {
    //             (it =~ /^FIPSAlgorithmPolicy\s+Enabled\s+: (.+?)\s+/).each {m0,m1->
    //                 fips_info = (m1 == '0') ? 'Disabled' : 'Enabled'
    //             }
    //         }
    //         test_item.results(fips_info)
    //     }
    // }

    // def storage_timeout(TestItem test_item) {
    //     run_script('Get-ItemProperty "HKLM:SYSTEM\\CurrentControlSet\\Services\\disk"') {
    //         def lines = exec('storage_timeout') {
    //             new File("${local_dir}/storage_timeout")
    //         }
    //         def value = ''
    //         lines.eachLine {
    //             (it =~ /TimeOutValue\s+: (\d+)/).each {m0,m1->
    //                 value = m1
    //             }
    //         }
    //         test_item.results(value)
    //     }
    // }

    // def network(TestItem test_item) {
    //     def command = '''\
    //         |Get-WmiObject Win32_NetworkAdapterConfiguration | `
    //         | Where{$_.IpEnabled -Match "True"} | `
    //         | Select MacAddress, IPAddress, DefaultIPGateway, Description | `
    //         | Format-List `
    //         |'''.stripMargin()

    //     run_script(command) {
    //         def lines = exec('network') {
    //             new File("${local_dir}/network")
    //         }
    //         def network_info = [:].withDefault{[:]}
    //         def tmp = [:].withDefault{''}
    //         def csv = []
    //         lines.eachLine {
    //             (it =~ /^DHCPEnabled\s*:\s+(.+)$/).each {m0,m1->
    //                 tmp['dhcp'] = m1
    //             }
    //             (it =~ /^IPAddress\s*:\s+(.+)$/).each {m0,m1->
    //                 tmp['ip'] = m1
    //             }
    //             (it =~ /^MacAddress\s*:\s+(.+)$/).each {m0,m1->
    //                 tmp['mac'] = m1
    //             }
    //             (it =~ /^DefaultIPGateway\s*:\s+(.+)$/).each {m0,m1->
    //                 tmp['gw'] = m1
    //             }
    //             (it =~ /^Description\s*:\s+(.+?)$/).each {m0,m1->
    //                 network_info[m1]['dhcp'] = tmp['dhcp']
    //                 network_info[m1]['ip']   = tmp['ip']
    //                 network_info[m1]['mac']  = tmp['mac']
    //                 network_info[m1]['gw']   = tmp['gw']
    //                 csv << [m1, tmp['ip'], tmp['mac'], tmp['gw'], tmp['dhcp']]
    //             }
    //         }
    //         def headers = ['device', 'ip', 'mac', 'gw', 'dhcp']
    //         test_item.devices(csv, headers)
    //         test_item.results(network_info.toString())
    //     }
    // }

}

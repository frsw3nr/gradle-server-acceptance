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

    def init() {
        super.init()

        this.ip          = test_server.ip
        def os_account   = test_server.os_account
        this.os_user     = os_account['user']
        this.os_password = os_account['password']
        this.script_path = local_dir + '/get_windows_spec.ps1'
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
    }

    def cpu(TestItem test_item) {
        def command = '''\
            |Get-WmiObject -Credential $cred -ComputerName $ip Win32_Processor
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('cpu') {
                new File("${local_dir}/cpu")
            }

            def cpuinfo    = [:].withDefault{0}
            def cpu_number = 0
            lines.eachLine {
                (it =~ /DeviceID\s+:\s(.+)/).each {m0, m1->
                    cpu_number += 1
                }
                (it =~ /Name\s+:\s(.+)/).each {m0, m1->
                    cpuinfo["model_name"] = m1
                }
                (it =~ /MaxClockSpeed\s+:\s(.+)/).each {m0, m1->
                    cpuinfo["mhz"] = m1
                }
            }
            cpuinfo["cpu_total"] = cpu_number
            test_item.results(cpuinfo)
        }
    }

    def memory(TestItem test_item) {
        def command = '''\
            |Get-WmiObject -Credential $cred -ComputerName $ip Win32_OperatingSystem | `
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

    def driver(TestItem test_item) {
        def command = '''\
            |Get-WmiObject -Credential $cred -ComputerName $ip Win32_PnPSignedDriver
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('driver') {
                new File("${local_dir}/driver")
            }
            def driverinfo = [:]
            lines.eachLine {
                (it =~ /^DeviceName\s*:\s+(.*Network.*?)$/).each {m0,m1->
                    driverinfo[m1] = 1
                }
            }
            test_item.results(driverinfo.keySet().toString())
        }
    }

    def filesystem(TestItem test_item) {
        def command = '''\
            |Get-WmiObject -Credential $cred -ComputerName $ip Win32_LogicalDisk
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('filesystem') {
                new File("${local_dir}/filesystem")
            }
            def csv = []
            def filesystem_info = [:]
            def device_id
            lines.eachLine {
                (it =~ /^DeviceID\s*:\s+(.+)$/).each {m0,m1->
                    device_id = m1
                }
                (it =~ /^Size\s*:\s+(\d+)$/).each {m0,m1->
                    filesystem_info[device_id] = m1
                    csv << [device_id, m1]
                }
            }
            def headers = ['device_id', 'size']
            test_item.devices(csv, headers)
            test_item.results(filesystem_info.toString())
        }
    }

    def fips(TestItem test_item) {
        def command = '''\
            |$reg = Get-WmiObject -List -Namespace root\\default -Credential $cred -ComputerName $ip | `
            |Where-Object {$_.Name -eq "StdRegProv"}
            |$HKLM = 2147483650
            |$reg.GetStringValue($HKLM,"System\\CurrentControlSet\\Control\\Lsa\\FIPSAlgorithmPolicy","Enabled").sValue
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('fips') {
                new File("${local_dir}/fips")
            }
            def fips_info = [:].withDefault{0}
            lines.eachLine {
            }
            test_item.results(fips_info)
        }
    }

// $hklm  = 2147483650
// $key   = "SYSTEM\CurrentControlSet\Services\disk"
// $value = "TimeoutValue"

// $sw = [Diagnostics.Stopwatch]::StartNew()

// $reg = get-wmiobject -list "StdRegProv" -namespace root\default -computername $ip -credential $cred | where-object { $_.Name -eq "StdRegProv" }
// $sw.Stop()
// write-host $sw.Elapsed

// $reg.GetStringValue($hklm, $key, $value) | Select-Object uValue


//    uValue
//    ------
//        60

    def storage_timeout(TestItem test_item) {
        def command = '''\
            |$hklm  = 2147483650
            |$key   = "SYSTEM\\CurrentControlSet\\Services\\disk"
            |$value = "TimeoutValue"
            |$reg = get-wmiobject -list "StdRegProv" -namespace root\\default -computername $ip -credential $cred | where-object { $_.Name -eq "StdRegProv" }
            |$reg.GetStringValue($hklm, $key, $value) | Select-Object uValue
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('storage_timeout') {
                new File("${local_dir}/storage_timeout")
            }
            def timeout_info = [:].withDefault{0}
            lines.eachLine {
                println it
            }
            test_item.results(timeout_info)
        }
    }

    def network(TestItem test_item) {
        def command = '''\
            |Get-WmiObject -Credential $cred -ComputerName $ip Win32_NetworkAdapterConfiguration
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('network') {
                new File("${local_dir}/network")
            }
            def network_info = [:].withDefault{[:]}
            def tmp = [:].withDefault{''}
            def csv = []
            lines.eachLine {
                (it =~ /^DHCPEnabled\s*:\s+(.+)$/).each {m0,m1->
                    tmp['dhcp'] = m1
                }
                (it =~ /^IPAddress\s*:\s+(.+)$/).each {m0,m1->
                    tmp['ip'] = m1
                }
                (it =~ /^DefaultIPGateway\s*:\s+(.+)$/).each {m0,m1->
                    tmp['gw'] = m1
                }
                (it =~ /^Description\s*:\s+(.*Network.*?)$/).each {m0,m1->
                    network_info[m1]['dhcp'] = tmp['dhcp']
                    network_info[m1]['ip']   = tmp['ip']
                    network_info[m1]['gw']   = tmp['gw']
                    csv << [m1, tmp['ip'], tmp['gw'], tmp['dhcp']]
                }
            }
            def headers = ['device', 'ip', 'gw', 'dhcp']
            test_item.devices(csv, headers)
            test_item.results(network_info.toString())
        }
    }
}

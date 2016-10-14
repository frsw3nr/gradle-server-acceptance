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
        def command = '''
            Get-WmiObject -Credential $cred -ComputerName $ip Win32_Processor
        '''

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
            |Get-WmiObject -Credential $cred -ComputerName $ip Win32_OperatingSystem |
            |    select TotalVirtualMemorySize,TotalVisibleMemorySize,
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
}

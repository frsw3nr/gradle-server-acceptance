package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
@InheritConstructors
class vCenterSpecBase extends InfraTestSpec {

    String vcenter_ip
    String vcenter_user
    String vcenter_password
    String vm
    String local_dir
    String script_path

    def init() {
        super.init()
 
        def vcenter_account = test_server.vcenter_account
        vcenter_ip       = vcenter_account['server']
        vcenter_user     = vcenter_account['user']
        vcenter_password = vcenter_account['password']
        vm               = test_server.vm
        local_dir        = test_server.evidence_log_dir
        script_path      = local_dir + '/get_vCenter_spec.ps1'
    }

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()

        def cmd = """\
            |powershell -NonInteractive ${script_path}
            |-log_dir '${local_dir}'
            |-server '${server_name}' -vm '${vm}'
            |-user '${vcenter_user}' -password '${vcenter_password}'
            |-vcenter '${vcenter_ip}'
        """.stripMargin()

        runPowerShellTest('lib/template', 'vCenter', cmd, test_items)
    }

    def vm(test_item) {
        def command = '''
            get-vm $vm
            | select NumCpu, PowerState, MemoryGB, VMHost, @{N="Cluster";E={Get-Cluster -VM $_}}
            | Format-List
        '''
        run_script(command) {
            def lines = exec('vm') {
                new File("${local_dir}/vm")
            }

            def res = [:]
            lines.eachLine {
                (it =~  /^(NumCpu|PowerState|MemoryGB|VMHost|Cluster)\s+:\s(.+)$/).each { m0,m1,m2->
                    res[m1] = m2
                }
            }
            test_item.results(res)
        }
    }
}

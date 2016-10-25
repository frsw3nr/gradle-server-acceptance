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
    String script_path

    def init() {
        super.init()

        def vcenter_account = test_server.vcenter_account
        vcenter_ip       = vcenter_account['server']
        vcenter_user     = vcenter_account['user']
        vcenter_password = vcenter_account['password']
        vm               = test_server.vm
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
        def command = '''\
            |Get-VM $vm | `
            | select NumCpu, PowerState, MemoryGB, VMHost, @{N="Cluster";E={Get-Cluster -VM $_}} | `
            | Format-List
        '''.stripMargin()
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

    def vmwaretool(test_item) {
        def command = '''\
            |Get-VM $vm | `
            | Get-AdvancedSetting vmware.tools.internalversion,vmware.tools.requiredversion | `
            | Select Name, Value
        '''.stripMargin()
        run_script(command) {
            def lines = exec('vmwaretool') {
                new File("${local_dir}/vmwaretool")
            }

            def res = [:]
            lines.eachLine {
                (it =~ /^vmware\.tools\.(.+?)\s+(\d+.)\s*$/).each { m0,m1,m2->
                    res[m1] = m2
                }
            }
            test_item.results(res.toString())
        }
    }

    def vm_iops_limit(test_item) {

        run_script('Get-VMResourceConfiguration -VM $vm | format-custom -property DiskResourceConfiguration') {
            def lines = exec('vm_iops_limit') {
                new File("${local_dir}/vm_iops_limit")
            }

            def res = [:]
            def vm_iops_limt_number = 0
            lines.eachLine {
                (it =~  /DiskLimitIOPerSecond = (.+?)\s*$/).each { m0,m1->
                    res["IOLimit${vm_iops_limt_number}"] = m1
                    vm_iops_limt_number++
                }
            }
            test_item.results(res.toString())
        }
    }

    def vm_storage(test_item) {
        run_script('Get-Harddisk -VM $vm | select Parent, Filename,CapacityGB, StorageFormat, DiskType') {
            def lines = exec('vm_storage') {
                new File("${local_dir}/vm_storage")
            }
            def csv = []
            def res = [:].withDefault{[:]}
            def utils = []
            def vm_storage_number = 0

            lines.eachLine {
                (it =~  /^(StorageFormat|Filename|CapacityGB)\s+:\s(.+)$/).each { m0,m1,m2->
                    res[vm_storage_number][m1] = m2
                    if (m1 == 'StorageFormat') {
                        csv << [
                            res[vm_storage_number]['Filename'],
                            res[vm_storage_number]['StorageFormat'],
                            res[vm_storage_number]['CapacityGB'],
                        ]
                        utils <<
                            res[vm_storage_number]['StorageFormat'] + ':' +
                            res[vm_storage_number]['CapacityGB']
                        vm_storage_number ++
                    }
                }
            }
            def headers = ['Filename', 'StorageFormat', 'CapacityGB']
            test_item.devices(csv, headers)
            test_item.results(utils.toString())
        }
    }

    def vm_timesync(test_item) {
        run_script('Get-VM $vm | Select @{N=\'TimeSync\';E={$_.ExtensionData.Config.Tools.syncTimeWithHost}}') {
            def lines = exec('vm_timesync') {
                new File("${local_dir}/vm_timesync")
            }

            def res = [:]

            lines.eachLine {
                (it =~  /^(TimeSync)\s+:\s(.+)$/).each { m0,m1,m2->
                    res[m1] = m2
                }
            }
            test_item.results(res.toString())
        }
    }
}

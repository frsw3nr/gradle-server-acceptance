package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import org.hidetake.groovy.ssh.Ssh
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

@Slf4j
@InheritConstructors
class vCenterSpec extends vCenterSpecBase {

    def init() {
        super.init()
    }

    def finish() {
        super.finish()
    }

    // def vm(test_item) {
    //     def command = '''\
    //         |Get-VM $vm | `
    //         | select NumCpu, PowerState, MemoryGB, VMHost, @{N="Cluster";E={Get-Cluster -VM $_}} | `
    //         | Format-List
    //     '''.stripMargin()
    //     run_script(command) {
    //         def lines = exec('vm') {
    //             new File("${local_dir}/vm")
    //         }

    //         def res = [:]
    //         lines.eachLine {
    //             (it =~  /^(NumCpu|PowerState|MemoryGB|VMHost|Cluster)\s+:\s(.+)$/).each { m0,m1,m2->
    //                 res[m1] = m2
    //             }
    //         }
    //         test_item.results(res)
    //     }
    // }

    // def vmwaretool(test_item) {
    //     def command = '''\
    //         |Get-VM $vm | `
    //         | Get-AdvancedSetting vmware.tools.internalversion,vmware.tools.requiredversion | `
    //         | Select Name, Value
    //     '''.stripMargin()
    //     run_script(command) {
    //         def lines = exec('vmwaretool') {
    //             new File("${local_dir}/vmwaretool")
    //         }

    //         def res = [:]
    //         lines.eachLine {
    //             (it =~  /^vmware\.tools\.(.+?)\s+(\d+.)\s*$/).each { m0,m1,m2->
    //                 res[m1] = m2
    //             }
    //         }
    //         test_item.results(res)
    //     }
    // }

    // def vm_iops_limit(test_item) {
    //     run_script('Get-VMResourceConfiguration -VM $vm | select DiskResourceConfiguration') {
    //         def lines = exec('vm_iops_limit') {
    //             new File("${local_dir}/vm_iops_limit")
    //         }

    //         def res = [:]
    //         def vm_iops_limt_number = 0
    //         lines.eachLine {
    //             (it =~  /DiskLimitIOPerSecond = (\d+)/).each { m0,m1->
    //                 res["IOLimit${vm_iops_limt_number}"] = m1
    //                 vm_iops_limt_number++
    //             }
    //         }
    //         test_item.results(res.toString())
    //     }
    // }

    // def vm_storage(test_item) {
    //     run_script('Get-Harddisk -VM $vm | select Parent, Filename,CapacityGB, StorageFormat, DiskType') {
    //         def lines = exec('vm_storage') {
    //             new File("${local_dir}/vm_storage")
    //         }

    //         def res = [:].withDefault{[:]}
    //         def vm_storage_number = 0
    //         lines.eachLine {
    //             (it =~  /^(StorageFormat|CapacityGB)\s+:\s(.+)$/).each { m0,m1,m2->
    //                 res["Disk${vm_storage_number}"][m1] = m2
    //                 if (m1 == 'CapacityGB')
    //                     vm_storage_number ++
    //             }
    //         }
    //         test_item.results(res.toString())
    //     }
    // }

    // def vm_timesync(test_item) {
    //     run_script('Get-VM $vm | Select @{N=\'TimeSync\';E={$_.ExtensionData.Config.Tools.syncTimeWithHost}}') {
    //         def lines = exec('vm_timesync') {
    //             new File("${local_dir}/vm_timesync")
    //         }

    //         def res = [:]

    //         lines.eachLine {
    //             (it =~  /^(TimeSync)\s+:\s(.+)$/).each { m0,m1,m2->
    //                 res[m1] = m2
    //             }
    //         }
    //         test_item.results(res.toString())
    //     }
    // }

}

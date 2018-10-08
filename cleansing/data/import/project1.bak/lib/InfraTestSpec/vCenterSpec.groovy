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

    // def datastore(test_item) {
    //     run_script("Get-Datastore -VM $vm | FL") {
    //         def lines = exec('datastore') {
    //             new File("${local_dir}/datastore")
    //         }
    //         def instance_number = 0
    //         def datastore_info = [:].withDefault{[:]}
    //         def datastore_names = []
    //         lines.eachLine {
    //             (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
    //                 datastore_info[instance_number][m1] = m2
    //             }
    //             if (it.size() == 0 && datastore_info[instance_number].size() > 0) {
    //                 instance_number ++
    //             }
    //         }
    //         def headers = ['Name','DatastoreBrowserPath', 'Type', 'State']
    //         def csv = []
    //         def datastore_name = ''
    //         (0..instance_number).each { row ->
    //             datastore_info[row].with {
    //                 if ( it.size() > 0 ) {
    //                     def columns = []
    //                     headers.each { header ->
    //                         columns.add( it[header] ?: '')
    //                     }
    //                     csv << columns
    //                     datastore_names << it['Name']
    //                 }
    //             }
    //         }
    //         test_item.devices(csv, headers)
    //         test_item.results(datastore_names.toString())
    //     }
    // }

    // def network(test_item) {
    //     run_script("Get-NetworkAdapter -VM $vm | FL") {
    //         def lines = exec('network') {
    //             new File("${local_dir}/network")
    //         }
    //         def instance_number = 0
    //         def network_info = [:].withDefault{[:]}
    //         def connections  = []
    //         lines.eachLine {
    //             (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
    //                 network_info[instance_number][m1] = m2
    //             }
    //             if (it.size() == 0 && network_info[instance_number].size() > 0) {
    //                 instance_number ++
    //             }
    //         }
    //         def headers = ['NetworkName', 'Type', 'ConnectionState']
    //         def csv = []
    //         def network_name = ''
    //         (0..instance_number).each { row ->
    //             network_info[row].with {
    //                 if ( it.size() > 0 ) {
    //                     def columns = []
    //                     headers.each { header ->
    //                         columns.add( it[header] ?: '')
    //                     }
    //                     csv << columns
    //                     def arrs = it['ConnectionState'].split(/,/)
    //                     connections << arrs[arrs.size() - 1].trim()
    //                 }
    //             }
    //         }
    //         test_item.devices(csv, headers)
    //         test_item.results(connections.toString())
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
    //             (it =~ /^vmware\.tools\.(.+?)\s+(\d+.)\s*$/).each { m0,m1,m2->
    //                 res[m1] = m2.trim()
    //             }
    //         }
    //         res['result'] = 'TestFaild'
    //         try {
    //             def internalversion = Integer.decode(res['internalversion'])
    //             def requiredversion = Integer.decode(res['requiredversion'])
    //             if (internalversion == 0)
    //                 res['result'] = 'NotInstalled'
    //             else if (internalversion < requiredversion)
    //                 res['result'] = 'UpdateRequired'
    //             else
    //                 res['result'] = 'OK'
    //         } catch (NumberFormatException e) {
    //             log.warn "Test failed : $e"
    //         }
    //         test_item.results(res.toString())
    //     }
    // }

    // def vm_iops_limit(test_item) {

    //     def command = '''\
    //         |Get-VMResourceConfiguration -VM $vm | `
    //         |format-custom -property DiskResourceConfiguration
    //     '''.stripMargin()
    //     run_script(command) {
    //         def lines = exec('vm_iops_limit') {
    //             new File("${local_dir}/vm_iops_limit")
    //         }

    //         def res = [:]
    //         def disk_id = 1
    //         lines.eachLine {
    //             (it =~  /DiskLimitIOPerSecond = (.+?)\s*$/).each { m0,m1->
    //                 res["IOLimit${disk_id}"] = m1
    //                 disk_id++
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
    //         def csv = []
    //         def res = [:].withDefault{[:]}
    //         def utils = []
    //         def vm_storage_number = 0

    //         lines.eachLine {
    //             (it =~  /^(StorageFormat|Filename|CapacityGB)\s+:\s(.+)$/).each { m0,m1,m2->
    //                 res[vm_storage_number][m1] = m2
    //                 if (m1 == 'StorageFormat') {
    //                     csv << [
    //                         res[vm_storage_number]['Filename'],
    //                         res[vm_storage_number]['StorageFormat'],
    //                         res[vm_storage_number]['CapacityGB'],
    //                     ]
    //                     utils <<
    //                         res[vm_storage_number]['StorageFormat'] + ':' +
    //                         res[vm_storage_number]['CapacityGB']
    //                     vm_storage_number ++
    //                 }
    //             }
    //         }
    //         def headers = ['Filename', 'StorageFormat', 'CapacityGB']
    //         test_item.devices(csv, headers)
    //         test_item.results(utils.toString())
    //     }
    // }

    // def vm_timesync(test_item) {
    //         def command = '''\
    //         |Get-VM $vm |
    //         |Select @{N=\'TimeSync\';E={$_.ExtensionData.Config.Tools.syncTimeWithHost}} |
    //         |Format-List
    //         '''.stripMargin()

    //     run_script(command) {
    //         def lines = exec('vm_timesync') {
    //             new File("${local_dir}/vm_timesync")
    //         }

    //         def res = ['TimeSync': 'TestFaild']
    //         lines.eachLine {
    //             (it =~  /^(TimeSync)\s+:\s(.+)$/).each { m0,m1,m2->
    //                 res['TimeSync'] = m2
    //             }
    //         }
    //         test_item.results(res.toString())
    //     }
    // }

}

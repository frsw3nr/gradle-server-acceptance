package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.hidetake.groovy.ssh.Ssh
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
@InheritConstructors
class VMHostSpec extends VMHostSpecBase {

    def init() {
        super.init()
    }

    def finish() {
        super.finish()
    }

    // def VMHost(test_item) {
    //     run_script('Get-VMHost $vm | Format-List') {
    //         def lines = exec('VMHost') {
    //             new File("${local_dir}/VMHost")
    //         }
    //         def res = [:]
    //         lines.eachLine {
    //             (it =~  /^(.+?)\s+:\s+(.+?)$/).each { m0,m1,m2->
    //                 res[m1] = m2
    //             }
    //         }
    //         test_item.results(res)
    //     }
    // }

    // def Account(test_item) {
    //     run_script('Get-VMHostAccount | Format-Table -Auto') {
    //         def lines = exec('Account') {
    //             new File("${local_dir}/Account")
    //         }
    //         def csv   = []
    //         def csize = []
    //         def row   = -1
    //         lines.eachLine {
    //             (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
    //                 m0, m1, m2, m3, m4 ->
    //                 row = 0
    //                 csize = [m1.size(), m2.size(), m3.size(), m4.size()]
    //             }
    //             if (row > 0 && it.size() > 0) {
    //                 (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.+)$/).each {
    //                     m0, m1, m2, m3, m4 ->
    //                     csv << [m1, m2, m3, m4]*.trim()
    //                 }
    //             }
    //             if (row >= 0)
    //                 row ++;
    //         }
    //         def headers = ['Name', 'Domain', 'Description', 'Server']
    //         test_item.devices(csv, headers)
    //         test_item.results(row.toString())
    //     }
    // }

    // def NetworkAdapter(test_item) {
    //     run_script('Get-VMHostNetworkAdapter -VMHost $vm | Format-Table -Auto') {
    //         def lines = exec('NetworkAdapter') {
    //             new File("${local_dir}/NetworkAdapter")
    //         }
    //         def csv   = []
    //         def csize = []
    //         def row   = -1
    //         lines.eachLine {
    //             (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
    //                 m0, m1, m2, m3, m4, m5, m6 ->
    //                 row = 0
    //                 csize = [m1.size(), m2.size(), m3.size(),
    //                          m4.size(), m5.size(), m6.size()]
    //             }
    //             if (row > 0 && it.size() > 0) {
    //                 (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.+)$/).each {
    //                     m0, m1, m2, m3, m4, m5, m6 ->
    //                     csv << [m1, m2, m3, m4, m5, m6]*.trim()
    //                 }
    //             }
    //             if (row >= 0)
    //                 row ++;
    //         }
    //         def headers = ['Name', 'Mac', 'DhcpEnabled', 'IP', 'SubnetMask', 'DeviceName']
    //         test_item.devices(csv, headers)
    //         test_item.results(row.toString())
    //     }
    // }

    // def Disk(test_item) {
    //     run_script('Get-VMHostDisk -VMHost $vm | Format-List') {
    //         def lines = exec('Disk') {
    //             new File("${local_dir}/Disk")
    //         }
    //         def row = 0
    //         def res = [:].withDefault{[:]}
    //         lines.eachLine {
    //             (it =~  /^(.+?)\s+:\s+(.+?)$/).each { m0,m1,m2->
    //                 if (m1 == 'Cylinders')
    //                     row ++
    //                 res[row][m1] = m2
    //             }
    //         }
    //         def csv = []
    //         def headers = ['Cylinders', 'Heads', 'Sectors', 'Uid']
    //         (1..row).each { id ->
    //             def columns = []
    //             headers.each { header ->
    //                 columns.add res[id][header]
    //             }
    //             csv << columns*.trim()
    //         }
    //         test_item.devices(csv, headers)
    //         test_item.results(row.toString())
    //     }
    // }

    // def DiskPartition(test_item) {
    //     run_script('Get-VMHost $vm | Get-VMHostDisk | Get-VMHostDiskPartition | Format-List') {
    //         def lines = exec('DiskPartition') {
    //             new File("${local_dir}/DiskPartition")
    //         }
    //         def row = 0
    //         def res = [:].withDefault{[:]}
    //         lines.eachLine {
    //             (it =~  /^(.+?)\s+:\s+(.+?)$/).each { m0,m1,m2->
    //                 if (m1 == 'PartitionNumber')
    //                     row ++
    //                 res[row][m1] = m2
    //             }
    //         }
    //         def csv = []
    //         def headers = ['PartitionNumber', 'Logical', 'StartSector', 'EndSector', 'VMHostDisk']
    //         (1..row).each { id ->
    //             def columns = []
    //             headers.each { header ->
    //                 columns.add res[id][header]
    //             }
    //             csv << columns*.trim()
    //         }
    //         test_item.devices(csv, headers)
    //         test_item.results(row.toString())
    //     }
    // }

    // def Datastore(test_item) {
    //     run_script('Get-Datastore -VMHost $vm | Format-Table -Auto') {
    //         def lines = exec('Datastore') {
    //             new File("${local_dir}/Datastore")
    //         }

    //         def csv   = []
    //         def csize = []
    //         def row   = -1
    //         lines.eachLine {
    //             (it =~ /^(-+ *?) (-+ *?) (-.+?)$/).each {
    //                 m0, m1, m2, m3 ->
    //                 row = 0
    //                 csize = [m1.size() + 1, m2.size() + 1, m3.size() + 1]
    //             }
    //             if (row > 0 && it.size() > 0) {
    //                 (it =~ /^(.{${csize[0]}})(.{${csize[1]}})(.+)$/).each {
    //                     m0, m1, m2, m3 ->
    //                     csv << [m1, m2, m3]*.trim()
    //                 }
    //             }
    //             if (row >= 0)
    //                 row ++;
    //         }
    //         def headers = ['Name', 'FreeSpaceGB', 'CapacityGB']
    //         test_item.devices(csv, headers)
    //         test_item.results(row.toString())
    //     }
    // }

}

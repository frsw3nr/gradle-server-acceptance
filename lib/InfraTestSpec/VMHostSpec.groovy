package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.hidetake.groovy.ssh.Ssh
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
@InheritConstructors
class VMHostSpec extends vCenterSpecBase {

    def init() {
        super.init()
    }

    def finish() {
        super.finish()
    }

    def VMHost(test_item) {
        run_script('Get-VMHost $vm | Format-List') {
            def lines = exec('VMHost') {
                new File("${local_dir}/VMHost")
            }
            def res = [:]
            lines.eachLine {
                (it =~  /^(.+?)\s+:\s+(.+?)$/).each { m0,m1,m2->
                    res[m1] = m2
                }
            }
            test_item.results(res)
        }
    }

    def Account(test_item) {
        run_script('Get-VMHostAccount | Format-Table -Auto') {
            def lines = exec('Account') {
                new File("${local_dir}/Account")
            }
            def csv = []
            def row = -1
            lines.eachLine {
                (it =~ /^----/).each {
                    row = 0
                }
                if (row > 0 && it.size() > 0) {
                    (it =~ /^(.{10})(.{7})(.{44})(.+)$/).each {
                        m0, m1, m2, m3, m4 ->
                        csv << [m1, m2, m3, m4]*.trim()
                    }
                }
                if (row >= 0)
                    row ++;
            }
            def headers = ['Name', 'Domain', 'Description', 'Server']
            test_item.devices(csv, headers)
            test_item.results(row.toString())
        }
    }

    def NetworkAdapter(test_item) {
        run_script('Get-VMHostNetworkAdapter -VMHost $vm | Format-Table -Auto') {
            def lines = exec('NetworkAdapter') {
                new File("${local_dir}/NetworkAdapter")
            }
            def csv = []
            def row = -1
            lines.eachLine {
                (it =~ /^----/).each {
                    row = 0
                }
                if (row > 0 && it.size() > 0) {
                    (it =~ /^(.{7})(.{18})(.{12})(.{15})(.{14})(.+)$/).each {
                        m0, m1, m2, m3, m4, m5, m6 ->
                        csv << [m1, m2, m3, m4, m5, m6]*.trim()
                    }
                }
                if (row >= 0)
                    row ++;
            }
            def headers = ['Name', 'Mac', 'DhcpEnabled', 'IP', 'SubnetMask', 'DeviceName']
            test_item.devices(csv, headers)
            test_item.results(row.toString())
        }
    }

    def Disk(test_item) {
        run_script('Get-VMHostDisk -VMHost $vm | Format-List') {
            def lines = exec('Disk') {
                new File("${local_dir}/Disk")
            }
            def row = 0
            def res = [:].withDefault{[:]}
            lines.eachLine {
                (it =~  /^(.+?)\s+:\s+(.+?)$/).each { m0,m1,m2->
                    if (m1 == 'Cylinders')
                        row ++
                    res[row][m1] = m2
                }
            }
            def csv = []
            def headers = ['Cylinders', 'Heads', 'Sectors', 'Uid']
            (1..row).each { id ->
                def columns = []
                headers.each { header ->
                    columns.add res[id][header]
                }
                csv << columns*.trim()
            }
            test_item.devices(csv, headers)
            test_item.results(row.toString())
        }
    }

    def DiskPartition(test_item) {
        run_script('Get-VMHost $vm | Get-VMHostDisk | Get-VMHostDiskPartition | Format-List') {
            def lines = exec('DiskPartition') {
                new File("${local_dir}/DiskPartition")
            }
            def row = 0
            def res = [:].withDefault{[:]}
            lines.eachLine {
                (it =~  /^(.+?)\s+:\s+(.+?)$/).each { m0,m1,m2->
                    if (m1 == 'PartitionNumber')
                        row ++
                    res[row][m1] = m2
                }
            }
            def csv = []
            def headers = ['PartitionNumber', 'Logical', 'StartSector', 'EndSector', 'VMHostDisk']
            (1..row).each { id ->
                def columns = []
                headers.each { header ->
                    columns.add res[id][header]
                }
                csv << columns*.trim()
            }
            test_item.devices(csv, headers)
            test_item.results(row.toString())
        }
    }

    def Datastore(test_item) {
        run_script('Get-Datastore -VMHost $vm | Format-Table -Auto') {
            def lines = exec('Datastore') {
                new File("${local_dir}/Datastore")
            }

            def csv = []
            def row = -1
            lines.eachLine {
                (it =~ /^----/).each {
                    row = 0
                }
                if (row > 0 && it.size() > 0) {
                    (it =~ /^(.{13})(.{12})(.+)$/).each {
                        m0, m1, m2, m3 ->
                        csv << [m1, m2, m3]*.trim()
                    }
                }
                if (row >= 0)
                    row ++;
            }
            def headers = ['Name', 'FreeSpaceGB', 'CapacityGB']
            test_item.devices(csv, headers)
            test_item.results(row.toString())
        }
    }
}

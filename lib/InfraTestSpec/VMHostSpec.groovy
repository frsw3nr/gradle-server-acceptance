package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.hidetake.groovy.ssh.Ssh
import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
@InheritConstructors
class VMHostSpec extends vCenterSpecBase {

    def init() {
        super.init()
        def os_account  = test_platform.os_account
        def standalone  = os_account?.standalone ?: false
        def ip          = test_platform?.test_target?.ip
        this.vcenter_ip = (standalone) ? ip : os_account['vCenter']
        this.vm = test_platform?.test_target?.remote_alias ?: ''
        if (this.vm == '') {
            this.vm = test_platform?.test_target?.name
        }
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
            // println lines
            lines.eachLine {
                (it =~  /^(.+?)\s+:\s+(.+?)$/).each { m0,m1,m2->
                    res[m1] = m2
                }
            }
            res['Version'] = 'ESXi ' + res['Version']
            res['Build'] = "'" + res['Build'] + "'"
            res['VMHost']  = (res.size() > 0) ? 'OK' : 'Not found'
            // println res
            test_item.verify_number_equal('NumCpu', res['NumCpu'])
            test_item.verify_number_equal('MemoryGB', res['MemoryTotalGB'], 0.1)
            test_item.results(res)
        }
    }

    def Account(test_item) {
        run_script('Get-VMHostAccount | Format-Table -Auto') {
            def lines = exec('Account') {
                new File("${local_dir}/Account")
            }
            def csv   = []
            def csize = []
            def row   = -1
            lines.eachLine {
                (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
                    m0, m1, m2, m3, m4 ->
                    row = 0
                    csize = [m1.size(), m2.size(), m3.size(), m4.size()]
                }
                if (row > 0 && it.size() > 0) {
                    (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.+)$/).each {
                        m0, m1, m2, m3, m4 ->
                        csv << [m1, m2, m3, m4]*.trim()
                    }
                }
                if (row >= 0)
                    row ++;
            }
            def headers = ['Name', 'Domain', 'Description', 'Server']
            test_item.devices(csv, headers)
            def result = (row == -1) ? 'No account': "${row} account"
            test_item.results(result)
        }
    }

    def NetworkAdapter(test_item) {
        run_script('Get-VMHostNetworkAdapter -VMHost $vm | Format-Table -Auto') {
            def lines = exec('NetworkAdapter') {
                new File("${local_dir}/NetworkAdapter")
            }
            def net_ip = [:]
            def csv    = []
            def csize  = []
            def row    = -1
            def nic    = 0
            def res    = [:]
            lines.eachLine {
                (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
                    m0, m1, m2, m3, m4, m5, m6 ->
                    row = 0
                    csize = [m1.size(), m2.size(), m3.size(),
                             m4.size(), m5.size(), m6.size()]
                }
                if (row > 0 && it.size() > 0) {
                    (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.+)$/).each {
                        m0, m1, m2, m3, m4, m5, m6 ->
                        def device  = m1.trim()
                        def mac     = m2.trim()
                        def address = m4.trim()
                        def mask    = m5.trim()
                        if (address.size() > 0 && mask.size() > 0) {
                            try {
                                SubnetInfo subnet = new SubnetUtils(address, mask).getInfo()
                                net_ip[device] = subnet.getCidrSignature()
                            } catch (IllegalArgumentException e) {
                                log.error "[ESXiVMHostTest] subnet convert : m1\n" + e
                            }
                            if (address && address != '127.0.0.1') {
                                nic++
                                test_item.lookuped_port_list(address, device)
                                add_new_metric("NetworkAdapter.dev.${nic}",  "[${nic}] デバイス", device, res)
                                add_new_metric("NetworkAdapter.ip.${nic}",   "[${nic}] IP", address, res)
                                add_new_metric("NetworkAdapter.mask.${nic}", "[${nic}] サブネット", mask, res)
                                add_new_metric("NetworkAdapter.mac.${nic}",  "[${nic}] MAC", mac, res)
                            }
                            // println "IP:${address},${device}"

                        }
                        csv << [m1, m2, m3, m4, m5, m6]*.trim()
                    }
                }
                if (row >= 0)
                    row ++;
            }
            def headers = ['Name', 'Mac', 'DhcpEnabled', 'IP', 'SubnetMask', 'DeviceName']
            test_item.devices(csv, headers)
            res['NetworkAdapter'] = net_ip.toString()
            // println net_ip
            test_item.results(res)
        }
    }

    def refVMHostDisk(device) {
        ( device =~ /__([^_].+?)__/ ).each { m0, m1 ->
            device = m1
        }
        ( device =~ /\/\/(.+?)$/ ).each { m0, m1 ->
            device = m1
        }
        return device
    }

    def Disk(test_item) {
        run_script('Get-VMHostDisk -VMHost $vm | Format-List') {
            def lines = exec('Disk') {
                new File("${local_dir}/Disk")
            }
            def row = 0
            def info = [:].withDefault{[:]}
            def device = ''
            def devices = [:]
            def res = [:]
            lines.eachLine {
                (it =~  /^(.+?)\s+:\s+(.+?)$/).each { m0,m1,m2->
                    if (m1 == 'DeviceName') {
                        device = refVMHostDisk(m2)
                    } else if (m1 == 'TotalSectors') {
                        def size = Float.parseFloat(m2) * 512 / 1000000000
                        def size_reform = String.format("%1.1f", size)
                        devices[row] = "${size_reform}GB"
                        add_new_metric("Disk.id.${row}",   "Disk[${row}] ID", device, res)
                        add_new_metric("Disk.size.${row}", "Disk[${row}] 容量GB", size_reform, res)

                    } else if (m1 == 'Cylinders') {
                        row ++
                    }
                    info[row][m1] = m2
                }
            }
            def csv = []
            def headers = ['DeviceName', 'Cylinders', 'Heads', 'Sectors','TotalSectors', 'Uid']
            (1..row).each { id ->
                def columns = []
                headers.each { header ->
                    columns.add info[id][header]
                }
                csv << columns*.trim()
            }
            res['Disk'] = "${devices}"
            test_item.devices(csv, headers)
            test_item.results(res)
        }
    }

    def DiskPartition(test_item) {
        run_script('Get-VMHost $vm | Get-VMHostDisk | Get-VMHostDiskPartition | Format-List') {
            def lines = exec('DiskPartition') {
                new File("${local_dir}/DiskPartition")
            }
            def row = 0
            def info = [:].withDefault{[:]}
            lines.eachLine {
                (it =~  /^(.+?)\s+:\s+(.+?)$/).each { m0,m1,m2->
                    if (m1 == 'PartitionNumber')
                        row ++
                    info[row][m1] = m2
                }
            }
            def csv = []
            def headers = ['PartitionNumber', 'Type', 'Logical', 'StartSector', 'EndSector', 'VMHostDisk']
            (1..row).each { id ->
                def columns = []
                headers.each { header ->
                    columns.add info[id][header]
                }
                csv << columns*.trim()
            }
            test_item.devices(csv, headers)
            test_item.results("${csv.size()} partitions")
        }
    }

    def Datastore(test_item) {
        run_script('Get-Datastore -VMHost $vm | Format-Table -Auto') {
            def lines = exec('Datastore') {
                new File("${local_dir}/Datastore")
            }
            // println lines
            def csv   = []
            def csize = []
            def row   = -1
            def id = 0
            def res  = [:]
            def info = [:]
            lines.eachLine {
                (it =~ /^(-+ *?) (-+ *?) (-.+?)$/).each {
                    m0, m1, m2, m3 ->
                    row = 0
                    csize = [m1.size() + 1, m2.size() + 1, m3.size() + 1]
                }
                if (row > 0 && it.size() > 0) {
                    (it =~ /^(.{${csize[0]}})(.{${csize[1]}})(.+)$/).each {
                        m0, name, free, capacity ->
                        id ++
                        name = name.trim()
                        def free_gb = NumberUtils.toDouble(free.replaceAll(",", ""))
                        def capacity_gb = NumberUtils.toDouble(capacity.replaceAll(",", ""))
                        csv << [name, free_gb, capacity_gb]
                        add_new_metric("Datastore.name.${id}", "Datastore[${id}] データストア名", name, res)
                        add_new_metric("Datastore.capa.${id}", "Datastore[${id}] 容量GB", capacity_gb, res)
                        add_new_metric("Datastore.free.${id}", "Datastore[${id}] 空き容量GB", free_gb, res)
                        info[name] = capacity_gb
                    }
                }
                if (row >= 0)
                    row ++;
            }
            def headers = ['Name', 'FreeSpaceGB', 'CapacityGB']
            test_item.devices(csv, headers)
            res['Datastore'] = "${csv.size()} datastores"
            test_item.results(res)
        }
    }

}

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
            res['vm'] = res['PowerState'] ?: 'unkown'
            test_item.results(res)
            // Verify 'NumCpu', 'MemoryGB' and 'VMHost' with intermediate match
            test_item.verify_number_equal('NumCpu', res['NumCpu'])
            test_item.verify_number_equal('MemoryGB', res['MemoryGB'], 0.1)
            test_item.verify_text_search('VMHost', res['VMHost'])
        }
    }

    def vm_hot_add(test_item) {
        def command = '''\
            |(Get-VM $vm | select ExtensionData).ExtensionData.config | `
            | Select Name,CpuHotAddEnabled,MemoryReservationLockedToMax,MemoryHotAddEnabled | `
            | Format-List
        '''.stripMargin()
        run_script(command) {
            def lines = exec('vm_hot_add') {
                new File("${local_dir}/vm_hot_add")
            }

            def res = [:]
            lines.eachLine {
                (it =~  /^(.+?)\s+:\s(.+?)$/).each { m0,m1,m2->
                    res["vm.${m1}"] = m2
                }
            }
            res['vm_hot_add'] = (res.size() == 0) ? 'NotFound' : 'Found'
            test_item.results(res)
        }
    }

    def datastore(test_item) {
        run_script("Get-Datastore -VM $vm | FL") {
            def lines = exec('datastore') {
                new File("${local_dir}/datastore")
            }
            def instance_number = 0
            def datastore_info = [:].withDefault{[:]}
            def datastore_names = []
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    datastore_info[instance_number][m1] = m2
                }
                if (it.size() == 0 && datastore_info[instance_number].size() > 0) {
                    instance_number ++
                }
            }
            def headers = ['Name','DatastoreBrowserPath', 'Type', 'State']
            def csv = []
            def datastore_name = ''
            (0..instance_number).each { row ->
                datastore_info[row].with {
                    if ( it.size() > 0 ) {
                        def columns = []
                        headers.each { header ->
                            columns.add( it[header] ?: '')
                        }
                        csv << columns
                        datastore_names << it['Name']
                    }
                }
            }
            test_item.devices(csv, headers)
            test_item.results(datastore_names.toString())
            test_item.verify_text_search('datastore', "${datastore_names}")
        }
    }

    def VMNetwork(test_item) {
        run_script("Get-NetworkAdapter -VM $vm | FL") {
            def lines = exec('VMNetwork') {
                new File("${local_dir}/VMNetwork")
            }
            def instance_number = 0
            def network_info = [:].withDefault{[:]}
            def connections  = []
            def nic_types  = []
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    network_info[instance_number][m1] = m2
                }
                if (it.size() == 0 && network_info[instance_number].size() > 0) {
                    instance_number ++
                }
            }
            def headers = ['NetworkName', 'Type', 'ConnectionState']
            def csv = []
            def network_name = ''
            (0..instance_number).each { row ->
                network_info[row].with {
                    if ( it.size() > 0 ) {
                        def columns = []
                        headers.each { header ->
                            columns.add( it[header] ?: '')
                        }
                        csv << columns
                        def arrs = it['ConnectionState'].split(/,/)
                        connections << arrs[arrs.size() - 1].trim()
                        nic_types << it['Type']
                    }
                }
            }
            def infos = [
                'VMNetwork' : "${connections}",
                'VMNetwork.type' : "${nic_types}",
            ]
            test_item.devices(csv, headers)
            test_item.results(infos)
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

            def infos = [:]
            def res = [:]
            lines.eachLine {
                (it =~ /^vmware\.tools\.(.+?)\s+(\d+.)\s*$/).each { m0,m1,m2->
                    res[m1] = m2.trim()
                }
            }
            infos['vmwaretool'] = 'TestFaild'
            try {
                def internalversion = Integer.decode(res['internalversion'])
                def requiredversion = Integer.decode(res['requiredversion'])
                if (internalversion == 0)
                    infos['vmwaretool'] = 'NotInstalled'
                else if (internalversion < requiredversion)
                    infos['vmwaretool'] = 'UpdateRequired'
                else
                    infos['vmwaretool'] = 'OK'
            } catch (NumberFormatException e) {
                log.warn "Test failed : $e"
            }
            infos['vmwaretool.version'] = res.toString()
            test_item.results(infos)
        }
    }

    def vm_iops_limit(test_item) {

        def command = '''\
            |Get-VMResourceConfiguration -VM $vm | `
            |format-custom -property DiskResourceConfiguration
        '''.stripMargin()
        run_script(command) {
            def lines = exec('vm_iops_limit') {
                new File("${local_dir}/vm_iops_limit")
            }

            def res = [:]
            def disk_id = 1
            lines.eachLine {
                (it =~  /DiskLimitIOPerSecond = (.+?)\s*$/).each { m0,m1->
                    res["IOLimit${disk_id}"] = m1
                    disk_id++
                }
            }
            test_item.results(res.toString())
        }
    }

    def vm_conf(test_item) {

        def command = '''\
            |Get-VMResourceConfiguration -VM $vm | `
            |FL
        '''.stripMargin()
        run_script(command) {
            def lines = exec('vm_conf') {
                new File("${local_dir}/vm_conf")
            }

            def res = [:]
            def disk_id = 1
            lines.eachLine {
                (it =~ /^(CpuLimitMhz|MemLimitGB|CpuSharesLevel|MemSharesLevel|CpuAffinity|CpuAffinityList)\s+:\s*(.*)$/).each {m0, m1, m2->
                    res[m1] = m2
                }
            }
            def resource_limit = [
                'CPU': res['CpuLimitMhz'] ?: 'unkown',
                'Mem': res['MemLimitGB'] ?: 'unkown',
            ]
            def shares_level = [
                'CPU': res['CpuSharesLevel'] ?: 'unkown',
                'Mem': res['MemSharesLevel'] ?: 'unkown',
            ]
            def cpu_affinity = "${res['CpuAffinity']} ${res['CpuAffinityList']}"

            def infos = [
                'vm_conf' : (res.size() > 0) ? 'Found' : 'Not found',
                'vm_conf.limit' : "${resource_limit}",
                'vm_conf.shares_level' : "${shares_level}",
                'vm_conf.cpu_affinity' : cpu_affinity,
            ]
            test_item.results(infos)
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

    def vm_floppy(test_item) {
        def command = '''\
            |Get-Vm $vm | Get-FloppyDrive | `
            | Select Parent, Name, ConnectionState | `
            | Format-List
        '''.stripMargin()
        run_script(command) {
            def lines = exec('vm_floppy') {
                new File("${local_dir}/vm_floppy")
            }

            def res = [:]
            lines.eachLine {
                (it =~  /^(.+?)\s+:\s(.+?)$/).each { m0,m1,m2->
                    res[m1] = m2
                }
            }
            // println res
            test_item.results((res.size() == 0) ? 'NotFound' : "${res}")
        }
    }

    def vm_nic_limit(test_item) {
            def command = '''\
            |Get-VM $vm | Get-NetworkAdapter |
            |sort Parent,{$_.ExtensionData.ResourceAllocation.Limit} |
            |select Parent,Name,{$_.ExtensionData.ResourceAllocation.Limit}
            '''.stripMargin()

        run_script(command) {
            def lines = exec('vm_nic_limit') {
                new File("${local_dir}/vm_nic_limit")
            }

            def res = []
            lines.eachLine {
                (it =~ /\s+(\d+)$/).each {m0, m1 ->
                    res << m1
                }
            }
            test_item.results((res.size() == 0) ? "No limit" : "${res}")
        }
    }

    def vm_timesync(test_item) {
            def command = '''\
            |Get-VM $vm |
            |Select @{N=\'TimeSync\';E={$_.ExtensionData.Config.Tools.syncTimeWithHost}} |
            |Format-List
            '''.stripMargin()

        run_script(command) {
            def lines = exec('vm_timesync') {
                new File("${local_dir}/vm_timesync")
            }

            def res = ['TimeSync': 'TestFaild']
            lines.eachLine {
                (it =~  /^(TimeSync)\s+:\s(.+)$/).each { m0,m1,m2->
                    res['TimeSync'] = m2
                }
            }
            test_item.results(res.toString())
        }
    }

    def vm_video_ram(test_item) {
        def command = '''\
            |Get-VM $vm | `
            | Select @{N="VideoRamSizeInKB";E={($_.ExtensionData.Config.Hardware.Device | `
            | where {$_.key -eq 500}).VideoRamSizeInKB}} | Sort-Object Name | `
            | Format-List
        '''.stripMargin()
        run_script(command) {
            def lines = exec('vm_video_ram') {
                new File("${local_dir}/vm_video_ram")
            }

            def res = [:]
            lines.eachLine {
                (it =~  /^(.+?)\s+:\s(.+?)$/).each { m0,m1,m2->
                    res[m1] = m2
                }
            }
            test_item.results((res.size() == 0) ? 'NotFound' : "${res}")
        }
    }
}

package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.lang.math.NumberUtils
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
                (it =~ /^(NumCpu|PowerState|MemoryGB|VMHost|Cluster)\s+:\s(.+)$/).each { m0,item,value->
                    if (item == 'MemoryGB') {
                    def value_number = NumberUtils.toDouble(value)
                        value =  String.format("%1.1f",value_number)
                    }
                    res["vm.${item}"] = value
                }
            }
            res["vm.name"] = vm
            test_item.results(res)
            test_item.make_summary_text('name':'Name', 'NumCpu':'CPU', 'MemoryGB':'MemoryGB','VMHost':'Host')

            // Verify 'NumCpu', 'MemoryGB' and 'VMHost' with intermediate match
            test_item.verify_number_equal('NumCpu',   res['vm.NumCpu'])
            test_item.verify_number_equal('MemoryGB', res['vm.MemoryGB'])
            test_item.verify_text_search('VMHost',    res['vm.VMHost'])
        }
    }

    def vmext(test_item) {
        def command = '''\
            |(Get-VM $vm | select ExtensionData).ExtensionData.config | `
            | Select Name,CpuHotAddEnabled,MemoryReservationLockedToMax,MemoryHotAddEnabled | `
            | Format-List
        '''.stripMargin()
        run_script(command) {
            def lines = exec('vmext') {
                new File("${local_dir}/vmext")
            }

            def res = [:]
            lines.eachLine {
                (it =~  /^(.+?)\s+:\s(.+?)$/).each { m0,m1,m2->
                    res["vmext.${m1}"] = m2
                }
            }

            res['vmext'] = (res.size() == 0) ? 'Not Found' : 'Found'
            test_item.results(res)
            test_item.make_summary_text(
                'CpuHotAddEnabled':'Cpu', 
                'MemoryReservationLockedToMax':'MemoryLock', 
                'MemoryHotAddEnabled':'Memory'
            )
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
                (it =~ /^(CpuLimitMhz|MemLimitGB|CpuSharesLevel|MemReservationMB|MemSharesLevel|CpuAffinity|CpuAffinityList)\s+:\s*(.*)$/).each {m0, m1, m2->
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
            def cpu_affinity = (res['CpuAffinity']) ? "${res['CpuAffinity']} ${res['CpuAffinityList']}" : ''

            def infos = [
                'vm_conf' : res['MemReservationMB'] ?: 'unkown',
                'vm_conf.limit' : "${resource_limit}",
                'vm_conf.shares_level' : "${shares_level}",
                'vm_conf.cpu_affinity' : cpu_affinity,
            ]
            test_item.results(infos)
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
            def res = [:]
            def csv = []
            (0..instance_number).each { row ->
                datastore_info[row].with {
                    if ( it.size() > 0 ) {
                        def columns = []
                        def name = it['Name']
                        headers.each { header ->
                            def new_test_id = "datastore.${header}.${name}"
                            def value = it[header] ?: ''
                            if (header != 'Name' || header != 'DatastoreBrowserPath') {
                                this.test_platform.add_test_metric(new_test_id, 
                                                       "${name}.${header}")
                                res[new_test_id] = value
                            }
                            columns.add( value )
                        }
                        csv << columns
                        datastore_names << it['Name']
                    }
                }
            }
            res['datastore'] = "${datastore_names}"
            test_item.devices(csv, headers)
            test_item.results(res)
            test_item.verify_text_search('datastore', "${datastore_names}")
        }
    }

    def vm_storage(test_item) {
        def command = '''\
            | Get-Harddisk -VM $vm | `
            | select Parent, Filename,CapacityGB, StorageFormat, DiskType | `
            | Format-List
        '''.stripMargin()

        run_script(command) {
            def lines = exec('vm_storage') {
                new File("${local_dir}/vm_storage")
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
            def headers = ['Filename','StorageFormat', 'CapacityGB', 'DiskType']
            def res = [:]
            def csv = []
            def name_counts = [:].withDefault{0}
            (0..instance_number).each { row ->
                datastore_info[row].with {
                    if ( it.size() > 0 ) {
                        def columns = []

                        def value_number = NumberUtils.toDouble(it['CapacityGB'])
                        it['CapacityGB'] = String.format("%1.1f", value_number)

                        def name = it['Filename']
                        (name=~/^\[(.+)\]/).each {m0, m1 ->
                            name = m1
                            name_counts[name] ++
                        }
                        def filename_simple = name
                        if (name_counts[name] > 1) {
                            filename_simple += ".${name_counts[name]}"
                        }
                        headers.each { header ->
                            def new_test_id = "vm_storage.${header}.${filename_simple}"
                            def value = it[header] ?: ''
                            if (header != 'Filename') {
                                def definition = "${filename_simple}.${header}"
                                this.test_platform.add_test_metric(new_test_id, 
                                                                   definition)
                                res[new_test_id] = value
                            }
                            columns.add( value )
                        }
                        csv << columns
                        datastore_names << "$filename_simple/${it['StorageFormat']}/${it['CapacityGB']} GB"
                    }
                }
            }
            res['vm_storage'] = "${datastore_names}"
            test_item.devices(csv, headers)
            test_item.results(res)
            test_item.verify_text_search('datastore', "${datastore_names}")
        }
    }

    def vmnet(test_item) {
        run_script("Get-NetworkAdapter -VM $vm | FL") {
            def lines = exec('vmnet') {
                new File("${local_dir}/vmnet")
            }
            def instance_number = 0
            def network_info = [:].withDefault{[:]}
            lines.eachLine {
                (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
                    network_info[instance_number][m1] = m2
                }
                if (it.size() == 0 && network_info[instance_number].size() > 0) {
                    instance_number ++
                }
            }
            def headers = ['NetworkName', 'Type', 'ConnectionState']
            def res = [:]
            def csv = []
            def network_names = []
            (0..instance_number).each { row ->
                network_info[row].with {
                    if ( it.size() > 0 ) {
                        def columns = []
                        def name = it['NetworkName']
                        network_names << name
                        headers.each { header ->
                            columns.add( it[header] ?: '')
                        }
                        csv << columns

                        def id1 = "vmnet.state.${name}"
                        this.test_platform.add_test_metric(id1, "ネットワーク状態.${name}")
                        def arrs = it['ConnectionState'].split(/,/)
                        res[id1] = arrs[arrs.size() - 1].trim()

                        def id2 = "vmnet.type.${name}"
                        this.test_platform.add_test_metric(id2, "ネットワークタイプ.${name}")
                        res[id2] = it['Type']
                    }
                }
            }
            res['vmnet'] = "${network_names}"
            test_item.devices(csv, headers)
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

            def infos = [:]
            def res = [:]
            lines.eachLine {
                (it =~ /^vmware\.tools\.(.+?)\s+(\d+.)\s*$/).each { m0,m1,m2->
                    res[m1] = m2.trim()
                }
            }
            infos['vmwaretool'] = 'TestFaild'
            if (res) {
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
            }
            infos['vmwaretool.version'] = res.toString()
            test_item.results(infos)
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
                    res["Drive${disk_id}"] = m1
                    disk_id++
                }
            }
            test_item.results(res.toString())
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

            def res
            lines.eachLine {
                (it =~  /^(.+?)\s+:\s(.+?)$/).each { m0,m1,m2->
                    if (m1 == 'ConnectionState') {
                        res = m2
                    }
                }
            }
            test_item.results(res ?: '')
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
            test_item.results((res.size() == 0) ? 'Not Found' : "${res}")
        }
    }
}

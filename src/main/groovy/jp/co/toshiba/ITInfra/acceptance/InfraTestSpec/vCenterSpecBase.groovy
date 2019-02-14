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
    int    timeout = 300

    def init_error(parameter_name) {
        def msg = "vCenter parameter not found : ${parameter_name}"
        throw new IllegalArgumentException(msg)
    }

    def init() {
        super.init()

        // def remote_account = test_server.remote_account
        // vcenter_ip       = remote_account['server']
        // vcenter_user     = remote_account['user']
        // vcenter_password = remote_account['password']
        // vm               = test_server.remote_alias
        def test_target = test_platform?.test_target
        def remote_account = test_platform.os_account
        vcenter_ip       = remote_account?.server
        vcenter_user     = remote_account?.user
        vcenter_password = remote_account?.password
        vm               = test_platform?.test_target?.remote_alias
        // def remote_account = test_platform.os_account ?: init_error('os_account')
        // vcenter_ip       = remote_account['server']   ?: init_error('server')
        // vcenter_user     = remote_account['user']     ?: init_error('user')
        // vcenter_password = remote_account['password'] ?: init_error('password')
        // vm               = test_platform.test_target.remote_alias ?: init_error('vm')
        script_path      = local_dir + '/get_vCenter_spec.ps1'
        timeout          = test_platform.timeout
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
        if (dry_run || (vcenter_ip && vcenter_user && vcenter_password && vm)) {
            runPowerShellTest('lib/template', 'vCenter', cmd, test_items)
        } else {
            log.info "Skip test because the target is not expected to be VM."
        }
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
            // Verify 'NumCpu', 'MemoryGB' and 'VMHost' with intermediate match
            test_item.verify_number_equal('NumCpu', res['NumCpu'])
            test_item.verify_number_equal('MemoryGB', res['MemoryGB'])
            test_item.verify_text_search('VMHost', res['VMHost'])
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
                    }
                }
            }
            test_item.devices(csv, headers)
            test_item.results(connections.toString())
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

}

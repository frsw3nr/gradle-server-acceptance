package InfraTestSpec

import static groovy.json.JsonOutput.*
import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.hidetake.groovy.ssh.Ssh
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
@InheritConstructors
class iLOSpecBase extends InfraTestSpec {

    String ip
    String os_user
    String os_password
    String script_path
    int    timeout

    def init() {
        super.init()

        def os_account   = test_platform.os_account
        this.ip          = test_platform.test_target.ip ?: 'unkown'
        this.os_user     = os_account['user']
        this.os_password = os_account['password']
        this.script_path = local_dir + '/get_hpilo_spec.ps1'
        this.timeout     = test_platform.timeout

        // this.ip          = test_server.ip
        // def os_account   = test_server.os_account
        // this.os_user     = os_account['user']
        // this.os_password = os_account['password']
        // this.script_path = local_dir + '/get_hpilo_spec.ps1'
        // this.timeout     = test_server.timeout
    }

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()

        def cmd = """\
            |powershell -NonInteractive ${script_path}
            |-log_dir '${local_dir}'
            |-ip '${ip}' -server '${server_name}'
            |-user '${os_user}' -password '${os_password}'
        """.stripMargin()

        runPowerShellTest('lib/template', 'iLO', cmd, test_items)
    }

    def trim(String value) {
        return value.replaceAll(/\A[\s　]+/,"").replaceAll(/[\s　]+\z/,"")
    }

    def FindHPiLO(TestItem test_item) {
        def command = '''
            |Find-HPiLO "\$ip" -Full `
            | | FL'''.stripMargin()

        run_script(command) {
            def lines = exec('FindHPiLO') {
                new File("${local_dir}/FindHPiLO")
            }

            def row = 0
            def results = [:]
            def infos = [:].withDefault{[:]}
            lines.eachLine {
                if (it.size() == 0) {
                    row ++
                }
                (it =~/^(.+?)\s+:\s+(.+?)$/).each { m0, m1, m2 ->
                    results[m1] = m2
                    infos[row][m1] = m2
                }
            }
            def headers = []
            def csv = []
            def summarys = []
            infos.each { index, info ->
                def item_names  = []
                def item_values = []
                info.each { key, value ->
                    item_names  << key
                    item_values << value
                    (key =~/(HSI_SBSN|HSI_SPN)/).each { m0, m1 ->
                        summarys << value
                    }
                }
                csv << item_values
                if (headers.size() == 0) {
                    headers = item_names
                }
            }
            test_item.results(results)
            test_item.make_summary_text('HSI_SPN':'Model', 'HSI_SBSN':'S/N')
            test_item.devices(csv, headers)
        }
    }

    def FwVersion(TestItem test_item) {
        def command = '''
            |\$xml = @"
            |<RIBCL VERSION="2.0">
            |   <LOGIN USER_LOGIN="adminname" PASSWORD="password">
            |      <RIB_INFO MODE="read">
            |         <GET_FW_VERSION/>
            |      </RIB_INFO>
            |   </LOGIN>
            |</RIBCL>
            |"@
            |Invoke-HPiLORIBCLCommand -Server "\$ip" -Credential \$cred `
            |    -RIBCLCommand \$xml `
            |    -DisableCertificateAuthentication -OutputType "ribcl"
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('FwVersion') {
                new File("${local_dir}/FwVersion").getText()
            }
            def fw_info = [:].withDefault{0}
            def xmls = []
            xmls = lines.split(/<\?xml version="1\.0"\?>/)
            xmls.each { xml ->
                if (xml.size() > 0) {
                    def records = new XmlSlurper().parseText(xml)
                    def nodes = records.GET_FW_VERSION.findAll {it.@'FIRMWARE_VERSION' != null}
                    nodes.each { node ->
                        fw_info['FwVersion']           = node.@'MANAGEMENT_PROCESSOR'
                        fw_info['FirmwareVersion']     = node.@'FIRMWARE_VERSION'
                        fw_info['FirmwareDate']        = node.@'FIRMWARE_DATE'
                        fw_info['ManagementProcessor'] = node.@'MANAGEMENT_PROCESSOR'
                        fw_info['LicenseType']         = node.@'LICENSE_TYPE'
                    }
                }
            }
            test_item.results(fw_info)
            test_item.make_summary_text('FwVersion':'Ver', 'LicenseType':'Lic')
        }
    }

    def BootMode(TestItem test_item) {
        def command = '''
            |Get-HPiLOCurrentBootMode -Server "\$ip" -Credential \$cred -DisableCertificateAuthentication `
            | | FL
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('BootMode') {
                new File("${local_dir}/BootMode")
            }
            def boot_mode = 'Unkown'
            lines.eachLine {
                (it =~ /BOOT_MODE\s+:\s(.+)/).each {m0, m1->
                    boot_mode = m1
                }
            }
            test_item.results(boot_mode)
        }
    }

    def FwInfo(TestItem test_item) {
        def command = '''
            |Get-HPiLOFirmwareInfo -Server "\$ip" -Credential \$cred -DisableCertificateAuthentication `
            | | Select -ExpandProperty "FirmwareInfo" `
            | | Select "FIRMWARE_NAME", "FIRMWARE_VERSION" `
            | | FL'''.stripMargin()

        run_script(command) {
            def lines = exec('FwInfo') {
                new File("${local_dir}/FwInfo")
            }

            def row = 0
            def infos = [:].withDefault{[:]}
            lines.eachLine {
                if (it.size() == 0) {
                    row ++
                }
                (it =~/^(.+?)\s+:\s+(.+?)$/).each { m0, m1, m2 ->
                    infos[row][m1] = m2
                }
            }
            def headers = []
            def csv = []
            infos.each { index, info ->
                def item_names  = []
                def item_values = []
                info.each { key, value ->
                    item_names  << key
                    item_values << value
                }
                csv << item_values
                if (headers.size() == 0) {
                    headers = item_names
                }
            }
            test_item.devices(csv, headers)
            test_item.results((csv.size() > 0) ? 'Fw Info found' : 'No data')
        }
    }

    def License(TestItem test_item) {
        def command = '''
            |Get-HPiLOLicense -Server "\$ip" -Credential \$cred -DisableCert `
            | | FL'''.stripMargin()

        run_script(command) {
            def lines = exec('License') {
                new File("${local_dir}/License")
            }

            def row = 0
            def infos = [:].withDefault{[:]}
            def license = 'Not Found'
            lines.eachLine {
                if (it.size() == 0) {
                    row ++
                }
                (it =~/^(.+?)\s+:\s+(.+?)$/).each { m0, m1, m2 ->
                    infos[row][m1] = m2
                }
            }
            def headers = []
            def csv = []
            infos.each { index, info ->
                def item_names  = []
                def item_values = []
                info.each { key, value ->
                    item_names  << key
                    item_values << value
                }
                csv << item_values
                if (headers.size() == 0) {
                    headers = item_names
                }
                if (infos[index]['LICENSE_KEY'])
                    license = infos[index]['LICENSE_KEY']
            }
            test_item.devices(csv, headers)
            test_item.results(license)
        }
    }

    def Processor(TestItem test_item) {
        def command = '''
            |Get-HPiLOProcessor -Server "\$ip" -Credential \$cred -DisableCert `
            | | Select -ExpandProperty "PROCESSOR" `
            | | Select "LABEL","NAME","SPEED","STATUS","EXECUTION_TECHNOLOGY" | FL'''.stripMargin()

        run_script(command) {
            def lines = exec('Processor') {
                new File("${local_dir}/Processor")
            }

            def row = 0
            def infos = [:].withDefault{[:]}
            def specs = [:].withDefault{0}
            lines.eachLine {
                if (it.size() == 0) {
                    row ++
                }
                (it =~/^(.+?)\s+:\s+(.+?)$/).each { m0, m1, m2 ->
                    infos[row][m1] = m2
                }
            }
            def headers = []
            def csv = []
            infos.each { index, info ->
                def item_names  = []
                def item_values = []
                def labels = []
                info.each { key, value ->
                    item_names  << key
                    item_values << value
                    if (key == 'NAME' || key == 'EXECUTION_TECHNOLOGY')
                        labels << value
                }
                specs[labels] += 1
                csv << item_values
                if (headers.size() == 0) {
                    headers = item_names
                }
            }
            // println prettyPrint(toJson(specs))
            test_item.devices(csv, headers)
            test_item.results("${specs}")
        }
    }

    def Memory(TestItem test_item) {
        def command = '''
            |Get-HPiLOMemoryInfo -Server "\$ip" -Credential \$cred -DisableCert `
            | | Select -ExpandProperty "MEMORY_COMPONENTS" `
            | | Select "MEMORY_LOCATION","MEMORY_SIZE","MEMORY_SPEED" | FL'''.stripMargin()

        run_script(command) {
            def lines = exec('Memory') {
                new File("${local_dir}/Memory")
            }

            def row = 0
            def infos = [:].withDefault{[:]}
            def specs = [:].withDefault{0}
            lines.eachLine {
                if (it.size() == 0) {
                    row ++
                }
                (it =~/^(.+?)\s+:\s+(.+?)$/).each { m0, m1, m2 ->
                    infos[row][m1] = m2
                }
            }
            def headers = []
            def csv = []
            infos.each { index, info ->
                def item_names  = []
                def item_values = []
                info.each { key, value ->
                    item_names  << key
                    item_values << value
                    if (key == 'MEMORY_SIZE')
                        specs[value] += 1
                }
                csv << item_values
                if (headers.size() == 0) {
                    headers = item_names
                }
            }
            test_item.devices(csv, headers)
            test_item.results(specs.sort().toString())
        }
    }

    def Nic(TestItem test_item) {
        def command = '''
            |\$xml = @"
            |<RIBCL VERSION="2.0">
            |   <LOGIN USER_LOGIN="adminname" PASSWORD="password">
            |      <SERVER_INFO MODE="read">
            |         <GET_EMBEDDED_HEALTH />
            |      </SERVER_INFO>
            |   </LOGIN>
            |</RIBCL>
            |"@
            |Invoke-HPiLORIBCLCommand -Server "\$ip" -Credential \$cred `
            |    -RIBCLCommand \$xml `
            |    -DisableCertificateAuthentication -OutputType "ribcl"
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('Nic') {
                new File("${local_dir}/Nic").getText()
            }
            def device_number = 0
            def nic_infos = [:].withDefault{[:]}
            def csv = []

            def nic_ips = []
            def mng_ips = []
            def xmls = []
            xmls = lines.split(/<\?xml version="1\.0"\?>/)
            xmls.each { xml ->
                if (xml.size() > 0) {
                    def records = new XmlSlurper().parseText(xml)
                    records['GET_EMBEDDED_HEALTH_DATA']['NIC_INFORMATION'].children().each { nic ->
                        nic_infos[device_number]['type'] = nic.name()
                        nic.children().each {
                            nic_infos[device_number][it.name()] = it.@'VALUE'
                            // println "NIC: ${nic.name()} : ${it.name()} : ${it.@'VALUE'}"
                        }
                        device_number ++
                    }
                }
            }
            def row = 0
            def res = [:]
            nic_infos.each { id, nic_info ->
                def ip_address = nic_info?.'IP_ADDRESS'?.toString()
                def type       = nic_info?.'type'?.toString()
                def port       = nic_info?.'NETWORK_PORT'?.toString()
                def desc       = nic_info?.'PORT_DESCRIPTION'?.toString()
                def location   = nic_info?.'LOCATION'?.toString()
                def mac        = nic_info?.'MAC_ADDRESS'?.toString()
                def status     = nic_info?.'STATUS'?.toString()
                csv << [type, port, desc, location, mac, ip_address, status] as String[]
                if (ip_address ==~ /^\d.+/ && location == 'Embedded') {
                    nic_ips << ip_address
                    row ++
                    add_new_metric("Nic.type.${row}",   "[${row}] タイプ",  type, res)
                    add_new_metric("Nic.ip.${row}",     "[${row}] IP",  ip_address, res)
                    add_new_metric("Nic.mac.${row}",    "[${row}] MAC", mac, res)
                    add_new_metric("Nic.status.${row}", "[${row}] ステータス",  status, res)
                    if (type == 'iLO') {
                        test_item.admin_port_list(ip_address, port)
                        mng_ips << ip_address
                    }
                }
            }
            def headers = ['type', 'port', 'desc', 'location', 'mac', 'ip', 'status']
            test_item.devices(csv, headers)
            res['Nic']    = nic_ips.toString()
            res['ip_mng'] = mng_ips.toString()
            test_item.results(res)
            test_item.verify_text_search('ip_mng', "${mng_ips}")
        }
    }

    def Storage(TestItem test_item) {
        def command = '''
            |\$xml = @"
            |<RIBCL VERSION="2.0">
            |   <LOGIN USER_LOGIN="adminname" PASSWORD="password">
            |      <SERVER_INFO MODE="read">
            |         <GET_EMBEDDED_HEALTH />
            |      </SERVER_INFO>
            |   </LOGIN>
            |</RIBCL>
            |"@
            |Invoke-HPiLORIBCLCommand -Server "\$ip" -Credential \$cred `
            |    -RIBCLCommand \$xml `
            |    -DisableCertificateAuthentication -OutputType "ribcl"
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('Storage') {
                new File("${local_dir}/Storage").getText()
            }
            def storage_number = 0
            def device_number  = 0
            def label
            def capacity
            def raid_level = 'Unkown'
            def storage_infos = [:].withDefault{[:]}
            def summarys = [:].withDefault{0}
            def csv = []
            def xmls = []
            def res = [:]
            xmls = lines.split(/<\?xml version="1\.0"\?>/)
            xmls.each { xml ->
                if (xml.size() > 0) {
                    // println xml
                    def records = new XmlSlurper().parseText(xml)
                    records['GET_EMBEDDED_HEALTH_DATA']['STORAGE']['CONTROLLER']['LOGICAL_DRIVE'].children().each { logical_drive ->
                        // println "STORAGE : ${logical_drive.name()} : ${logical_drive.@'VALUE'}"
                        storage_infos[device_number]['L_' + logical_drive.name()] = logical_drive.@'VALUE'
                        if (logical_drive.name() == 'FAULT_TOLERANCE') {
                            // raid_level = "${device_number} ${logical_drive.@'VALUE'}"
                            raid_level = logical_drive.@'VALUE'
                            storage_number ++
                        }
                        logical_drive.children().each {
                            // println "STORAGE : ${logical_drive.name()} : ${it.name()} : ${it.@'VALUE'}"
                            storage_infos[device_number]['P_' + it.name()] = it.@'VALUE'
                            if (it.name() == 'MEDIA_TYPE') {
                                device_number++
                            }
                            if (it.name() == 'CAPACITY') {
                                def key = "${storage_number},${raid_level},${it.@'VALUE'}"
                                summarys[key] += 1
                            }
                        }
                    }
                    def memory_infos = [:]
                    records['GET_EMBEDDED_HEALTH_DATA']['MEMORY']['MEMORY_DETAILS_SUMMARY'].children().each { 
                        memory_details ->
                        def memory_socket = memory_details.name()
                        memory_details.children().each { memory_detail ->
                            def name = memory_detail.name()
                            def value = memory_detail.@'VALUE'
                            // println "$memory_socket, NAME:$name, VAL:$value"
                            // memory_infos[memory_socket][memory_detail.name()] = memory_detail.@'VALUE'
                            if (name == 'TOTAL_MEMORY_SIZE') {
                                memory_infos[memory_socket] = value
                            }
                        }
                    }
                    records['GET_EMBEDDED_HEALTH_DATA']['MEMORY']['MEMORY_COMPONENTS'].children().each { 
                        memory_components ->
                        def mem_row = 0
                        def values = [:]
                        memory_components.children().each { memory_component ->
                            memory_component.each {
                                values[mem_row] = it.@'VALUE'
                                mem_row += 1
                            }
                        }
                        if (values[1] != 'Not Installed') {
                            memory_infos[values[0]] = values[1]
                        }
                    }
                    if (memory_infos)
                        res['Memory'] = memory_infos.toString()
                }
            }
            def row = 1
            def ldisk  = ''
            def status = ''
            def capa   = ''
            def raid   = ''
            storage_infos.each { id, storage_info ->
                ldisk  = storage_info?.'L_LABEL'?.toString() ?: ldisk 
                status = storage_info?.'L_STATUS'?.toString() ?: status
                capa   = storage_info?.'L_CAPACITY'?.toString() ?: capa 
                raid   = storage_info?.'L_FAULT_TOLERANCE'?.toString() ?: raid
                def model  = storage_info?.'P_MODEL'?.toString() 
                def size   = storage_info?.'P_CAPACITY'?.toString()
                def fw     = storage_info?.'P_FW_VERSION'?.toString() 
                def type   = storage_info?.'P_MEDIA_TYPE'?.toString()

                csv << ["'${ldisk}'", status, capa, raid, model, size, fw, type]
                add_new_metric("Storage.type.${row}",   "[${row}] タイプ", type, res)
                add_new_metric("Storage.raid.${row}",   "[${row}] RAID", raid, res)
                add_new_metric("Storage.ldisk.${row}",  "[${row}] LUN", "'${ldisk}'", res)
                add_new_metric("Storage.size.${row}",   "[${row}] サイズ", size, res)
                add_new_metric("Storage.fw.${row}",     "[${row}] FW", fw, res)
                add_new_metric("Storage.status.${row}", "[${row}] ステータス", status, res)

                row ++
            }
            def headers = ['ldisk', 'status', 'capa', 'raid', 'model', 'size', 'fw', 'media_type']
            test_item.devices(csv, headers)
            res['Storage'] = summarys.toString()

            test_item.results(res)
        }
    }

    def SNMP(TestItem test_item) {
        def command = '''
            |Get-HPiLOSNMPIMSetting -Server "\$ip" -Credential \$cred -DisableCert `
            | | FL'''.stripMargin()

        run_script(command) {
            def lines = exec('SNMP') {
                new File("${local_dir}/SNMP")
            }

            def csv = []
            def trap_info = [:].withDefault{[]}
            def snmp_info = [:]

            snmp_info['SNMP']       = 'OK'
            lines.eachLine {
                if (it.size() > 0) {
                    // SNMP_ADDRESS_1                       : 192.168.125.120
                    // SNMP_ADDRESS_1_ROCOMMUNITY           : public1
                    // SNMP_ADDRESS_1_TRAPCOMMUNITY         : trapcomm1
                    // SNMP_ADDRESS_1_TRAPCOMMUNITY_VERSION : v1
                    (it=~/^SNMP_ADDRESS_(\d+)(.+?)\s+:\s(.*?)$/).each { m0, row, m1, m2 ->
                        List metrics = m1.split(/_/)
                        def metric = 'etc'
                        if (metrics.size() == 1) {
                            metric = 'ip'
                        } else if (metrics.size() >= 2) {
                            metric = metrics.pop().toLowerCase()
                        }
                        def value = trim(m2)
                        trap_info[metric] << value
                        add_new_metric("SNMP.${metric}.${row}", "[${row}] ${metric}", value, snmp_info)
                    }
                    // STATUS_TYPE                          : OK
                    // STATUS_MESSAGE                       : OK
                    (it=~/^STATUS_(.+?)\s+:\s(.+?)$/).each { m0, m1, m2 ->
                        add_new_metric("SNMP.status.${m1}", "ステータス ${m1}", m2, snmp_info)
                        if (m2 != 'OK')
                            snmp_info['SNMP'] = m2
                    }
                    (it=~/^(.+?PORT)\s+:\s(.+?)$/).each { m0, m1, m2 ->
                        add_new_metric("SNMP.${m1}", m1, "'${m2}'", snmp_info)
                    }
                    csv << [it]
                }
            }
            def headers = ['snmp_info']
            test_item.devices(csv, headers)
            test_item.results(snmp_info)
            test_item.verify_text_search_list('snmp_address', trap_info['ip'])
            test_item.verify_text_search_list('snmp_community', trap_info['trapcommunity'])
            test_item.verify_text_search_list('snmp_version', trap_info['version'])
        }
    }
}

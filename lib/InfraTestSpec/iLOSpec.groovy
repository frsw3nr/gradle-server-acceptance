package InfraTestSpec

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
            results['FindHPiLO'] = (csv.size() > 0) ? 'Fw Info found' : 'No data'
            test_item.devices(csv, headers)
            test_item.results(results)
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
                info.each { key, value ->
                    item_names  << key
                    item_values << value
                    if (key == 'NAME' || key == 'EXECUTION_TECHNOLOGY')
                        specs[value] += 1
                }
                csv << item_values
                if (headers.size() == 0) {
                    headers = item_names
                }
            }
            test_item.devices(csv, headers)
            test_item.results(specs.toString())
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
            nic_infos.each { id, nic_info ->
                def ip_address = nic_info?.'IP_ADDRESS'?.toString()
                def port       = nic_info?.'NETWORK_PORT'?.toString()
                csv << [
                    nic_info?.'type'?.toString(), 
                    ip_address, 
                    port,
                    nic_info?.'LOCATION'?.toString(), 
                    nic_info?.'MAC_ADDRESS'?.toString(), 
                    nic_info?.'IP_ADDRESS'?.toString(),
                    nic_info?.'STATUS'?.toString(),
                ] as String[]
                if (ip_address ==~ /^\d.+/) {
                    test_item.admin_port_list(ip_address, port)
                    nic_ips << ip_address
                }
            }
            def headers = ['type', 'port', 'desc', 'location', 'mac', 'ip', 'status']
            test_item.devices(csv, headers)
            println "NIC:${nic_ips}"
            def info = [:]
            info['Nic']    = nic_ips.toString()
            info['ip_mng'] = nic_ips.toString()
            test_item.results(info)
            test_item.verify_text_search('ip_mng', nic_ips.toString())
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
            def test_results = [:]
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
                    def memory_infos = [:].withDefault{[:]}
                    records['GET_EMBEDDED_HEALTH_DATA']['MEMORY']['MEMORY_DETAILS_SUMMARY'].children().each { 
                        memory_details ->
                        def memory_socket = memory_details.name()
                        memory_details.children().each { memory_detail ->
                            memory_infos[memory_socket][memory_detail.name()] = memory_detail.@'VALUE'
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
                        test_results['Memory'] = memory_infos.toString()
                }
            }
            storage_infos.each { id, storage_info ->
                csv << [
                    storage_info?.'L_LABEL'?.toString() ?: '', 
                    storage_info?.'L_STATUS'?.toString() ?: '',
                    storage_info?.'L_CAPACITY'?.toString() ?: '', 
                    storage_info?.'L_FAULT_TOLERANCE'?.toString() ?: '',
                    storage_info?.'P_MODEL'?.toString(), 
                    storage_info?.'P_CAPACITY'?.toString(),
                    storage_info?.'P_FW_VERSION'?.toString(), 
                    storage_info?.'P_MEDIA_TYPE'?.toString(),
                ]
            }
            def headers = ['ldisk', 'status', 'capacity', 'raid', 'model', 'size', 'fw', 'media_type']
            test_item.devices(csv, headers)
            test_results['Storage'] = summarys.toString()
            test_item.results(test_results)
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

            snmp_info['SNMP']       = 'No data'
            snmp_info['SNMPStatus'] = 'OK'
            lines.eachLine {
                if (it.size() > 0) {
                    // SNMP_ADDRESS_1                       : 192.168.125.120
                    // SNMP_ADDRESS_1_ROCOMMUNITY           : public1
                    // SNMP_ADDRESS_1_TRAPCOMMUNITY         : trapcomm1
                    // SNMP_ADDRESS_1_TRAPCOMMUNITY_VERSION : v1
                    (it=~/^SNMP_ADDRESS_(\d.+?)\s+:\s(.+?)$/).each { m0, m1, m2 ->
                        List metrics = m1.split(/_/)
                        def metric = 'etc'
                        if (metrics.size() == 1) {
                            metric = 'ip'
                        } else if (metrics.size() >= 2) {
                            metric = metrics.pop().toLowerCase()
                        }
                        def value = trim(m2)
                        trap_info[metric] << value
                    }
                    // STATUS_TYPE                          : OK
                    // STATUS_MESSAGE                       : OK
                    (it=~/^STATUS_(.+?)\s+:\s(.+?)$/).each { m0, m1, m2 ->
                        if (m2 != 'OK')
                            snmp_info['SNMPStatus'] = m2
                    }
                    csv << [it]
                    snmp_info['SNMP'] = 'SNMP Info found'
                }
            }
            def headers = ['snmp_info']
            test_item.devices(csv, headers)
            // test_item.results((csv.size() > 0) ? 'SNMP Info found' : 'No data')
            test_item.results(trap_info)
            test_item.results(snmp_info)
            test_item.verify_text_search('SNMPStatus', snmp_info['SNMPStatus'])
            test_item.verify_text_search_list('snmp_address', trap_info['ip'])
            test_item.verify_text_search_list('snmp_community', trap_info['trapcommunity'])
            test_item.verify_text_search_list('snmp_version', trap_info['version'])
        }
    }
}

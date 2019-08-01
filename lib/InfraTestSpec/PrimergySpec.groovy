package InfraTestSpec

import java.net.*
import javax.net.ssl.*
import java.security.*
import java.security.cert.*
import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import org.hidetake.groovy.ssh.Ssh
import org.json.JSONObject
import org.json.JSONException
import groovy.json.*
import com.goebl.david.Webb
import com.goebl.david.WebbException
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
@InheritConstructors
class PrimergySpec extends LinuxSpecBase {

    class TrustingHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    String ip
    String os_user
    String os_password
    String url
    int iRMC           = 5
    int  timeout       = 30
    int  profile_wait  = 120
    def  serial_no     = null
    Webb webb          = null
    def  http_type     = 'https'
    def  label_fujitsu = 'ts_fujitsu'

    def init() {
        super.init()

        def os_account     = test_platform.os_account
        this.ip            = test_platform.test_target.ip ?: 'unkown'
        this.os_user       = os_account['user']
        this.os_password   = os_account['password']
        this.iRMC          = os_account['iRMC'] ?: 5
        this.http_type     = (this.iRMC >= 5) ? 'https' : 'http'
        this.label_fujitsu = (this.iRMC >= 5) ? 'ts_fujitsu' : 'Fujitsu'
        // this.script_path = local_dir + '/get_hpilo_spec.ps1'
        this.timeout     = test_platform.timeout
    }

    def finish() {
        super.finish()
    }

    def size_info(arr = [:]) {
        (arr['#text'] && arr['@Unit']) ? "${arr['#text']}${arr['@Unit']}" : 'Unkown'
    }

    def allow_all_https_protocol(webb) {
        def trustAllCerts = [
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) 
                    throws CertificateException {
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) 
                    throws CertificateException {
                }
            }
        ] as TrustManager[] 
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        webb.setSSLSocketFactory(sslContext.getSocketFactory());
        webb.setHostnameVerifier(new TrustingHostnameVerifier());
    }

    def setup_exec(TestItem[] test_items) {

        def credentials = os_user + ":" + os_password
        String encoded = credentials.bytes.encodeBase64().toString()

        String auth = "Basic " + encoded
        webb = Webb.create();

        if (this.iRMC >= 5) {
            allow_all_https_protocol(webb);
            webb.setDefaultHeader(Webb.HDR_AUTHORIZATION, auth);
        }

        // サーバのシリアル番号を取得
        if (!dry_run) {
            JSONObject result = webb
                    .get("${http_type}://${ip}/redfish/v1/Systems/")
                    .header("Content-Type", "application/json")
                    .useCaches(false)
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            (result["Members"]?.get(0)["@odata.id"] =~ /.+\/(.+?)$/).each {m0, m1->
                serial_no = m1
            }
            log.info "SERIAL_NO:$serial_no"
        }
        test_items.each {
            def method = this.metaClass.getMetaMethod(it.test_id, TestItem)
            if (method) {
                log.debug "Invoke command '${method.name}()'"
                try {
                    long start = System.currentTimeMillis();
                    method.invoke(this, it)
                    long elapsed = System.currentTimeMillis() - start
                    log.debug "Finish test method '${method.name}()' in ${this.server_name}, Elapsed : ${elapsed} ms"
                    // it.succeed = 1
                } catch (Exception e) {
                    it.verify(false)
                    log.error "[primergy Test] Test method '${method.name}()' faild, skip.\n" + e
                }
            }
        }
    }

    def fwver(test_item) {
        def lines = exec('fwver') {
            def url = "${http_type}://${ip}/redfish/v1/Systems/${serial_no}/Oem/${label_fujitsu}/FirmwareInventory"

            JSONObject result = webb
                                .get(url)
                                .header("Content-Type", "application/json")
                                .useCaches(false)
                                .ensureSuccess()
                                .asJsonObject()
                                .getBody();

            def content = JsonOutput.prettyPrint(result.toString())
            new File("${local_dir}/fwver").text = content
            return content
        }
        def jsonSlurper = new JsonSlurper()
        def fw_infos = jsonSlurper.parseText(lines)
        // "SystemBIOS": "V5.0.0.11 R1.15.0 for D3279-B1x",
        // "BMCFirmware": "8.67F",
        // "SDRRVersion": "3.04",
        // "SDRRId": "0464",
        def headers = ['name', 'value']
        def csv = []
        def res = [:]
        ["SystemBIOS", "BMCFirmware", "SDRRVersion", "SDRRId"].each { item_name ->
            def value = fw_infos[item_name] ?: 'NaN'
            csv << [item_name, value]
            if (item_name == 'SDRRVersion') {
                value = "'${value}'"
            }
            res["fwver.${item_name}"] = value
        }

        res["fwver"] = res["fwver.SystemBIOS"]
        test_item.devices(csv, headers)
        test_item.results(res)
    }

    def nic(test_item) {
        def lines = exec('nic') {
            def url = "${http_type}://${ip}/redfish/v1/Systems/${serial_no}/Oem/${label_fujitsu}/FirmwareInventory/NIC"
            JSONObject result = webb
                                .get(url)
                                .header("Content-Type", "application/json")
                                .useCaches(false)
                                .ensureSuccess()
                                .asJsonObject()
                                .getBody();
            def content = JsonOutput.prettyPrint(result.toString())
            new File("${local_dir}/nic").text = content
            return content
        }
        // println lines
        def jsonSlurper = new JsonSlurper()
        def nic_infos = jsonSlurper.parseText(lines)
        def headers = ['BiosVersion', 'MacAddress', 'ModuleName', 'SpeedMbps']
        def mac_infos = [:].withDefault{[]}
        def csv = []
        def row = 0
        def res = [:]
        nic_infos['Ports'].each { port ->
            row ++
            def columns = []
            headers.each {
                columns.add(port[it] ?: 'NaN')
            }
            csv << columns
            mac_infos[port['ModuleName']] << port['MacAddress']
            add_new_metric("nic.module.${row}", "[${row}] モジュール", port['ModuleName'], res)
            add_new_metric("nic.adapter.${row}", "[${row}] アダプター", port['AdapterName'], res)
            add_new_metric("nic.mac.${row}", "[${row}] MAC", port['MacAddress'], res)
        }
        test_item.devices(csv, headers)
        res['nic_ip'] = ip
        res['nic'] = "${csv.size} network modules"
        test_item.results(res)
        test_item.verify_text_search('nic_ip', ip)
    }

    def ntp0(test_item) {
        def lines = exec('ntp0') {
            // v1/Managers/iRMC/Oem/ts_fujitsu/iRMCConfiguration/Time/NtpServers
            def url = "${http_type}://${ip}/redfish/v1/Managers/iRMC/Oem/${label_fujitsu}/iRMCConfiguration/Time/NtpServers/0"
            JSONObject result = webb
                                .get(url)
                                .header("Content-Type", "application/json")
                                .useCaches(false)
                                .ensureSuccess()
                                .asJsonObject()
                                .getBody();
            def content = JsonOutput.prettyPrint(result.toString())
            new File("${local_dir}/ntp0").text = content
            return content
        }
        def jsonSlurper = new JsonSlurper()
        def ntp_infos = jsonSlurper.parseText(lines)
        def ntp_server = ntp_infos?.'NtpServerName' ?: 'unkown'
        test_item.results(ntp_server)
    }

    def ntp1(test_item) {
        def lines = exec('ntp1') {
            // v1/Managers/iRMC/Oem/ts_fujitsu/iRMCConfiguration/Time/NtpServers
            def url = "${http_type}://${ip}/redfish/v1/Managers/iRMC/Oem/${label_fujitsu}/iRMCConfiguration/Time/NtpServers/1"
            JSONObject result = webb
                                .get(url)
                                .header("Content-Type", "application/json")
                                .useCaches(false)
                                .ensureSuccess()
                                .asJsonObject()
                                .getBody();
            def content = JsonOutput.prettyPrint(result.toString())
            new File("${local_dir}/ntp1").text = content
            return content
        }
        def jsonSlurper = new JsonSlurper()
        def ntp_infos = jsonSlurper.parseText(lines)
        def ntp_server = ntp_infos?.'NtpServerName' ?: 'unkown'
        test_item.results(ntp_server)
    }

    def report_xml(test_item) {
        def lines = exec('report_xml') {
            def url = "${http_type}://${ip}/report.xml"
            def result = webb.get(url).asString().getBody();
            new File("${local_dir}/report_xml").text = result
            return result
        }
        def isok = (lines.size() > 0) ? 'OK':'NG'
        test_item.results(isok)
    }

    def network(test_item) {
        def lines = exec('network') {
            def url = "${http_type}://${ip}/redfish/v1/Managers/iRMC/EthernetInterfaces/0"
            JSONObject result = webb
                                .get(url)
                                .header("Content-Type", "application/json")
                                .useCaches(false)
                                .ensureSuccess()
                                .asJsonObject()
                                .getBody();
            def content = JsonOutput.prettyPrint(result.toString())
            new File("${local_dir}/network").text = content
            return content
        }
        def jsonSlurper = new JsonSlurper()
        def network_infos = jsonSlurper.parseText(lines)
        def headers = ['Address', 'SubnetMask', 'Gateway', 'AddressOrigin']
        def aliases = ['IP', 'Mask', 'GW', 'Mode']
        def infos = []
        def csv = []
        def rows = 0
        def res = [:]
        def ip_addresses = [:]
        network_infos['IPv4Addresses'].each { port ->
            rows ++
            def columns = []
            def info = [:]
            def cols = 0
            headers.each {
                def value = port[it] ?: 'NaN'
                columns.add(value)
                info[aliases[cols]] = value
                cols ++
                if (it == 'Address' && value != '127.0.0.1') {
                    test_item.admin_port_list(ip, "iRMC-network${rows}")
// PORT : [Address:10.40.6.11, AddressOrigin:Static, Gateway:null, SubnetMask:255.255.255.0]
                    add_new_metric("network.ip.${rows}",     "[${rows}] IP ", port['Address'], res)
                    add_new_metric("network.type.${rows}",   "[${rows}] モード", port['AddressOrigin'], res)
                    add_new_metric("network.gw.${rows}",     "[${rows}] ゲートウェイ", port['Gateway'] ?: 'NaN', res)
                    add_new_metric("network.subnet.${rows}", "[${rows}] サブネット", port['SubnetMask'], res)
                    ip_addresses[value] = 1
                }
            }
            csv << columns
            infos << info
        }
        test_item.devices(csv, headers)
        res['network'] = "${ip_addresses}"
        test_item.results(res)
        test_item.verify_text_search('network_ip', "${ip_addresses}")
    }

    def disk(test_item) {
        def lines = exec('disk') {
            def profile_url = "${http_type}://${ip}/rest/v1/Oem/eLCM/ProfileManagement"
            def session_id
            def result1
            (1..10).find { wait_count ->
                def url = "${profile_url}/get?PARAM_PATH=Server/HWConfigurationIrmc/Adapters/RAIDAdapter"
                result1 = webb.post(url)
                        .asJsonObject()
                        .getBody();
                if (result1) {
                    session_id = result1?.'Session'?.'Id'
                    return true
                }
                sleep(5 * 1000)
            }
            if (session_id == null) {
                log.info "Profile create session failed, skip : ${url}"
                return
            }
            log.info "Create profile sesson : ${session_id}."
            log.info "Wait ${profile_wait} seconds to create a Disk Profile ..."
            def session_status = 'running'
            def elapse = 0
            (1..100).find { wait_count ->
                def result2 = webb
                        .get("${http_type}://${ip}/sessionInformation/${session_id}/status")
                        .header("Content-Type", "application/json")
                        .useCaches(false)
                        .asJsonObject()
                        .getBody();
                session_status = result2?.'Session'?.'Status'
                if (session_status.toLowerCase() != 'running')
                    return true;
                sleep(5 * 1000)
                elapse += 5
                if (elapse > profile_wait)
                    return true;
            }
            if (session_status == 'running') {
                log.info "Create profile timeout."
                return
            }
            JSONObject result = webb
                    .get("${profile_url}/RAIDAdapter")
                    .asJsonObject()
                    .getBody();
            def content = JsonOutput.prettyPrint(result.toString())
            new File("${local_dir}/disk").text = content
            def result3 = webb.delete("${profile_url}/RAIDAdapter").asVoid();
            def result4 = webb.delete("${http_type}://${ip}/sessionInformation/${session_id}/remove").asVoid();
 
            return content
        }
        // println lines
        def results = [:]
        def json = new JsonSlurper().parseText(lines)
        def raid_infos = json["Server"]["HWConfigurationIrmc"]["Adapters"]["RAIDAdapter"][0]
        raid_infos.each { item, value ->
            (item=~/^(.+)Rate$/).each { m0, m1 ->
                results["RAID.rate.${m1}"] = value
            }
            (item=~/^Enable(.+)$/).each { m0, m1 ->
                results["RAID.${m1}"] = value
            }

        }
        results["RAID.AutoRebuild"] = raid_infos["AutoRebuild"]
        raid_infos['Features'].each { item, value ->
            (item=~/^(.+)Mode$/).each { m0, m1 ->
                results["RAID.mode.${m1}"] = value
            }
        }
        def physical_disks = [:]
        raid_infos['PhysicalDisks']['PhysicalDisk'].each { disk ->
            def disk_number = disk['@Number']
            def size_label = size_info(disk['Size'])
            disk['Size'] = size_label
            disk.remove('@Action')
            disk.remove('@Number')
            physical_disks[disk_number] = "${disk}"
        }
        def logical_disks = [:]
        def disk_summary  = []
        raid_infos['LogicalDrives']['LogicalDrive'].each { disk ->
            def disk_number = disk['@Number']
            def size_label = size_info(disk['Size'])
            disk['Size'] = size_label
            def stripe_label = size_info(disk['Stripe'])
            disk['Stripe'] = stripe_label
            disk.remove('@Action')
            disk.remove('@Number')
            disk.remove('ArrayRefs')
            logical_disks[disk_number] = "${disk}"
            disk_summary << "RAID${disk['RaidLevel']}(Stripe:${disk['Stripe']},Size:${disk['Size']})"
            add_new_metric("disk.raid.${disk_number}", "[${disk_number}] RAIDレベル", "'" + disk['RaidLevel'] + "'", results)
            add_new_metric("disk.size.${disk_number}", "[${disk_number}] 論理ディスク容量", size_label, results)
            add_new_metric("disk.stripe.${disk_number}", "[${disk_number}] ストライピングサイズ", stripe_label, results)
        }
        results['disk'] = "${disk_summary}"

        def headers = ['name', 'value']
        def csv = []
        raid_infos['Arrays']['Array'].each { disk ->
            csv << ['drive', logical_disks[disk['@Number']]]
            disk['PhysicalDiskRefs']['PhysicalDiskRef'].each {
                def id = it['@Number']
                csv << ['drive_physical', physical_disks[id]]
                add_new_metric("disk.drive.${id}", "[${id}] ドライブ構成", physical_disks[id], results)
            }
        }
        test_item.devices(csv, headers)
        test_item.results(results)
    }

    def snmp(test_item) {
        def lines = exec('snmp') {
            def profile_url = "${http_type}://${ip}/rest/v1/Oem/eLCM/ProfileManagement"
            def session_id
            def result1
            (1..10).find { wait_count ->
                def url = "${profile_url}/get?PARAM_PATH=Server/SystemConfig/IrmcConfig/NetworkServices"
                result1 = webb.post(url)
                        .asJsonObject()
                        .getBody();
                if (result1) {
                    session_id = result1?.'Session'?.'Id'
                    return true
                }
                sleep(5 * 1000)
            }
            if (session_id == null) {
                log.info "Profile create session failed, skip : ${result1}"
                return
            }
            log.info "Create profile sesson : ${session_id}."
            log.info "Wait ${profile_wait} seconds to create a SNMP Profile ..."
            def session_status = 'running'
            def elapse = 0
            (1..100).find { wait_count ->
                def result2 = webb
                        .get("${http_type}://${ip}/sessionInformation/${session_id}/status")
                        .header("Content-Type", "application/json")
                        .useCaches(false)
                        .asJsonObject()
                        .getBody();
                session_status = result2?.'Session'?.'Status'
                if (session_status.toLowerCase() != 'running')
                    return true;
                sleep(5 * 1000)
                elapse += 5
                if (elapse > profile_wait)
                    return true;
            }
            if (session_status == 'running') {
                log.info "Create profile timeout."
                return
            }
            JSONObject result = webb
                    .get("${profile_url}/NetworkServices")
                    .asJsonObject()
                    .getBody();
            def content = JsonOutput.prettyPrint(result.toString())
            new File("${local_dir}/snmp").text = content
            def result3 = webb.delete("${profile_url}/NetworkServices").asVoid();
            def result4 = webb.delete("${http_type}://${ip}/sessionInformation/${session_id}/remove").asVoid();
 
            return content
        }
        def jsonSlurper = new JsonSlurper()
        def snmp_infos = jsonSlurper.parseText(lines)

        def results = [:]
        def headers = ['name', 'value']
        def csv = []
        def infos = snmp_infos["Server"]["SystemConfig"]["IrmcConfig"]["NetworkServices"]
        infos['Proxy'].with {
            if (it['Authentication'])
                it['Authentication'].remove('Password')
            it.each { item, value ->
                results["proxy_info.${item}"] = "${value}"
            }
        }
        infos['IpmiOverLanEnabled'].with {
            results['ipmi_over_lan_enabled'] = "${it}"
        }
        infos['Cim'].with {
            results['cim'] = "${it}"
        }
        infos['Snmp'].with {
            def trap_dests = []
            it['TrapDestinations']['TrapDestination'].each {
                if (it['Name'])
                    trap_dests << "${it['Name']} ${it['Protocol']}"
            }
            it.remove('TrapDestinations')
            results['snmp_dest'] = "${trap_dests}"
            it.each { item,value ->
                if (item == 'ServicePort') {
                    value = "'${value}'"
                }
                results["Snmp.${item}"] = "${value}"
            }
        }
        infos['Tls'].each { item,value ->
            (item=~/^(.+)Enabled$/).each { m0, m1 ->
                results["ssl_info.${m1}"] = "${value}"
            }
        }
        infos['Text'].each { item,value ->
            results["net.${item}"] = "$value"
        }
        def json_results = JsonOutput.toJson(results)
        // println "results:${JsonOutput.prettyPrint(json_results)}"
        test_item.results(results)
        test_item.verify_text_search('Snmp.Enabled',           results['Snmp.Enabled'])
        test_item.verify_text_search('Snmp.CommunityName',     results['Snmp.CommunityName'])
        test_item.verify_text_search('Snmp.TrapCommunityName', results['Snmp.TrapCommunityName'])
        test_item.verify_text_search('Snmp.ServicePort',       results['Snmp.ServicePort'])
        test_item.verify_text_search('snmp_dest',              results['snmp_dest'])
    }
}

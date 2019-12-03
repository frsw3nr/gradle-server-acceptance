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
class iLOSpecBase extends InfraTestSpec {

    String ip
    String os_user
    String os_password
    // String script_path
    String url
    int    timeout
    Webb webb          = null
    def  http_type     = 'https'

    class TrustingHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
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

    def init() {
        super.init()

        def os_account   = test_platform.os_account
        this.ip          = test_platform.test_target.ip ?: 'unkown'
        this.os_user     = os_account['user']
        this.os_password = os_account['password']
        // this.script_path = local_dir + '/get_hpilo_spec.ps1'
        this.timeout     = test_platform.timeout

        // this.ip          = test_server.ip
        // def os_account   = test_server.os_account
        // this.os_user     = os_account['user']
        // this.os_password = os_account['password']
        // this.script_path = local_dir + '/get_hpilo_spec.ps1'
        // this.timeout     = test_server.timeout
    }

    def setup_exec(TestItem[] test_items) {
        // super.setup_exec()

        // def cmd = """\
        //     |powershell -NonInteractive ${script_path}
        //     |-log_dir '${local_dir}'
        //     |-ip '${ip}' -server '${server_name}'
        //     |-user '${os_user}' -password '${os_password}'
        // """.stripMargin()

        // runPowerShellTest('lib/template', 'iLO', cmd, test_items)
        def credentials = os_user + ":" + os_password
        String encoded = credentials.bytes.encodeBase64().toString()

        String auth = "Basic " + encoded
        webb = Webb.create();
        allow_all_https_protocol(webb);
        webb.setDefaultHeader(Webb.HDR_AUTHORIZATION, auth);

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

    def trim(String value) {
        return value.replaceAll(/\A[\s　]+/,"").replaceAll(/[\s　]+\z/,"")
    }

    def rest_get(String test_name, String url) {
        JSONObject result = webb
                            .get(url)
                            .header("Content-Type", "application/json")
                            .useCaches(false)
                            .ensureSuccess()
                            .asJsonObject()
                            .getBody();

        def content = JsonOutput.prettyPrint(result.toString())
        new File("${local_dir}/${test_name}").text = content
        return content
    }

    def overview(TestItem test_item) {
        def lines = exec('overview') {
            return this.rest_get('overview', "https://${ip}/json/overview")
        }
        def jsonSlurper = new JsonSlurper()
        def overviews = jsonSlurper.parseText(lines)
        def res = [:]
        def metrics = [
            'product_name'   : '機種',
            'product_id'     : '型番',
            'serial_num'     : 'S/N',
            'license'        : 'ライセンスタイプ',
            'ilo_fw_version' : 'FWバージョン',
            'isUEFI'         : 'UEFI有効化',
            'system_rom'     : 'システムロムバージョン',
            'ip_address'     : '管理IP',
            'system_health'  : 'システム状態',
        ]
        def aliases = [
            'product_name'   : 'HSI_SPN',
            'product_id'     : 'HSI_PRODUCTID',
            'serial_num'     : 'HSI_SBSN',
            'ip_address'     : 'ip_mng',
        ]
        metrics.each { item, description ->
            def value = overviews?."$item" ?: 'N/A'
            add_new_metric("overview.${item}", description, value, res)
            if (aliases.containsKey(item)) {
                res[aliases[item]] = value
            }
        }
        res['overview'] = overviews?.self_test ?: 'N/A'
        println metrics.ip_address
        test_item.admin_port_list(metrics.ip_address, "iLO-network")
        test_item.results(res)
    }

    def License(TestItem test_item) {
        def lines = exec('License') {
            def uri = 'redfish/v1/Managers/1/LicenseService/1/'
            return this.rest_get('License', "https://${ip}/${uri}")
        }
        def jsonSlurper = new JsonSlurper()
        def lic = jsonSlurper.parseText(lines)
        def res = lic?.ConfirmationRequest?.EON?.LicenseKey ?: 'N/A'
        test_item.devices([[res]], ['license'])

        test_item.results(res)
    }

    def proc_info(TestItem test_item) {
        def lines = exec('proc_info') {
            return this.rest_get('proc_info', "https://${ip}/json/proc_info")
        }
        def jsonSlurper = new JsonSlurper()
        def proc_infos = jsonSlurper.parseText(lines)
        def res = [:].withDefault{0}

        def headers = ['proc_socket', 'proc_name', 'proc_num_cores', 'proc_num_threads', 'proc_status', 'proc_num_l1cache', 'proc_num_l2cache', 'proc_num_l3cache']
        def titles = ['proc_name', 'proc_num_cores', 'proc_num_threads']
        def csv = []

        proc_infos?.processors.each { processor ->
            def row = []
            def cpu_titles = []
            headers.each { header ->
                def value = processor?."$header" ?: 'N/A'
                row << value
                if (header in titles) {
                    cpu_titles << value
                } 
            }
            def cpu_title = cpu_titles.join(' / ')
            res[cpu_title] ++
            csv << row
        }
        test_item.devices(csv, headers)
        test_item.results(res.toString())
    }

    def mem_info(TestItem test_item) {
        def lines = exec('mem_info') {
            return this.rest_get('mem_info', "https://${ip}/json/mem_info")
        }
        def jsonSlurper = new JsonSlurper()
        def mem_infos = jsonSlurper.parseText(lines)
        def res = [:]
        def metrics = [
            'mem_total_mem_size' : 'メモリ容量',
            'mem_op_speed'       : 'メモリスピード',
            'mem_condition'      : 'メモリ状態',
        ]
        def memory_size_gb = 0
        metrics.each { item, description ->
            def value = mem_infos?."$item" ?: 'N/A'
            add_new_metric("mem_info.${item}", description, value, res)
            if (item == 'mem_total_mem_size') {
                memory_size_gb = value / 1024
            }
        }
        res['mem_info'] = memory_size_gb
        test_item.results(res)
    }

    def network(TestItem test_item) {
        def lines = exec('network') {
            def uri = 'redfish/v1/Managers/1/EthernetInterfaces/1'
            return this.rest_get('network', "https://${ip}/${uri}")
        }

        def jsonSlurper = new JsonSlurper()
        def networks = jsonSlurper.parseText(lines)
        def res = [:]
        def summary = [:]

        networks?.IPv4Addresses.with { ipv4 ->
            add_new_metric("network.ipv4.address", "IPv4アドレス", ipv4?.'Address', res)
            add_new_metric("network.ipv4.gateway", "IPv4ゲートウェイ", ipv4?.'Gateway', res)
            add_new_metric("network.ipv4.subnet",  "IPv4サブネット", ipv4?.'SubnetMask', res)
            add_new_metric("network.ipv4.origin",  "IPv4 Origin", ipv4?.'AddressOrigin', res)
            summary['GW']     = ipv4?.'Gateway'
            summary['Subnet'] = ipv4?.'SubnetMask'
            summary['Origin'] = ipv4?.'AddressOrigin'
        }
        networks?.IPv6Addresses.with { ipv6 ->
            add_new_metric("network.ipv6.address", "IPv6アドレス", ipv6?.'Address', res)
        }
        add_new_metric("network.autoneg", "自動ネゴシエーション", networks?.'AutoNeg', res)
        add_new_metric("network.mac", "MACアドレス", networks?.'MACAddress', res)
        res['network'] = summary.toString()
        test_item.results(res)
    }

    def Storage(TestItem test_item) {
        def lines_drives = exec('health_drives') {
            return this.rest_get('health_drives', "https://${ip}/json/health_drives")
        }
        def jsonSlurper = new JsonSlurper()
        def drives = jsonSlurper.parseText(lines_drives)

        def res = [:]
        def summary = []
        def csv = []
        drives?.'log_drive_arrays'.each { drive_array ->
            def total_memory = drive_array?.'accel_tot_mem' ?: 'N/A'
            def model = drive_array?.'model' ?: 'N/A'
            def serial_no = drive_array?.'serial_no' ?: 'N/A'
            def status = drive_array?.'status' ?: 'N/A'
            def lu = 0
            drive_array?.'logical_drives'.each { logical_drive ->
                lu ++
                def raid = logical_drive?.'flt_tol' ?: 'N/A'
                def lu_name = logical_drive?.'name' ?: 'N/A'
                def lu_size = logical_drive?.'capacity' ?: 'N/A'
                def drive_number = logical_drive?.'physical_drives'?.size()
                csv << [lu_name, model, serial_no, raid, drive_number, status, lu_size]
                add_new_metric("Storage.model.${lu}", "ストレージ[$lu]", model, res)
                add_new_metric("Storage.raid.${lu}", "ストレージ[$lu] RAID", raid, res)
                add_new_metric("Storage.drive_number.${lu}", "ストレージ[$lu] ディスク本数", drive_number, res)
                add_new_metric("Storage.capacity.${lu}", "ストレージ[$lu] 容量", lu_size, res)
                add_new_metric("Storage.status.${lu}", "ストレージ[$lu] 状態", status, res)
                summary << raid
                summary << lu_size
            }
        }
        def headers = ['lu', 'model', 'serial', 'raid', 'drive_number', 'status', 'capacity']
        test_item.devices(csv, headers)
        res['Storage'] = summary.toString()
        test_item.results(res)
    }

    def drive(TestItem test_item) {
        def lines_phy_drives = exec('health_phy_drives') {
            return this.rest_get('health_phy_drives', "https://${ip}/json/health_phy_drives")
        }
        def jsonSlurper = new JsonSlurper()
        def phy_drives = jsonSlurper.parseText(lines_phy_drives)

        def summary = [:].withDefault{0}
        def res = [:]
        def csv = []
        phy_drives?.'phy_drive_arrays'.each { drive_array ->
            def id = 0
            drive_array?.'physical_drives'.each { physical_drive ->
                id ++
                def type = physical_drive?.'drive_mediatype' ?: 'N/A'
                def bay  = physical_drive?.'name' ?: 'N/A'
                def model = physical_drive?.'model' ?: 'N/A'
                def serial = physical_drive?.'serial_no' ?: 'N/A'
                def status = physical_drive?.'status' ?: 'N/A'
                def capacity = physical_drive?.'capacity' ?: 'N/A'
                csv << [id, type, bay, model, serial, status, capacity]
                def label = "${type}:${capacity}"
                summary[label] ++
                add_new_metric("drive.${id}", "ディスク[$id]", label, res)
            }
        }
        def headers = ['id', 'type', 'bay', 'model', 'serial', 'status', 'capacity']
        test_item.devices(csv, headers)
        res['drive'] = summary.toString()
        test_item.results(res)
    }

    def snmp(TestItem test_item) {
        def lines = exec('snmp') {
            def url_suffix = "redfish/v1/Managers/1/snmpservice/snmpalertdestinations/1"
            return this.rest_get('snmp', "https://${ip}/${url_suffix}")
        }
        def jsonSlurper = new JsonSlurper()
        def snmps = jsonSlurper.parseText(lines)
        def res = [:]
        def summary = []
        def metrics = [
            'AlertDestination'  : 'TRAP 送信先',
            'TrapCommunity'     : 'コミュニティ',
            'SNMPAlertProtocol' : 'バージョン',
        ]
        metrics.each { item, description ->
            def value = snmps?."$item" ?: 'N/A'
            add_new_metric("snmp.${item}", description, value, res)
            if (value != 'N/A') {
                summary << value
            }
        }
        res['snmp'] = (summary.size() == 0) ? 'NotConfigured' : summary.toString()
        test_item.results(res)
    }

    def power_regulator(TestItem test_item) {
        def lines = exec('power_regulator') {
            return this.rest_get('power_regulator', "https://${ip}/json/power_regulator")
        }
        def jsonSlurper = new JsonSlurper()
        def power_regulators = jsonSlurper.parseText(lines)
        def value = power_regulators?.'prmode' ?: 'N/A'
        test_item.results(value)
    }

    def power_summary(TestItem test_item) {
        def lines = exec('power_summary') {
            return this.rest_get('power_summary', "https://${ip}/json/power_summary")
        }
        def jsonSlurper = new JsonSlurper()
        def power_summarys = jsonSlurper.parseText(lines)
        def summary = []
        def res = [:]
        def metrics = [
            'volts'                 : '電圧',
            'max_measured_wattage'  : '最大ワット',
            'power_cap_mode'        : '電源容量モード',
        ]
        metrics.each { item, description ->
            def value = power_summarys?."$item" ?: 'N/A'
            add_new_metric("power_summary.${item}", description, value, res)
            if (value != 'N/A') {
                summary << value
            }
        }
        // res['mem_info'] = memory_size_gb
        res['power_summary'] = (summary.size() == 0) ? 'NotConfigured' : summary.toString()
        test_item.results(res)
    }

}

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
class HitachVSP2Spec extends LinuxSpecBase {

    class TrustingHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    String ip
    String os_user
    String os_password
    String url
    int  timeout       = 30
    int  profile_wait  = 120
    def  serial_no     = null
    Webb webb          = null
    def  http_type     = 'http'
    String session_id
    String storage_id
    String token

    def init() {
        super.init()

        def os_account     = test_platform.os_account
        this.ip            = test_platform.test_target.ip ?: 'unkown'
        this.os_user       = os_account['user']
        this.os_password   = os_account['password']
        this.timeout     = test_platform.timeout
    }

    def finish() {
        super.finish()
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

    JSONObject http_rest_get(String rest_url, String body = null) {
        if (this.token) {
            return webb
                    .get("${http_type}://${ip}/${rest_url}")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Session ${this.token}")
                    .useCaches(false)
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
        }else {
            return webb
                    .get("${http_type}://${ip}/${rest_url}")
                    .header("Content-Type", "application/json")
                    .useCaches(false)
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
        }
    }

    JSONObject http_rest_get_token() {
        JSONObject postObj = new JSONObject()
        postObj.put('aliveTime', 30)
        return webb
                .post("${http_type}://${ip}/ConfigurationManager/v1/objects/sessions/")
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .body(postObj)
                .useCaches(false)
                .ensureSuccess()
                .asJsonObject()
                .getBody();
    }

    JSONObject http_rest_delete_session() {
        return webb
                .delete("${http_type}://${ip}/ConfigurationManager/v1/objects/storages/${this.storage_id}/sessions/${this.session_id}")
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .header("Authorization", "Session ${this.token}")
                .useCaches(false)
                .ensureSuccess()
    }

    def setup_exec(TestItem[] test_items) {

        def credentials = os_user + ":" + os_password
        String encoded = credentials.bytes.encodeBase64().toString()

        String auth = "Basic " + encoded
        webb = Webb.create();

        webb.setDefaultHeader(Webb.HDR_AUTHORIZATION, auth);
        // }

        if (!dry_run) {
            // ストレージIDを取得
            def result = http_rest_get('ConfigurationManager/v1/objects/storages')
            this.storage_id = result?."data"?.get(0)?."storageDeviceId"
            if (!this.storage_id) {
                log.error "Not found storage_id"
                return
            }
            // トークンを取得
            log.info "STORAGE_ID:${this.storage_id}"
            def result2 = http_rest_get_token()

            def content2 = JsonOutput.prettyPrint(result2.toString())
            this.token = result2?."token"
            this.session_id = result2?."sessionId"
            if (!this.token) {
                log.error "Auth error, Token not found : ${this.storage_id}"
                return
            }
            log.info "TOKEN:${this.token}"
            log.info "SESSION:${this.session_id}"
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
        if (!dry_run) {
            http_rest_delete_session()
        }
    }

    def health_status(test_item) {
        def lines = exec('health_status') {
            def url = "ConfigurationManager/simple/v1/objects/health-status"
            def result = http_rest_get(url)
            def content = JsonOutput.prettyPrint(result.toString())
            new File("${local_dir}/health_status").text = content
            return content
        }
        def json = new JsonSlurper().parseText(lines)
        def res = [:]
        def statuses = [:]
        ["poolStatus" : "ストレージプール状態", 
         "snapshotStatus" : "スナップショット状態"].each { 
            item_name, definition ->
            def value = json[item_name] ?: 'NaN'
            add_new_metric("health_status.${item_name}", definition, value, res)
            statuses[value] = 1
        }
        String summary = statuses.keySet().toString()
        res['health_status'] = summary
        test_item.results(res)
    }

    def storage(test_item) {
        def lines = exec('storage') {
            def url = "ConfigurationManager/simple/v1/objects/storage"
            def result = http_rest_get(url)
            def content = JsonOutput.prettyPrint(result.toString())
            new File("${local_dir}/storage").text = content
            return content
        }
        def json = new JsonSlurper().parseText(lines)
        // println json
        def res = [:]
        def networks = []
        [
            "modelName" : "モデル",
            "serial" : "S/N",
            "gumVersion" :  "GUMバージョン",
            "dkcMicroVersion" : "マイクロコードバージョン",
            "warningLedStatus" :    "障害ランプ状態",
            "numberOfTotalVolumes" : "ボリューム数",
            // "numberOfFreeDrives" : "空きドライブ数",
            // "numberOfTotalServers" : "サーバ数",
            "totalPhysicalCapacity" : "物理ディスク総容量（MiB）",
            "totalPoolCapacity" : "プールの総容量（MiB）",
            "usedPoolCapacity" : "プールの使用量（MiB）",
            // "freePoolCapacity" : "プールの空き容量（MiB）",
            "totalPoolCapacityWithTiPool" : "ストレージ内プールの総容量（MiB）",
            "usedPoolCapacityWithTiPool" : "ストレージ内プールの使用量（MiB）",
            // "freePoolCapacityWithTiPool" : "ストレージ内プールの空き容量（MiB）",
            "ipAddressIpv4Ctl1" :   "IPアドレス(IPv4) コントローラ1",
            "ipAddressIpv4Ctl2" :   "IPアドレス(IPv4) コントローラ2",
            "ipAddressIpv6Ctl1" :   "IPアドレス(IPv6) コントローラ1",
            "ipAddressIpv6Ctl2" :   "IPアドレス(IPv6) コントローラ2",
        ].each { 
            item_name, definition ->
            def value = json[item_name] ?: 'NaN'
            if (item_name == "serial")
                value = "'$value'"
            if (item_name =~ /ipAddressIpv4.*/) {
                networks << value
                test_item.lookuped_port_list(value, item_name)
            }

            add_new_metric("storage.${item_name}", definition, value, res)
        }
        res['storage.network'] = networks.toString()
        String summary = "障害ランプ : ${res['storage.warningLedStatus']}"
        res['storage'] = summary

        test_item.results(res)
    }

    def parity_groups(test_item) {
        def lines = exec('parity_groups') {
            def url = "ConfigurationManager/v1/objects/storages/${storage_id}/parity-groups"
            def result = http_rest_get(url)
            def content = JsonOutput.prettyPrint(result.toString())
            // println content
            new File("${local_dir}/parity_groups").text = content
            return content
        }
        def jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parseText(lines)
        def headers = ["clprId", "parityGroupId", "numOfLdevs", "usedCapacityRate", 
            "isAcceleratedCompressionEnabled", "physicalCapacity", 
            "availableVolumeCapacity", "driveType", "driveTypeName", 
            "totalCapacity", "raidLevel", "raidType"]
        def csv = []
        def res = [:]

        json?."data"?.each { group_info ->
            def row = []
            headers.each { item_name ->
                def value = group_info[item_name]
                row << value
            }
            csv << row
        }
        res["parity_groups"] = "${csv.size()} groups"
        test_item.devices(csv, headers)
        test_item.results(res)
    }

    def ldevs(test_item) {
        def lines = exec('ldevs') {
            def url = "ConfigurationManager/v1/objects/storages/${storage_id}/ldevs?headLdevId=0&count=1000"
            def result = http_rest_get(url)
            def content = JsonOutput.prettyPrint(result.toString())
            // println content
            new File("${local_dir}/ldevs").text = content
            return content
        }
        def jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parseText(lines)
        def headers = ["mpBladeId", "driveType", "raidLevel", "raidType", "status", 
            "emulationType", "ssid", "clprId", "composingPoolId", "numOfParityGroups", 
            "byteFormatCapacity", "driveByteFormatCapacity", "driveBlockCapacity", 
            "blockCapacity"] 
        def csv = []
        def res = [:]
        json?."data"?.each { ldev_info ->
            if (ldev_info['emulationType'] != 'NOT DEFINED') {
                def row = []
                headers.each { item_name ->
                    def value = ldev_info[item_name]
                    row << value
                }
                csv << row
            }
        }
        res["ldevs"] = "${csv.size()} volumes"
        test_item.devices(csv, headers)
        test_item.results(res)
    }

    def users(test_item) {
        def lines = exec('users') {
            def url = "ConfigurationManager/v1/objects/storages/${storage_id}/users"
            def result = http_rest_get(url)
            def content = JsonOutput.prettyPrint(result.toString())
            new File("${local_dir}/users").text = content
            return content
        }
        def jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parseText(lines)
        def headers = ["userId", "userGroupNames", "isAccountStatus", "isBuiltIn", 
                       "authentication"] 
        def csv = []
        def res = [:]
        json?."data"?.each { ldev_info ->
            def row = []
            headers.each { item_name ->
                def value = ldev_info[item_name]
                row << value
            }
            csv << row
        }
        res["users"] = "${csv.size()} users"
        test_item.devices(csv, headers)
        test_item.results(res)
    }

    def snmp(test_item) {
        def lines = exec('snmp') {
            def url = "ConfigurationManager/v1/objects/storages/${storage_id}/snmp-settings/instance"
            def result = http_rest_get(url)
            def content = JsonOutput.prettyPrint(result.toString())
            new File("${local_dir}/snmp").text = content
            return content
        }
        def json = new JsonSlurper().parseText(lines)
        def res = [:]
        [
            "snmpVersion" : "SNMP バージョン",
            "isSNMPAgentEnabled" : "SNMP 有効化",
            "sendingTrapSetting" :  "SNMP トラップ設定",
            "systemGroupInformation" : "SNMP 情報設定",
            "requestAuthenticationSetting" : "SNMP 認証設定",
        ].each { 
            item_name, definition ->
            def value = json[item_name] ?: 'NaN'
            if (item_name == "serial")
                value = "'$value'"
            add_new_metric("snmp.${item_name}", definition, value, res)
        }
        String summary = "SNMP有効化 : ${res['snmp.isSNMPAgentEnabled']}"
        res['snmp'] = summary

        test_item.results(res)
    }
}

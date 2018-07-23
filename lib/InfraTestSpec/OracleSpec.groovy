package InfraTestSpec

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
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
@InheritConstructors
class OracleSpec extends InfraTestSpec {

    String oracle_ip
    String oracle_user
    String oracle_password
    String target_server
    String url
    String token
    int    timeout = 30
    def host_ids = [:]
    def hostnames = [:]

    def init() {
        super.init()

        def os_account       = test_platform.os_account
        this.oracle_ip       = os_account['server']
        this.oracle_user     = os_account['user']
        this.oracle_password = os_account['password']
        this.target_server   = test_platform.test_target.name
        this.timeout         = test_platform.timeout

        // println "ZABBIX_IP : ${this.zabbix_ip}"
        // println "ZABBIX_USER : ${this.zabbix_user}"
        // println "ZABBIX_PASSWORD : ${this.zabbix_password}"
        // println "TARGET_SERVER : ${this.target_server}"
        // println "TIMEOUT : ${this.timeout}"
        // def remote_account = test_server.remote_account
        // this.zabbix_ip       = remote_account['server']
        // this.zabbix_user     = remote_account['user']
        // this.zabbix_password = remote_account['password']
        // this.target_server   = test_server.server_name
        // this.timeout         = test_server.timeout
    }

    def finish() {
        super.finish()
    }

    def setup_exec(TestItem[] test_items) {

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
                    log.error "[Zabbix Test] Test method '${method.name}()' faild, skip.\n" + e
                }
            }
        }
    }

    def cdbstorage(test_item) {
        def lines = exec('cdbstorage') {


            def content = ''
            new File("${local_dir}/cdbstorage").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def dbattrs(test_item) {
        def lines = exec('dbattrs') {


            def content = ''
            new File("${local_dir}/dbattrs").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def dbcomps(test_item) {
        def lines = exec('dbcomps') {


            def content = ''
            new File("${local_dir}/dbcomps").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def dbfeatusage(test_item) {
        def lines = exec('dbfeatusage') {


            def content = ''
            new File("${local_dir}/dbfeatusage").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def dbinfo(test_item) {
        def lines = exec('dbinfo') {


            def content = ''
            new File("${local_dir}/dbinfo").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def dbvers(test_item) {
        def lines = exec('dbvers') {


            def content = ''
            new File("${local_dir}/dbvers").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def nls(test_item) {
        def lines = exec('nls') {


            def content = ''
            new File("${local_dir}/nls").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def parmdef(test_item) {
        def lines = exec('parmdef') {


            def content = ''
            new File("${local_dir}/parmdef").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def redoinfo(test_item) {
        def lines = exec('redoinfo') {


            def content = ''
            new File("${local_dir}/redoinfo").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def sgasize(test_item) {
        def lines = exec('sgasize') {


            def content = ''
            new File("${local_dir}/sgasize").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def sysmetric(test_item) {
        def lines = exec('sysmetric') {


            def content = ''
            new File("${local_dir}/sysmetric").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def systime(test_item) {
        def lines = exec('systime') {


            def content = ''
            new File("${local_dir}/systime").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def tabstorage(test_item) {
        def lines = exec('tabstorage') {


            def content = ''
            new File("${local_dir}/tabstorage").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }
}

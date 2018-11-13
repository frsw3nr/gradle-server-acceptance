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
class ZabbixServerSpec extends InfraTestSpec {

    static final def zabbix_labels = [
        'status' : [
            '0' : 'Monitored',
            '1' : 'Unmonitored',
        ],
        'available' : [
            '0' : 'Unknown',
            '1' : 'Available',
            '2' : 'Unavailable',
        ],
        'trigger.status' : [
            '0' : 'Enabled',
            '1' : 'Disabled',
        ],
        'trigger.state' : [
            '0' : 'Normal',
            '1' : 'Unknown',
        ],
        'trigger.priority' : [
            '0' : 'Not classified',
            '1' : 'Information',
            '2' : 'Warning',
            '3' : 'Average',
            '4' : 'High',
            '5' : 'Disaster',
        ]
    ]

    String zabbix_ip
    String zabbix_user
    String zabbix_password
    String target_server
    String url
    String token
    int    timeout = 30
    def hostnames = [:]

    def init() {
        super.init()

        def os_account       = test_platform.os_account
        // this.zabbix_ip       = os_account['server']
        this.zabbix_ip       = test_platform.test_target.ip
        this.zabbix_user     = os_account['user']
        this.zabbix_password = os_account['password']
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

        def json = JsonOutput.toJson(
            [
                jsonrpc: "2.0",
                method: "user.login",
                params: [
                    user:     this.zabbix_user,
                    password: this.zabbix_password,
                ],
                id: "1",
            ]
        )

        Webb webb = Webb.create()
        if (!dry_run) {
            url = "http://${this.zabbix_ip}/zabbix/api_jsonrpc.php"
            JSONObject result = webb.post(url)
                                        .header("Content-Type", "application/json")
                                        .useCaches(false)
                                        .body(json)
                                        .ensureSuccess()
                                        .asJsonObject()
                                        .getBody();
            token = result.getString("result")
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
                    log.error "[Zabbix Test] Test method '${method.name}()' faild, skip.\n" + e
                }
            }
        }
    }

    def getHostGroup() {
        def lines = exec('HostGroup') {

            def json = JsonOutput.toJson(
                [
                    jsonrpc: "2.0",
                    method: "HostGroup.get",
                    params: [
                        output: "extend",
                    ],
                    id: "1",
                    auth: token,
                ]
            )
            Webb webb = Webb.create();
            JSONObject result = webb.post(url)
                                        .header("Content-Type", "application/json")
                                        .useCaches(false)
                                        .body(json)
                                        .ensureSuccess()
                                        .asJsonObject()
                                        .getBody();

            def content = result.getString("result")
            new File("${local_dir}/HostGroup").text = content
            return content
        }

        def jsonSlurper = new JsonSlurper()
        def host_groups = jsonSlurper.parseText(lines)

        def host_group_ids = [:]
        host_groups.each { host_group ->
            host_group_ids[host_group['name']] = host_group['groupid']
        }
        return host_group_ids
    }


    def apiinfo(test_item) {
        def lines = exec('apiinfo') {

            def json = JsonOutput.toJson(
                [
                    jsonrpc: "2.0",
                    method: "apiinfo.version",
                    params: [],
                    id: "1",
                    // auth: token,
                ]
            )
            Webb webb = Webb.create();
            JSONObject result = webb.post(url)
                                        .header("Content-Type", "application/json")
                                        .useCaches(false)
                                        .body(json)
                                        .ensureSuccess()
                                        .asJsonObject()
                                        .getBody();
            def content = result.getString("result")
            new File("${local_dir}/apiinfo").text = content
            return content
        }
        test_item.results(lines)
    }

    def HostGroup(test_item) {
        def lines = exec('HostGroup') {

            def json = JsonOutput.toJson(
                [
                    jsonrpc: "2.0",
                    method: "HostGroup.get",
                    params: [
                        output: "extend",
                    ],
                    id: "1",
                    auth: token,
                ]
            )
            Webb webb = Webb.create();
            JSONObject result = webb.post(url)
                                        .header("Content-Type", "application/json")
                                        .useCaches(false)
                                        .body(json)
                                        .ensureSuccess()
                                        .asJsonObject()
                                        .getBody();

            def content = result.getString("result")
            new File("${local_dir}/HostGroup").text = content
            return content
        }

        def jsonSlurper = new JsonSlurper()
        def host_groups = jsonSlurper.parseText(lines)

        def headers = ['groupid', 'name']
        def results = [:]
        def csv = []
        def name
        host_groups.each { host_group ->
            def columns = []
            headers.each {
                columns.add(host_group[it] ?: 'NaN')
            }
            results[host_group['groupid']] = host_group['name']
            csv << columns
        }
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def User(test_item) {
        def lines = exec('User') {

            def json = JsonOutput.toJson(
                [
                    jsonrpc: "2.0",
                    method: "User.get",
                    params: [
                        output: "extend",
                        selectMedias: "extend",
                        selectUsrgrps: "extend",
                    ],
                    id: "1",
                    auth: token,
                ]
            )
            Webb webb = Webb.create();
            JSONObject result = webb.post(url)
                                        .header("Content-Type", "application/json")
                                        .useCaches(false)
                                        .body(json)
                                        .ensureSuccess()
                                        .asJsonObject()
                                        .getBody();

            def content = result.getString("result")
            new File("${local_dir}/User").text = content
            return content
        }

        def jsonSlurper = new JsonSlurper()
        def users = jsonSlurper.parseText(lines)

        def headers = ['userid', 'usrgrps', 'alias', 'name', 'medias']
        def results = [:]
        def csv = []
        users.each { user ->
            def columns = []
            headers.each {
                if (it == 'usrgrps') {
                    def usrgrps = []
                    user[it].each { usrgrp ->
                        usrgrps.add(usrgrp['name'])
                    }
                    columns.add(usrgrps.toString())

                } else if (it == 'medias') {
                    def medias = []
                    user[it].each { media ->
                        medias.add(media['sendto'])
                    }
                    columns.add(medias.toString())

                } else {
                    columns.add(user[it] ?: 'NaN')
                }
            }
            results[user['userid']] = user['alias']
            csv << columns
        }
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def UserGroup(test_item) {
        def lines = exec('UserGroup') {

            def json = JsonOutput.toJson(
                [
                    jsonrpc: "2.0",
                    method: "usergroup.get",
                    params: [
                        output: "extend",
                    ],
                    id: "1",
                    auth: token,
                ]
            )
            Webb webb = Webb.create();
            JSONObject result = webb.post(url)
                                        .header("Content-Type", "application/json")
                                        .useCaches(false)
                                        .body(json)
                                        .ensureSuccess()
                                        .asJsonObject()
                                        .getBody();

            def content = result.getString("result")
            new File("${local_dir}/UserGroup").text = content
            return content
        }

        def jsonSlurper = new JsonSlurper()
        def users = jsonSlurper.parseText(lines)

        def headers = ['usrgrpid', 'name', 'gui_access', 'users_status']
        def csv = []
        def results = [:]
        users.each { user ->
            def columns = []
            headers.each {
                columns.add(user[it] ?: 'NaN')
            }
            results[user['usrgrpid']] = user['name']
            csv << columns
        }
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }

    def Action(test_item) {
        def lines = exec('Action') {

            def host_group_ids = this.getHostGroup()

            def results = new JSONObject()
            host_group_ids.each { host_group, group_id ->
                def json = JsonOutput.toJson(
                    [
                        "jsonrpc": "2.0",
                        "method": "action.get",
                        "params": [
                            "output": "extend",
                            "selectOperations": "extend",
                            "selectFilter": "extend",
                            "groupids": group_id,
                        ],
                        "auth": token,
                        "id": 1
                    ]
                )
                Webb webb = Webb.create();
                JSONObject result = webb.post(url)
                                            .header("Content-Type", "application/json")
                                            .useCaches(false)
                                            .body(json)
                                            .ensureSuccess()
                                            .asJsonObject()
                                            .getBody();
                results.put(host_group, result.get("result"))
            }
            def content = results.toString()
            // println content
            // println JsonOutput.prettyPrint(content)
            new File("${local_dir}/Action").text = content
            return content
        }

        // def jsonSlurper = new JsonSlurper()
        def action_lists = new JsonSlurper().parseText(lines)

        def headers = ['actionid', 'host_group', 'name', 'status']
        def csv = []
        def results = [:]
        action_lists.each { host_group, action_list ->
            if (action_list.size() > 0) {
                action_list.each { action ->
                    // println "Action1:\n ${action}"
                    def columns = []
                    headers.each {
                        def result = 'NaN'
                        if (it == 'host_group') {
                            result = host_group
                        } else if (it == 'status') {
                            if (action[it] == '0') {
                                result = 'Enabled'
                            } else if (action[it] == '1') {
                                result = 'Disabled'
                            }
                        } else if (action.get(it)) {
                            result = action.get(it).toString()
                        }
                        columns.add(result)
                    }
                    results[host_group] = action_list['name']
                    csv << columns
                }
            } 
        }
        test_item.devices(csv, headers)
        test_item.results(results.toString())
    }
}

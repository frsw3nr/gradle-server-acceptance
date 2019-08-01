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
        ],
        'user_group.gui_access' : [
            '0' : 'Default',
            '1' : 'Internal',
            '2' : 'Disabled',
        ],
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
            new File("${local_dir}/apiinfo").text = "'${content}'"
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
        def res     = [:]
        def csv     = []
        host_groups.each { host_group ->
            def columns = []
            headers.each {
                columns.add(host_group[it] ?: 'NaN')
            }
            def id   = host_group['groupid']
            def name = host_group['name']
            // res[id] = name
            csv << columns
            add_new_metric("HostGroup.${name}", "[${name}] ID", 
                           "'${id}'", res)
        }
        res['HostGroup'] = "${csv.size()} Host groups."
        test_item.devices(csv, headers)
        test_item.results(res)
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
        def res = [:]
        def csv = []
        users.each { user ->
            def columns = []
            def alias = user['alias']
            def id    = user['userid']
            def lang  = user['lang']
            add_new_metric("User.${alias}.id", "[${alias}] ID", "'${id}'", res)
            add_new_metric("User.${alias}.lang", "[${alias}] 言語", lang, res)
            headers.each {
                if (it == 'usrgrps') {
                    def usrgrps = []
                    user[it].each { usrgrp ->
                        usrgrps.add(usrgrp['name'])
                    }
                    columns.add("${usrgrps}")
                    add_new_metric("User.${alias}.groups", "[${alias}] グループ", 
                           "${usrgrps}", res)

                } else if (it == 'medias') {
                    def medias = []
                    user[it].each { media ->
                        medias.add(media['sendto'])
                    }
                    columns.add("${medias}")
                    add_new_metric("User.${alias}.media", "[${alias}] メディア", 
                           "${medias}", res)

                } else {
                    columns.add(user[it] ?: 'NaN')
                }
            }
            csv << columns
        }
        res['User'] = "${csv.size()} users"
        test_item.devices(csv, headers)
        test_item.results(res)
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
        def res = [:]
        users.each { user ->
            def columns = []
            headers.each {
                columns.add(user[it] ?: 'NaN')
            }
            def id     = user['usrgrpid']
            def name   = user['name']
            def gui    = zabbix_labels['user_group.gui_access'][user['gui_access']]
            def status = zabbix_labels['trigger.status'][user['users_status']]
            // res[user['usrgrpid']] = user['name']
            csv << columns
            add_new_metric("UserGroup.${name}.id", "[${name}] ID", "'${id}'", res)
            add_new_metric("UserGroup.${name}.gui", "[${name}] GUIアクセス", "'${gui}'", res)
            add_new_metric("UserGroup.${name}.status", "[${name}] ステータス", "'${status}'", res)
        }
        test_item.devices(csv, headers)
        res['UserGroup'] = "${csv.size()} user groups"
        test_item.results(res)
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
        def res = [:]
        action_lists.each { host_group, action_list ->
            if (action_list.size() > 0) {
                action_list.each { action ->
                    // println "Action1:\n ${action}\n"
                    // println "Action2:\n ${action.keySet()}\n"
                    def columns = []
                    headers.each {
                        def result = 'NaN'
                        if (it == 'host_group') {
                            result = host_group
                        } else if (it == 'status') {
                            result = zabbix_labels['trigger.status'][action[it]]
                        } else if (action.get(it)) {
                            result = action.get(it).toString()
                        }
                        columns.add(result)
                    }
                    def name = action_list['name']
                    def status = zabbix_labels['trigger.status'][action['status']]
                    add_new_metric("Action.${host_group}.name", "[${host_group}] 名前", name, res)
                    add_new_metric("Action.${host_group}.status", "[${host_group}] ステータス", status, res)

                    csv << columns
                }
            } 
        }
        test_item.devices(csv, headers)
        res['Action'] = "${csv.size()} actions"
        test_item.results(res)
    }
}

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
class ZabbixSpec extends InfraTestSpec {

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
    def host_ids = [:]
    def hostnames = [:]

    def init() {
        super.init()

        def os_account       = test_platform.os_account
        this.zabbix_ip       = os_account['server']
        this.zabbix_user     = os_account['user']
        this.zabbix_password = os_account['password']
        this.timeout         = test_platform.timeout

        this.target_server = test_platform?.test_target?.remote_alias ?: ''
        if (this.target_server == '') {
            this.target_server = test_platform?.test_target?.name
        }
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

    def HostGroup(test_item) {
        if (target_server)
            return true

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
        def csv = []
        def name
        host_groups.each { host_group ->
            def columns = []
            headers.each {
                columns.add(host_group[it] ?: 'NaN')
            }
            csv << columns
        }

        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def User(test_item) {
        if (target_server)
            return true

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
            csv << columns
        }
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def Host(test_item) {
        def lines = exec('Host') {

            def params = [
                output: "extend",
                selectInterfaces: "extend",
                selectGroups: "extend",
                selectMacros: "extend",
                selectParentTemplates: "extend",
            ]
            if (target_server) {
                params['filter'] = [
                    'host' : target_server
                ]
            }

            def json = JsonOutput.toJson(
                [
                    jsonrpc: "2.0",
                    method: "Host.get",
                    params: params,
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
            new File("${local_dir}/Host").text = content
            return content
        }

        def jsonSlurper = new JsonSlurper()
        def hosts = jsonSlurper.parseText(lines)

        def headers = ['hostid', 'groups', 'macros', 'parentTemplates', 'host', 'name',
                       'interfaces', 'status', 'available', 'error']
        def csv = []
        def host_info = [:]
        hosts.each { host ->
            def columns = []
            headers.each {
                def value = 'NaN'
                if (it == 'groups') {
                    def groups = []
                    host[it].each { group ->
                        def name = group['name']
                        groups.add(name)
                    }
                    value = groups.toString()
                    add_new_metric("Host.host_group", "ホストグループ", value, host_info)

                } else if (it == 'macros') {
                    def macros = [:]
                    host[it].each { macro ->
                        def new_test_id = 'Host.' + macro['macro']
                        def macro_value = macro['value']
                        // def desc = test_platform?.test_metrics['Host']?.description
                        // this.test_platform.add_test_metric(new_test_id, desc)
                        host_info[new_test_id] = macro_value
                        macros[macro['macro']] = macro_value
                        add_new_metric(new_test_id,
                                       "マクロ ${new_test_id}",
                                       macro_value, host_info)
                    }
                    value = macros.size()

                } else if (it == 'parentTemplates') {
                    def templates = []
                    host[it].each { template ->
                        def name = template['name']
                        templates.add(name)
                        add_new_metric("Host.template.${name}",
                                       "テンプレート ${name}",
                                       "Enable", host_info)
                    }
                    value = templates.toString()

                } else if (it == 'interfaces') {
                    def addresses = []
                    host[it].each { addr ->
                        addresses.add((addr['useip'] == '1') ? addr['ip'] : addr['dns'])
                    }
                    value = addresses.toString()

                } else if (it == 'status' || it == 'available') {
                    def id = host[it]
                    value = zabbix_labels[it][id]

                } else {
                    value = host[it]
                }
                if (it == 'hostid') {
                    host_ids[target_server] = value
                    hostnames[value] = host['host']
                    value = "'${value}'"
                }
                host_info[it] = value
                columns.add(value)
            }
            csv << columns
        }
        test_item.devices(csv, headers)
        host_info['Host'] = (hosts.size() == 1) ? hosts[0]['host'] : ''
        test_item.results(host_info)
        // println host_info
        // println "ZABBIX_STATUS: ${host_info['status']}"
        test_item.verify_text_search('status', host_info['status'])
        test_item.verify_text_search('available', host_info['available'])
    }

    def syslog(test_item) {
        if(target_server && !host_ids.containsKey(target_server)) {
            log.info "Can't find host_id of ${target_server}, 'syslog' test needs 'Host' test before."
            this.Host(test_item)
        }

        def lines = exec('syslog') {

            def params = [
                output: "extend",
                selectHosts: "extend",
                search: [
                    key_: "log", 
                ],
            ]

            if (target_server) {
                params['hostids'] = [
                    host_ids[target_server]
                ]
            }
            def json = JsonOutput.toJson(
                [
                    jsonrpc: "2.0",
                    method: "Item.get",
                    params: params,
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
            new File("${local_dir}/syslog").text = content
            return content
        }

        def jsonSlurper = new JsonSlurper()
        def results = jsonSlurper.parseText(lines)
        def res = [:]
        if (results.size() > 0) {
            def message = 'Monitored'
            def lastlogsize = 0
            def csv   = []
            results.each { result ->
                def hostid   = result['hostid']
                def hostname = hostnames[hostid] ?: null
                def itemname = result['name']
                def error    = result['error']
                def key_     = result['key_']
                def state    = result['state'] ?: 0
                def status   = result['status'] ?: 0

                // Type of imformation is LOG : 2
                if (hostname && result['value_type'] == '2') {  
                    def label_state  = zabbix_labels['status'][state]
                    def label_status = zabbix_labels['trigger.status'][status]
                    if (label_state != 'Monitored' && label_status == 'Enabled') {
                        message = 'Unmonitored'
                    }
                    def logsize  = NumberUtils.toDouble(result['lastlogsize'])
                    add_new_metric("syslog.${itemname}", itemname, 
                                   "$label_state/$label_status", res)

                    csv << [hostname, itemname, logsize, key_, label_status, 
                            label_state, error]
                    lastlogsize += logsize
                }
            }
            def headers = ['Hostname', 'ItemName', 'LastLogSize', 'Key', 
                           'Status', 'State', 'Error']
            test_item.devices(csv, headers)
            res['syslog'] = message
            test_item.results(res)
            test_item.verify_text_search('syslog', message)
        }
    }

    def trigger(test_item) {
        if(target_server && !host_ids.containsKey(target_server)) {
            log.error "Can't find host_id of ${target_server}, 'trigger' test needs 'Host' test before."
            this.Host(test_item)
        }

        def lines = exec('trigger') {

            def params = [
                output: "extend",
                // selectHosts: "extend",
                // selectHosts: "extend",
                selectFunctions : "extend", 
                expandData : "true", 
                expandDescription : "true", 
                expandExpression : "true", 
            ]
            if (target_server) {
                params['hostids'] = [
                    host_ids[target_server]
                ]
            }
            def json = JsonOutput.toJson(
                [
                    jsonrpc: "2.0",
                    method: "Trigger.get",
                    params: params,
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
            new File("${local_dir}/trigger").text = content
            return content
        }
        def jsons = new JsonSlurper().parseText(lines)

        def headers = ['priority', 'description', 'expression', 'flags', 'state', 'status']
        def csv   = []
        def results = [:].withDefault{0}
        def zabbix_info = [:]
        def result = 'NotFoundError'
        jsons.each { json ->
            def columns = []
            def label_statuses = [:]
            def description = 'NaN'
            headers.each { item_name ->
                def value = json[item_name]
                if (item_name == 'state' || item_name == 'status') {
                    label_statuses[item_name] = value
                    if (value != '0') {
                        def id = "${item_name}."
                        id += zabbix_labels["trigger.${item_name}"][value]
                        results[id] += 1
                    }

                } else if (item_name == 'priority' || item_name == 'status') {
                    value = zabbix_labels["trigger.${item_name}"][value]

                } else if (item_name == 'description') {
                    description = value.replaceAll(/${target_server}/,"ホスト")
                }
                columns.add(value)
            }
            def label_status = (label_statuses['status'] == '0') ? 'Enabled' : 'Disabled'
            if (label_statuses['state'] != '0') {
                label_status = 'Error'
                result = 'ErrorFound'
            }
            def new_test_id = 'trigger.' + description

            // If the Excel comment set multibyte, garbled characters will occur,
            // so set the metric's description
            // def desc = test_platform?.test_metrics['trigger']?.description
            // this.test_platform.add_test_metric(new_test_id, desc)
            add_new_metric(new_test_id, description, label_status, zabbix_info)
            // zabbix_info[new_test_id] = label_status
            csv << columns
        }
        // def res = (results.size() == 0) ? 'AllEnabled' : results.toString()
        // test_item.results(result)
        // zabbix_info['trigger'] = (results.size() == 0) ? 'AllEnabled' : results.toString()
        zabbix_info['trigger'] = result
        test_item.results(zabbix_info)
        test_item.verify_text_search('trigger', result)
        test_item.devices(csv, headers)
    }

    def action(test_item) {
        if (target_server)
            return true

        def lines = exec('User') {

            def json = JsonOutput.toJson(
                [
                    "jsonrpc": "2.0",
                    "method": "action.get",
                    "params": [
                        "output": "extend",
                        "selectOperations": "extend",
                        "selectFilter": "extend",
                        "filter": [
                            "eventsource": 1
                        ]
                    ],
                    "auth": token,
                    "id": 1
                ]
                // [
                //     jsonrpc: "2.0",
                //     method: "User.get",
                //     params: [
                //         output: "extend",
                //         selectMedias: "extend",
                //         selectUsrgrps: "extend",
                //     ],
                //     id: "1",
                //     auth: token,
                // ]
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
            new File("${local_dir}/action").text = content
            return content
        }

        def jsonSlurper = new JsonSlurper()
        def actions = jsonSlurper.parseText(lines)

// {
//     "jsonrpc": "2.0",
//     "result": [
//         {
//             "actionid": "2",
//             "name": "Auto discovery. Linux servers.",
//             "eventsource": "1",
//             "status": "1",
//             "esc_period": "0",
//             "def_shortdata": "",
//             "def_longdata": "",
//             "recovery_msg": "0",
//             "r_shortdata": "",
//             "r_longdata": "",
//             "filter": {
//                 "evaltype": "0",
//                 "formula": "",
//                 "conditions": [
//                     {
//                         "conditiontype": "10",
//                         "operator": "0",
//                         "value": "0",
//                         "formulaid": "B"
//                     },
//                     {
//                         "conditiontype": "8",
//                         "operator": "0",
//                         "value": "9",
//                         "formulaid": "C"
//                     },
//                     {
//                         "conditiontype": "12",
//                         "operator": "2",
//                         "value": "Linux",
//                         "formulaid": "A"
//                     }
//                 ],
//                 "eval_formula": "A and B and C"
//             },
//             "operations": [
//                 {
//                     "operationid": "1",
//                     "actionid": "2",
//                     "operationtype": "6",
//                     "esc_period": "0",
//                     "esc_step_from": "1",
//                     "esc_step_to": "1",
//                     "evaltype": "0",
//                     "opconditions": [],
//                     "optemplate": [
//                         {
//                             "operationid": "1",
//                             "templateid": "10001"
//                         }
//                     ]
//                 },
//                 {
//                     "operationid": "2",
//                     "actionid": "2",
//                     "operationtype": "4",
//                     "esc_period": "0",
//                     "esc_step_from": "1",
//                     "esc_step_to": "1",
//                     "evaltype": "0",
//                     "opconditions": [],
//                     "opgroup": [
//                         {
//                             "operationid": "2",
//                             "groupid": "2"
//                         }
//                     ]
//                 }
//             ]
//         }
//     ],
//     "id": 1
// }

        def headers = ['userid', 'usrgrps', 'alias', 'name', 'medias']
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
            csv << columns
        }
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }
}

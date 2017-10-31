package jp.co.toshiba.ITInfra.acceptance

import groovy.io.FileType
import groovy.util.logging.Slf4j
import static groovy.json.JsonOutput.*
import groovy.json.*
import org.apache.commons.io.FileUtils
import groovy.sql.Sql
import java.sql.*

@Slf4j
@Singleton
class CMDBModel {

    final static def current_build = 1
    EvidenceManager evidence_manager
    def cmdb
    def cmdb_cache = [:]

    def registMaster(table_name, columns) throws SQLException {
        def cache_key = table_name + columns.toString()
        if (cmdb_cache.containsKey(cache_key)) {
            return cmdb_cache[cache_key]
        }
        def conditions = []
        def values = []
        columns.each { column_name, value ->
            conditions << "${column_name} = ?"
            values << value
        }
        def query = "select id from ${table_name} where " +
                    conditions.join(' and ')
        def rows = cmdb.rows(query, values)
        if (rows != null && rows.size() == 1) {
            def id = rows[0]['id']
            cmdb_cache[cache_key] = id
            return id
        }
        log.debug "INSERT:$table_name"
        log.debug "INSERT_DATA:$columns"
        def table = cmdb.dataSet(table_name)
        try {
            table.add(columns)
        } catch (SQLException e) {
            log.info "This table already have a data, Skip\n" +
                     "${table_name} : ${columns}\n"
        }
        rows = cmdb.rows(query, values)
        if (rows != null && rows.size() == 1) {
            def id = rows[0]['id']
            cmdb_cache[cache_key] = id
            return id
        }
    }

    def registMetric(node_id, metric_id, Map metric) throws SQLException {
        cmdb.execute("delete from test_results where node_id = ? and metric_id = ?",
                     [node_id, metric_id])
        def columns = [node_id: node_id, metric_id: metric_id]
        ['value', 'verify'].each { item_name ->
            if (metric.containsKey(item_name)) {
                columns[item_name.toUpperCase()] = metric[item_name]
            }
        }
        cmdb.dataSet('test_results').add(columns)
    }

    def registDevice(node_id, metric_id, List devices) throws SQLException {
        cmdb.execute("delete from device_results where node_id = ? and metric_id = ?",
                     [node_id, metric_id])
        def seq = 1
        devices.each { device ->
            def keys = [node_id: node_id, metric_id: metric_id, seq: seq]
            device.each { item_name, value ->
                def columns = keys + [item_name: item_name, value: value]
                cmdb.dataSet('device_results').add(columns)
            }
            seq ++
        }
    }

    def export(String node_config_source) throws IOException, SQLException,
        IllegalArgumentException {

        long start = System.currentTimeMillis()
        // Regist tag, group table
        def tag_id   = registMaster("tags", [tag_name: evidence_manager.project_name])

        new File(node_config_source).eachDir {
            def domain_name = it.name
            log.info "Regist domain ${domain_name}"
            def domain_id = registMaster("platforms", [platform_name: domain_name])
            def domain_dir = "${node_config_source}/${domain_name}"

            // Regist Metrics
            new File(domain_dir).eachFile {
                ( it.name =~ /(.+).json/ ).each { json_file, node_name ->
                    def metric_text = new File("${domain_dir}/${json_file}").text
                    def metrics = new JsonSlurper().parseText(metric_text)

                    log.info "Regist node ${node_name}"
                    def node_id = registMaster("nodes", [node_name: node_name])
                    registMaster("tag_nodes", [tag_id: tag_id, node_id: node_id])
                    metrics.each { metric ->
                        log.debug "Regist metric ${metric}"
                        def metric_id = registMaster("metrics",
                                                    [metric_name: metric?.test_id,
                                                     platform_id: domain_id])
                        registMetric(node_id, metric_id, metric)
                    }
                }
            }

            // Regist Devices
            new File(domain_dir).eachDir {
                def node_name = it.name
                def node_id = registMaster("nodes", [node_name: node_name])
                log.debug "DEVICE: $node_name, $node_id"
                registMaster("tag_nodes", [tag_id: tag_id, node_id: node_id])
                def device_dir = "${domain_dir}/${node_name}"
                def set_flag_sql = 'update metrics set device_flag = true where id = ?'
                new File(device_dir).eachFile {
                    ( it.name =~/(.+).json/).each { json_file, metric_name->
                        def device_text = new File("${device_dir}/${json_file}").text
                        def devices = new JsonSlurper().parseText(device_text)
                        log.info "Regist device ${node_name} ${metric_name}"
                        def metric_id = registMaster("metrics",
                                                    [metric_name: metric_name,
                                                     platform_id: domain_id])
                        registDevice(node_id, metric_id, devices)
                        cmdb.execute(set_flag_sql, metric_id)

                    }
                }
            }
        }
        long elapsed = System.currentTimeMillis() - start
        log.info "Export, Elapsed : ${elapsed} ms"
    }

    def getMetricByHost(String server_name) throws SQLException {
        def sql = '''\
            |select domain_name, node_name, metric_name, value
            |from domains, nodes, metrics, test_results
            |where nodes.id = test_results.node_id
            |and test_results.metric_id = metrics.id
            |and metrics.domain_id = domains.id
            |and node_name = ?
        '''.stripMargin()

        cmdb.rows(sql, server_name)
    }

    def getDeviceResultByHost(String server_name) throws SQLException {
        def sql = '''\
            |select domain_name, node_name, metric_name, seq, item_name, value
            |from domains, nodes, metrics, device_results
            |where nodes.id = device_results.node_id
            |and device_results.metric_id = metrics.id
            |and metrics.domain_id = domains.id
            |and node_name = ?
        '''.stripMargin()

        cmdb.rows(sql, server_name)
    }

    def initialize(EvidenceManager evidence_manager) throws IOException, SQLException {
        this.evidence_manager = evidence_manager
        def db_config = evidence_manager.db_config
        def config_db = Config.instance.read(db_config)
        def config_ds = config_db?.cmdb?.dataSource
        this.initialize(config_ds)
    }

    def initialize(Map config_ds) throws IOException, SQLException {
        if (!this.cmdb) {
            if (!config_ds) {
                def msg = "Config not found cmdb.dataSource: ${db_config}"
                throw new IllegalArgumentException(msg)
            }
            this.cmdb = Sql.newInstance(config_ds['url'], config_ds['username'],
                                  config_ds['password'], config_ds['driver'])
        }

        // Confirm existence of Version table. If not, execute db create script
        def cmdb_exists = false
        try {
            List rows = this.cmdb.rows('select * from version')
            if (rows.size()) {
                cmdb_exists = true
            }
        } catch (SQLException e) {
            log.warn "VERSION table not found in CMDB, Create tables"
        }
        if (!cmdb_exists) {
            def getconfig_home = evidence_manager?.getconfig_home ?: System.getProperty("user.dir")
            log.info "Set HOME: ${getconfig_home}";
            def create_db_script = "${getconfig_home}/lib/script/cmdb/create_db.sql"
            def create_sqls = new File(create_db_script).text.split(/;\s*\n/).each {
                this.cmdb.execute it
            }
        }
    }

    def initialize_data(Boolean is_test = false) throws IOException, SQLException {
        def platforms = [:]
        def tags      = [:]
        def groups    = [:]
        def accounts  = [:]

        // Regist default 'GROUPS'
        groups['_Default'] = registMaster("groups", [group_name: '_Default'])
        // Regist default 'PLATFORMS'
        ['Linux', 'Windows', 'vCenter'].each { name ->
            def platform_id = registMaster("platforms", [platform_name: name, build: 1])
            platforms[name] = platform_id
            if (name == 'vCenter') {
                // Regist default 'PLATFORM_CONFIG_DETAILS'
                ['cpu', 'memory', 'storage'].each { item_name ->
                    registMaster('platform_config_details', [platform_id: platform_id, item_name: item_name])
                }
            }
        }

        // For development environment
        if (is_test) {
            // Regits 'ACCOUNTS'
            [[platform_name: 'Linux',   account_name: 'LinuxAccount1',   username: 'someuser'],
             [platform_name: 'Windows', account_name: 'WindowsAccount1', username: 'Administrator'],
             [platform_name: 'vCenter', account_name: 'vCenterAccount1', username: 'guest', remote_ip: '192.168.10.10']
            ].each { info ->
                def platform_name = info.platform_name
                info['password']  = 'P@ssword'
                info.remove('platform_name')
                accounts[platform_name] = registMaster('accounts', info)
            }

            // Regist 'TAGS'
            ['Deploy01', 'Deploy02', 'Deploy03', 'Deploy04'].each { name ->
                tags[name] = registMaster("tags", [tag_name: name])
            }
            // Regist 'GROUPS'
            ['System01', 'System02'].each { name ->
                groups[name] = registMaster("groups", [group_name: name])
            }
            // Regist 'NODES'
            [[node_name: 'ostrich', platform_name: 'Linux',   ip: '192.168.10.1'],
             [node_name: 'w2016',   platform_name: 'Windows', ip: '192.168.10.2'],
            ].each { info->
                def platform_name = info.platform_name
                info['group_id']  = groups['System01']
                info.remove('platform_name')
                def node_id = registMaster('nodes', info)

                // Regist 'NODE_CONFIGS'
                [platform_name, 'vCenter'].each{ platform ->
                    def node_config_id = registMaster('node_configs',
                        [platform_id: platforms[platform], node_id: node_id, account_id: accounts[platform]])

                    // Regist 'NODE_CONFIG_DETAILS'
                    if (platform == 'vCenter') {
                        registMaster('node_config_details', [node_config_id: node_config_id, item_name: 'cpu',       value: 4])
                        registMaster('node_config_details', [node_config_id: node_config_id, item_name: 'memory',    value: 8])
                        registMaster('node_config_details', [node_config_id: node_config_id, item_name: 'disk_size', value: 20])
                    }
                }
            }

            def verify_test_id = registMaster('verify_tests', [verify_test_name: 'Deploy01A'])
            registMaster('verify_configs', [verify_test_id: verify_test_id, item_name: 'node_config_file_path'])
        }
    }
}

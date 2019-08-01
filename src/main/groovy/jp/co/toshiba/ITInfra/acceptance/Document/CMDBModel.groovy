package jp.co.toshiba.ITInfra.acceptance


import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.Model.ResultStatus

import java.sql.SQLException

@Slf4j
@Singleton
@ToString(includePackage = false)
class CMDBModel {

    final static def current_build = 1
    ConfigObject cmdb_config
    def create_db_sql
    String project_name
    String tenant_name
    def cmdb
    def cmdb_cache = [:]

    def set_environment(ConfigTestEnvironment env) {
        this.cmdb_config   = env.get_cmdb_config()
        this.create_db_sql = env.get_create_db_sql()
        this.project_name  = env.get_project_name()
        this.tenant_name   = env.get_tenant_name()
    }

    def initialize() throws IOException, SQLException {
        if (!this.cmdb) {
            def config_ds = this.cmdb_config?.cmdb?.dataSource
            if (!config_ds) {
                def msg = "Config not found cmdb.dataSource in 'config/cmdb.groovy'"
                throw new IllegalArgumentException(msg)
            }
            try {
                this.cmdb = Sql.newInstance(config_ds['url'], config_ds['username'],
                                            config_ds['password'], config_ds['driver'])
            } catch (SQLException e) {
                def msg = "Connection error in 'config/cmdb.groovy' : "
                throw new SQLException(msg + e)
            }
        }

        // Confirm existence of Version table. If not, execute db create script
        def cmdb_exists = false
        try {
            List rows = this.cmdb.rows('select * from tenants')
            if (rows.size()) {
                cmdb_exists = true
            }
        } catch (SQLException e) {
            log.warn "VERSION table not found in CMDB, Create tables"
        }
        if (!cmdb_exists) {
            def create_sqls = new File(this.create_db_sql).text.split(/;\s*\n/).each {
                this.cmdb.execute it
            }
        }
    }

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
        def table = cmdb.dataSet(table_name)
        try {
            table.add(columns)
        } catch (SQLException e) {
            log.info "This table already have a data, Skip\n" +
                     "${table_name} : ${columns}"
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
        if (metric.value != null)
            columns['value']  = metric.value
        if (metric.verify == ResultStatus.OK || metric.verify == ResultStatus.NG)
            columns['verify'] = (metric.verify == ResultStatus.OK) ? 1 : 0
        cmdb.dataSet('test_results').add(columns)
    }

    def registDevice(node_id, metric_id, Map device_info) throws SQLException {
        cmdb.execute("delete from device_results where node_id = ? and metric_id = ?",
                     [node_id, metric_id])
        def rownum = 1
        def header = device_info.header
        def csv = device_info.csv
        log.debug "Regist device: ${node_id}, ${metric_id}, ${header}"
        csv.each { line ->
            def keys = [node_id: node_id, metric_id: metric_id, seq: rownum]
            def colnum = 0

            line.each { value ->
                def item_name = header[colnum]
                def columns = keys + [item_name: item_name, value: value]
                log.debug "Set device_results: $columns"
                cmdb.dataSet('device_results').add(columns)
                colnum ++
            }
            rownum ++
        }
    }

    def export(String node_config_source) throws IOException, SQLException,
        IllegalArgumentException {

        // long start = System.currentTimeMillis()
        // Regist SITE, TENANT table
        def site_id   = registMaster("sites", [site_name: this.project_name])
        def tenant_id = registMaster("tenants", [tenant_name: this.tenant_name])

        new File(node_config_source).eachDir { node_dir ->
            def node_name = node_dir.name
            // log.info "Regist node ${node_name}"
            def node_id = registMaster("nodes",
                                       [node_name: node_name,
                                        tenant_id: tenant_id])
            registMaster("site_nodes", [site_id: site_id, node_id: node_id])
            def device_metric = [:]
            def set_flag_sql = 'update metrics set device_flag = true where id = ?'
            node_dir.eachFile { 
                ( it.name =~ /(.+).json/ ).each { json_file, platform_name ->
                    log.info "Regist node ${node_name}:${platform_name}"

                    def platform_id = registMaster("platforms", [platform_name: platform_name])
                    def metrics = new JsonSlurper().parseText(it.text)
                    metrics.each { metric_name, metric ->
                        def metric_value = metric.value
                        if (metric.devices) {
                            // Regist device
                            device_metric[metric_name] = true
                            log.debug "Regist device ${metric_name}"
                            def metric_id = registMaster("metrics",
                                                         [metric_name: metric_name,
                                                          platform_id: platform_id])
                            cmdb.execute(set_flag_sql, metric_id)
                            try {
                                registMetric(node_id, metric_id, metric)
                                registDevice(node_id, metric_id, metric.devices)
                            } catch (SQLException e) {
                                log.warn "Regist device metric failed $node_name, $platform_name, $metric_name: $e"
                            }

                        } else {
                            // Regist metric
                            // Skip if postfix is ​​device name
                            def is_device_metric = false
                            (metric_name =~ /^(.+?)\./).each { check_metric, postfix ->
                                if (device_metric.containsKey(postfix))
                                    is_device_metric = true
                            }
                            if (!is_device_metric) {
                                log.debug "Regist metric ${metric_name}"
                                def metric_id = registMaster("metrics",
                                                             [metric_name: metric_name,
                                                              platform_id: platform_id])
                                registMetric(node_id, metric_id, metric)
                            }
                        }
                    }
                }
            }
        }
        // long elapsed = System.currentTimeMillis() - start
        // log.info "Export, Elapsed : ${elapsed} ms"
    }

    def getMetricByHost(String server_name) throws SQLException {
        def sql = '''\
            |select platform_name, node_name, metric_name, value
            |from platforms, nodes, metrics, test_results
            |where nodes.id = test_results.node_id
            |and test_results.metric_id = metrics.id
            |and metrics.platform_id = platforms.id
            |and node_name = ?
        '''.stripMargin()

        cmdb.rows(sql, server_name)
    }

    def getDeviceResultByHost(String server_name) throws SQLException {
        def sql = '''\
            |select platform_name, node_name, metric_name, seq, item_name, value
            |from platforms, nodes, metrics, device_results
            |where nodes.id = device_results.node_id
            |and device_results.metric_id = metrics.id
            |and metrics.platform_id = platforms.id
            |and node_name = ?
        '''.stripMargin()

        cmdb.rows(sql, server_name)
    }
}

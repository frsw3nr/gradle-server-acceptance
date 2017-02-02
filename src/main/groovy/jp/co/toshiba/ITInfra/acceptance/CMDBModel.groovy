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
        cmdb.execute("delete from test_result where node_id = ? and metric_id = ?",
                     [node_id, metric_id])
        def columns = [node_id: node_id, metric_id: metric_id]
        ['value', 'verify'].each { item_name ->
            if (metric.containsKey(item_name)) {
                columns[item_name.toUpperCase()] = metric[item_name]
            }
        }
        cmdb.dataSet('test_result').add(columns)
    }

    def registDevice(node_id, metric_id, List devices) throws SQLException {
        cmdb.execute("delete from device_result where node_id = ? and metric_id = ?",
                     [node_id, metric_id])
        def seq = 1
        devices.each { device ->
            def keys = [node_id: node_id, metric_id: metric_id, seq: seq]
            device.each { item_name, value ->
                def columns = keys + [item_name: item_name, value: value]
                cmdb.dataSet('device_result').add(columns)
            }
            seq ++
        }
    }

    def export(String node_config_source) throws IOException, SQLException,
        IllegalArgumentException {
        // Regist SITE, TENANT table
        def site_id   = registMaster("site", [site_name: evidence_manager.project_name])
        def tenant_id = registMaster("tenant", [tenant_name: '_Default'])

        new File(node_config_source).eachDir {
            def domain_name = it.name
            log.info "Regist domain ${domain_name}"
            def domain_id = registMaster("domain", [domain_name: domain_name])
            def domain_dir = "${node_config_source}/${domain_name}"

            // Regist Metrics
            new File(domain_dir).eachFile {
                ( it.name =~ /(.+).json/ ).each { json_file, node_name ->
                    def metric_text = new File("${domain_dir}/${json_file}").text
                    def metrics = new JsonSlurper().parseText(metric_text)

                    log.info "Regist node ${node_name}"
                    def node_id = registMaster("node", [node_name: node_name,
                                               tenant_id: tenant_id])
                    registMaster("site_node", [site_id: site_id, node_id: node_id])
                    metrics.each { metric ->
                        log.debug "Regist metric ${metric}"
                        def metric_id = registMaster("metric",
                                                    [metric_name: metric?.test_id,
                                                     domain_id: domain_id])
                        registMetric(node_id, metric_id, metric)
                    }
                }
            }

            // Regist Devices
            new File(domain_dir).eachDir {
                def node_name = it.name
                def node_id = registMaster("node", [node_name: node_name,
                                           tenant_id: tenant_id])
                registMaster("site_node", [site_id: site_id, node_id: node_id])
                def device_dir = "${domain_dir}/${node_name}"
                new File(device_dir).eachFile {
                    ( it.name =~/(.+).json/).each { json_file, metric_name->
                        def device_text = new File("${device_dir}/${json_file}").text
                        def devices = new JsonSlurper().parseText(device_text)
                        log.info "Regist device ${node_name} ${metric_name}"
                        def metric_id = registMaster("metric",
                                                    [metric_name: metric_name,
                                                     domain_id: domain_id])
                        registDevice(node_id, metric_id, devices)
                    }
                }
            }
        }
    }

    def getMetricByHost(String server_name) throws SQLException {
        def sql = '''\
            |select domain_name, node_name, metric_name, value
            |from domain, node, metric, test_result
            |where node.id = test_result.node_id
            |and test_result.metric_id = metric.id
            |and metric.domain_id = domain.id
            |and node_name = ?
        '''.stripMargin()

        cmdb.rows(sql, server_name)
    }

    def getDeviceResultByHost(String server_name) throws SQLException {
        def sql = '''\
            |select domain_name, node_name, metric_name, seq, item_name, value
            |from domain, node, metric, device_result
            |where node.id = device_result.node_id
            |and device_result.metric_id = metric.id
            |and metric.domain_id = domain.id
            |and node_name = ?
        '''.stripMargin()

        cmdb.rows(sql, server_name)
    }

    def initialize(EvidenceManager evidence_manager) throws IOException, SQLException {
        if (!this.cmdb) {
            this.evidence_manager = evidence_manager
            def db_config = evidence_manager.db_config
            def config_db = Config.instance.read(db_config)
            def config_ds = config_db?.cmdb?.dataSource
            if (!config_ds) {
                def msg = "Config not found cmdb.dataSource: ${db_config}"
                throw new IllegalArgumentException(msg)
            }
            this.cmdb = Sql.newInstance(config_ds['url'], config_ds['username'],
                                  config_ds['password'], config_ds['driver'])
        }

        // Confirm existence of Version table. If not, execute db create script
        def cmdb_version = null
        try {
            List rows = this.cmdb.rows('select build from version')
            if (rows.size()) {
                cmdb_version = rows[0]['build'] ?: null
            }
        } catch (SQLException e) {
            log.warn "VERSION table not found in CMDB, Create tables"
        }
        if (cmdb_version == null) {
            def getconfig_home = evidence_manager.getconfig_home
            def create_db_script = "${getconfig_home}/lib/script/cmdb/create_db.sql"
            def create_sqls = new File(create_db_script).text.split(/;\s*\n/).each {
                this.cmdb.execute it
            }
        // If the version is old, execute update scrit (TBD)
        } else if (cmdb_version < current_build) {
            log.warn "Rebuild table (TBD)"
        }
    }

}

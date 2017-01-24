package jp.co.toshiba.ITInfra.acceptance

import groovy.io.FileType
import groovy.util.logging.Slf4j
import static groovy.json.JsonOutput.*
import groovy.json.*
import org.apache.commons.io.FileUtils
import groovy.sql.Sql
import java.sql.*

@Slf4j
class ConfigManageDB {

    final static def current_build = 1
    String home
    String project_name
    String tenant_name
    String last_run_config
    String db_config
    String node_config_source
    def cmdb
    def cmdb_cache = [:]

    ConfigManageDB(Map params) {
        assert params.home
        this.home = params.home
        this.project_name = new File(this.home).getName()
        this.tenant_name = '_Default'
        this.last_run_config = params.last_run_config ?: "${params.home}/build/.last_run"
        this.db_config  = params.db_config ?: 'config/cmdb.groovy'
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
        def query = "select ID from ${table_name} where " + conditions.join(' and ')
        def rows = cmdb.rows(query, values)
        if (rows != null && rows.size() == 1) {
            def id = rows[0]['ID']
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
            def id = rows[0]['ID']
            cmdb_cache[cache_key] = id
            return id
        }
    }

    def registMetric(node_id, metric_id, Map metric) throws SQLException {
        cmdb.execute("DELETE FROM TEST_RESULT WHERE NODE_ID = ? AND METRIC_ID = ?",
                     [node_id, metric_id])
        def columns = [NODE_ID: node_id, METRIC_ID: metric_id]
        ['value', 'verify'].each { item_name ->
            if (metric.containsKey(item_name)) {
                columns[item_name.toUpperCase()] = metric[item_name]
            }
        }
        cmdb.dataSet('TEST_RESULT').add(columns)
    }

    def registDevice(node_id, metric_id, List devices) throws SQLException {
        cmdb.execute("DELETE FROM DEVICE_RESULT WHERE NODE_ID = ? AND METRIC_ID = ?",
                     [node_id, metric_id])
        def seq = 1
        devices.each { device ->
            def keys = [NODE_ID: node_id, METRIC_ID: metric_id, SEQ: seq]
            device.each { item_name, value ->
                def columns = keys + [ITEM_NAME: item_name, VALUE: value]
                cmdb.dataSet('DEVICE_RESULT').add(columns)
            }
            seq ++
        }
    }

    def export(String node_config_source) throws IOException, SQLException,
        IllegalArgumentException {
        assert home
        initialize()
        // Regist SITE, TENANT table
        def site_id   = registMaster("SITE", [SITE_NAME: project_name])
        def tenant_id = registMaster("TENANT", [TENANT_NAME: '_Default'])

        new File(node_config_source).eachDir {
            def domain_name = it.name
            log.info "Regist DOMAIN ${domain_name}"
            def domain_id = registMaster("DOMAIN", [DOMAIN_NAME: domain_name])
            def domain_dir = "${node_config_source}/${domain_name}"

            // Regist Metrics
            new File(domain_dir).eachFile {
                ( it.name =~ /(.+).json/ ).each { json_file, node_name ->
                    def metric_text = new File("${domain_dir}/${json_file}").text
                    def metrics = new JsonSlurper().parseText(metric_text)

                    log.info "Regist NODE ${node_name}"
                    def node_id = registMaster("NODE", [NODE_NAME: node_name,
                                               SITE_ID: site_id,
                                               TENANT_ID: tenant_id])
                    metrics.each { metric ->
                        log.debug "Regist METRIC ${metric}"
                        def metric_id = registMaster("METRIC",
                                                    [METRIC_NAME: metric?.test_id,
                                                     DOMAIN_ID: domain_id])
                        registMetric(node_id, metric_id, metric)
                    }
                }
            }

            // Regist Devices
            new File(domain_dir).eachDir {
                def node_name = it.name
                def node_id = registMaster("NODE", [NODE_NAME: node_name,
                                           SITE_ID: site_id,
                                           TENANT_ID: tenant_id])
                def device_dir = "${domain_dir}/${node_name}"
                new File(device_dir).eachFile {
                    ( it.name =~/(.+).json/).each { json_file, metric_name->
                        def device_text = new File("${device_dir}/${json_file}").text
                        def devices = new JsonSlurper().parseText(device_text)
                        log.info "Regist DEVICE ${node_name} ${metric_name}"
                        def metric_id = registMaster("METRIC",
                                                    [METRIC_NAME: metric_name,
                                                     DOMAIN_ID: domain_id])
                        registDevice(node_id, metric_id, devices)
                    }
                }
            }
        }
    }

    def initialize() throws IOException, SQLException {
        def config_db = Config.instance.read(db_config)
        def config_ds = config_db?.cmdb?.dataSource
        if (!config_ds) {
            def msg = "Config not found cmdb.dataSource: ${db_config}"
            throw new IllegalArgumentException(msg)
        }
        cmdb = Sql.newInstance(config_ds['url'], config_ds['username'],
                              config_ds['password'], config_ds['driver'])

        // Confirm existence of Version table. If not, execute db create script
        def cmdb_version = null
        try {
            List rows = cmdb.rows('SELECT BUILD FROM VERSION')
            if (rows.size()) {
                cmdb_version = rows[0]['BUILD'] ?: null
            }
        } catch (SQLException e) {
            log.warn "VERSION table not found in CMDB, Create tables"
        }
        if (cmdb_version == null) {
            def create_db_script = "lib/script/cmdb/create_db.sql"
            def create_sqls = new File(create_db_script).text.split(/;\s*\n/).each {
                cmdb.execute it
            }
        // If the version is old, execute update scrit (TBD)
        } else if (cmdb_version < current_build) {
            log.warn "Rebuild table (TBD)"
        }
    }

}

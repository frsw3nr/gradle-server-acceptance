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
class InventoryDB {

    ConfigObject db_config
    def create_db_sql
    String base_test_log_dir
    String project_name
    String tenant_name
    def cmdb
    def cmdb_cache = [:]

    def set_environment(ConfigTestEnvironment env) {
        this.db_config         = env.get_inventory_db_config()
        this.base_test_log_dir = env.get_base_test_log_dir()
        this.create_db_sql     = env.get_create_inventory_db_sql()
        this.project_name      = env.get_project_name()
        this.tenant_name       = env.get_tenant_name()
    }

    // def initialize() throws IOException, SQLException {
    //     if (!this.cmdb) {
    //         def config_ds = this.db_config?.dataSource
    //         if (!config_ds) {
    //             def msg = "Config not found inventory_db.dataSource in 'config/inventory_db.groovy'"
    //             throw new IllegalArgumentException(msg)
    //         }
    //         try {
    //             this.cmdb = Sql.newInstance(config_ds['url'], config_ds['username'],
    //                                         config_ds['password'], config_ds['driver'])
    //         } catch (SQLException e) {
    //             def msg = "Connection error in 'config/cmdb.groovy' : "
    //             throw new SQLException(msg + e)
    //         }
    //     }

    //     // Confirm existence of Version table. If not, execute db create script
    //     def cmdb_exists = false
    //     try {
    //         List rows = this.cmdb.rows('select * from version')
    //         if (rows.size()) {
    //             cmdb_exists = true
    //         }
    //     } catch (SQLException e) {
    //         log.warn "VERSION table not found in CMDB, Create tables"
    //     }
    //     if (!cmdb_exists) {
    //         def create_sqls = new File(this.create_db_sql).text.split(/;\s*\n/).each {
    //             this.cmdb.execute it
    //         }
    //     }
    // }

    // def rows(String sql, args = null) {
    //     return (args == null) ? this.cmdb.rows(sql) : this.cmdb.rows(sql, args)
    // }

    // def registMaster(table_name, columns) throws SQLException {
    //     def cache_key = table_name + columns.toString()
    //     if (cmdb_cache.containsKey(cache_key)) {
    //         return cmdb_cache[cache_key]
    //     }
    //     def conditions = []
    //     def values = []
    //     columns.each { column_name, value ->
    //         conditions << "${column_name} = ?"
    //         values << value
    //     }
    //     def query = "select id from ${table_name} where " +
    //                 conditions.join(' and ')
    //     def rows = cmdb.rows(query, values)
    //     if (rows != null && rows.size() == 1) {
    //         def id = rows[0]['id']
    //         cmdb_cache[cache_key] = id
    //         return id
    //     }
    //     def table = cmdb.dataSet(table_name)
    //     try {
    //         table.add(columns)
    //     } catch (SQLException e) {
    //         log.info "This table already have a data, Skip\n" +
    //                  "${table_name} : ${columns}" + e
    //     }
    //     rows = cmdb.rows(query, values)
    //     if (rows != null && rows.size() == 1) {
    //         def id = rows[0]['id']
    //         cmdb_cache[cache_key] = id
    //         return id
    //     }
    // }

    Boolean filter_text_match(String target, String keyword = null) {
        if (keyword == null)
            return true
        def target_lc = target.toLowerCase()
        def keyword_lc = keyword.toLowerCase()
        if (target_lc =~ /$keyword_lc/)
            return true
        else
            return false
    }

    def export(String filter_node = null, String filter_platform = null) throws IOException, SQLException,
        IllegalArgumentException {

        def node_updates = [:]
        def node_platforms = [:].withDefault{[]}
        new File(base_test_log_dir).eachDir { node_dir ->
            def node_name = node_dir.name
            if (!(node_name=~/^[a-zA-Z]/))
                return
            if (!(filter_text_match(node_name, filter_node)))
                return
            node_dir.eachFile { platform_dir ->
                if (!(platform_dir.isDirectory()))
                    return
                def platform = platform_dir.name
                if (!(filter_text_match(platform, filter_platform)))
                    return
                node_platforms[node_name] << platform
                def last_modified = new Date(platform_dir.lastModified()).format('yyyy/MM/dd HH:mm:ss')
                node_updates[node_name] = last_modified
            }
        }

        println String.format("%-12s %-32s %s", "Node", "Platform",'LastUpdated')
        def node_count = 0
        node_updates.each { node_name, last_modified ->
            def platforms = node_platforms[node_name]
            println String.format("%-12s %-32s %s", node_name, platforms, last_modified)
            node_count ++
        }

        if (node_count == 0)
            println "No data"
        else 
            println "${node_count} nodes"
    }
}

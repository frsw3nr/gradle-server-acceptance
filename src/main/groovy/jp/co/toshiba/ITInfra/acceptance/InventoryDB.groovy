package jp.co.toshiba.ITInfra.acceptance


import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.Model.*

// import java.sql.SQLException

@Slf4j
@Singleton
@ToString(includePackage = false)
class InventoryDB {

    String base_test_log_dir
    String project_test_log_dir
    String base_node_dir
    String project_node_dir
    String project_name
    String filter_node
    String filter_platform
    static final String NODE_LIST_FORMAT = "%-18s %-18s %-30s %s"
    static final String[] NODE_LIST_HEADERS = ["Directory", "Node", "Platform", 'LastUpdated']
    // ConfigObject db_config
    // def create_db_sql
    // String tenant_name
    // def cmdb
    // def cmdb_cache = [:]

    def set_environment(ConfigTestEnvironment env) {
        this.base_test_log_dir    = env.get_base_test_log_dir()
        this.project_test_log_dir = env.get_project_test_log_dir()
        this.base_node_dir        = env.get_base_node_dir()
        this.project_node_dir     = env.get_project_node_dir()
        this.project_name         = env.get_project_name()
        // this.db_config         = env.get_inventory_db_config()
        // this.create_db_sql     = env.get_create_inventory_db_sql()
        // this.tenant_name       = env.get_tenant_name()
    }

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

    int print_node_list(String project, String test_log_dir) {
        def node_updates = [:]
        def node_platforms = [:].withDefault{[]}
        new File(test_log_dir).eachDir { node_dir ->
            def node_name = node_dir.name
            if (!(node_name=~/^[a-zA-Z]/))
                return
            if (!(filter_text_match(node_name, this.filter_node)))
                return
            node_dir.eachFile { platform_dir ->
                if (!(platform_dir.isDirectory()))
                    return
                def platform = platform_dir.name
                if (!(filter_text_match(platform, this.filter_platform)))
                    return
                node_platforms[node_name] << platform
                def last_modified = new Date(platform_dir.lastModified()).format('yyyy/MM/dd HH:mm:ss')
                node_updates[node_name] = last_modified
            }
        }

        def node_count = 0
        node_updates.each { node_name, last_modified ->
            def platforms = node_platforms[node_name]
            println String.format(NODE_LIST_FORMAT, project, node_name, platforms, last_modified)
            node_count ++
        }
        return node_count
    }

    def export(String filter_node = null, String filter_platform = null) throws IOException, 
        IllegalArgumentException {

        this.filter_node     = filter_node
        this.filter_platform = filter_platform
        def row = 0
        println String.format(NODE_LIST_FORMAT, NODE_LIST_HEADERS)
        row += print_node_list('Current', this.project_test_log_dir)
        row += print_node_list('Base', this.base_test_log_dir)

        if (row == 0)
            println "No data"
        else 
            println "${row} rows"
    }

    def copy_compare_target_inventory_data(TestScenario test_scenario) {
        def domain_metrics = test_scenario.test_metrics.get_all()
        def targets = test_scenario.test_targets.get_all()
        targets.each { target_name, domain_targets ->
            // def port_lists = this.read_port_lists(target_name)
            domain_targets.each { domain, test_target ->
                if (test_target.target_status == RunStatus.INIT &&
                    test_target.comparision == true) {
                    // test_target.port_list = port_lists
                    // this.read_port_list(target_name, domain)
                    def platform_metrics = domain_metrics[domain].get_all()
                    platform_metrics.each { platform_name, platform_metric ->
                        println "TARGET_NAME:${target_name},PLATFORM_NAME:${platform_name},PLATFORM_METRIC:${platform_metric}"
                        // def test_platform = this.read_test_platform_result(target_name,
                        //                                                    platform_name,
                        //                                                    true)
                        // if (test_platform) {
                        //     test_platform.test_target = test_target
                        //     test_target.test_platforms[platform_name] = test_platform
                        // }
                    }
                }
            }
        }

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

}

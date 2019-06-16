package jp.co.toshiba.ITInfra.acceptance

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform

@Slf4j
@ToString(includePackage = false)
@Singleton
// class ConfigTestEnvironment extends SpecModel {
class ConfigTestEnvironment {
    String config_file
    ConfigObject config
    ConfigObject cmdb_config

    def read_config(String config_file = 'config/config.groovy') {
        this.config_file = config_file
        this.config = Config.instance.read(config_file)
    }

    def read_from_test_runner(TestRunner test_runner) {
        def test_runner_config = test_runner.getProperties() as Map

        this.config_file = test_runner.config_file ?: './config/config.groovy'
        try {
            this.config = Config.instance.read(config_file)
        } catch (Exception e) {
            log.warn "Read error : " + e
            // System.exit(1)
        }
        if (!this.config) {
            this.config = new ConfigObject()
        }
        test_runner.getProperties().findAll{ name, value ->
            if (name == 'class')
                return
            this.config."$name" = value
        }
    }

    def get_inventory_db_config(String cmdb_config_path = null) {
        def cmdb_config = get_cmdb_config(cmdb_config_path)
        def inventory_db_config = cmdb_config?.inventory_db
        if(inventory_db_config == null){
            throw new IOException("Not found inventory_db in ${config_path}")
        }
        return inventory_db_config
    }

    def get_cmdb_config(String cmdb_config_path = null) {
        if (! this.cmdb_config) {
            if (!cmdb_config_path) {
                cmdb_config_path = this.get_cmdb_config_path()
            }
            if(!(new File(cmdb_config_path)).exists()){
                throw new IOException("Not found CMDB config file : ${cmdb_config_path}")
            }
            this.cmdb_config =  Config.instance.read(cmdb_config_path)
        }
        return this.cmdb_config
    }

    private get_config_account(Map config_account, String platform, String id) {
        def account = [:]
        if (id) {
            account = config_account[platform][id]
            if (!account) {
                def msg = "Not found parameter 'account.${platform}.${id}' in ${config_file}"
                log.error(msg)
                throw new IllegalArgumentException(msg)
            }
        }
        return account
    }

    def set_account(TestPlatform test_platform) {
        def platform = test_platform.name
        def test_target = test_platform.test_target
        def config_account = this.config['account']
        if (!config_account) {
            def msg = "Not found parameter 'account.{platform}.{id}' in ${config_file}"
            log.error(msg)
            throw new IllegalArgumentException(msg)
        }
        test_platform.with {
            os_account = get_config_account(config_account, platform,
                                            test_target.account_id)
            // if (test_target.specific_password)
            //     os_account.password = test_target.specific_password
        }
    }

    def get_redmine_uri() {
        return this.config?.redmine_uri ?: System.getenv("REDMINE_URL") ?: "http://localhost/redmine";
    }

    def get_redmine_api_key() {
        return this.config?.api_key ?: System.getenv("REDMINE_API_KEY") ?: "";
    }

    def get_redmine_project() {
        return this.config?.redmine_project_name ?: System.getenv("REDMINE_PROJECT") ?:
               this.config?.ticket?.redmine_project ?: "cmdb";
    }

    def get_custom_field_inventory() {
        return this.cmdb_config?.ticket?.custom_field?.inventory ?: "インベントリ";
    }

    def get_custom_field_rack_location() {
        return this.cmdb_config?.ticket?.custom_field?.rack_location ?: "ラック位置";
    }

    def get_custom_field_rack_location_prefix() {
        return this.cmdb_config?.ticket?.custom_field?.rack_location_prefix ?: "RackTables:";
    }

    def get_port_list_custom_fields() {
        return this.cmdb_config?.port_list?.custom_fields;
    }

    def get_tracker_port_list() {
        return this.cmdb_config?.port_list?.tracker ?: 'ポートリスト';
    }

    def get_in_operation_status_id() {
        return this.cmdb_config?.port_list?.in_operation_status_id ?: 10;
    }

    def get_getconfig_home() {
        return this.config?.getconfig_home ?: System.getProperty("getconfig_home") ?: '.'
    }

    def get_project_home() {
        return this.config?.project_home ?: System.getProperty("user.dir")
    }

    def get_project_name() {
        def project_home = this.get_project_home()
        return new File(project_home).getName()
    }

    def get_tenant_name() {
        return '_Default'
    }

    def get_cmdb_config_path() {
        def getconfig_home = this.get_getconfig_home()

        return this.config?.db_config ?: "${getconfig_home}/config/cmdb.groovy"
        // return "${project_home}/config/cmdb.groovy"
    }

    def get_node_dir() {
        def getconfig_home = this.get_getconfig_home()
        return "${getconfig_home}/node/"
    }

    def get_create_db_sql() {
        def project_home = this.get_project_home()
        return "${project_home}/lib/script/cmdb/create_db.sql"
    }

    def get_create_inventory_db_sql() {
        def getconfig_home = this.get_getconfig_home()
        return "${getconfig_home}/lib/script/cmdb/create_inventory_db.sql"
    }

    def get_last_run_config() {
        def project_home = this.get_project_home()
        return this.config?.last_run_config ?: "${project_home}/build/.last_run"
    }

    def get_db_config() {
        def getconfig_home = this.get_getconfig_home()
        return this.config?.db_config ?: "${getconfig_home}/config/cmdb.groovy"
    }

    def get_base_node_dir() {
        def project_home = this.get_project_home()
        return this.config?.node_dir ?: "${project_home}/node"
    }

    def get_test_resource() {
        return this.config?.test_resource ?: './src/test/resources/log'
    }

    def get_base_test_resource() {
        def project_home = this.get_project_home()
        return this.config?.node_dir ?: "${project_home}/src/test/resources/log"
    }

    def get_silent() {
        return this.config?.silent
    }

    def get_auto_tag() {
        return this.config?.auto_tag
    }

    def get_cluster_size() {
        return this.config?.cluster_size ?: 10
    }

    def get_verify_test() {
        return this.config?.verify_test ?: true
    }

    def get_excel_file() {
        return this.config?.excel_file ?: this.config?.evidence?.source ?:
                          './check_sheet.xlsx'
    }

    def get_output_evidence() {
        return this.config?.output_evidence ?: config?.evidence?.target ?:
                               './check_sheet.xlsx'
    }

    def get_sheet_prefixes() {
        return this.config?.evidence?.sheet_prefix
    }

    def get_result_dir() {
        return this.config?.evidence?.result_dir ?: './build/json/'
    }

    def get_filter_server() {
        return this.config?.filter_server
    }

    def get_filter_metric() {
        return this.config?.filter_metric
    }

    def get_parallel_degree() {
        return this.config?.parallel_degree ?: 0
    }

    def get_snapshot_level() {
        if ("${this.config?.snapshot_level}" == '[:]')
            return 0
        else
            return this.config?.snapshot_level
    }

    def get_item_map() {
        return this.config?.report?.item_map
    }

    def get_dry_run(String platform) {
        def config_platform = this.config?.test?."${platform}"
        return this.config?.dry_run ?: config_platform?.dry_run ?: false
    }

    def get_timeout(String platform) {
        def config_platform = this.config?.test?."${platform}"
        return this.config?.timeout ?: config_platform?.timeout ?: 0
    }

    def get_debug(String platform) {
        def config_platform = this.config?.test?."${platform}"
        return this.config?.debug ?: config_platform?.debug ?: false
    }

    def get_dry_run_staging_dir(String platform) {
        return this.config?.test?.dry_run_staging_dir ?: './src/test/resources/log'
    }

    def get_evidence_log_share_dir() {
        return this.config?.evidence?.staging_dir ?: './build/log/'
    }

    def get_evidence_log_dir(String platform, String target) {
        def evidence_log_share_dir = this.get_evidence_log_share_dir()
        return "${evidence_log_share_dir}/${target}"
    }

    def print_config() {
        println "getconfig_home :  ${get_getconfig_home()}"
        println "project_home :    ${get_project_home()}"
        println "project_name :    ${get_project_name()}"
        println "tenant_name :     ${get_tenant_name()}"
        println "last_run_config : ${get_last_run_config()}"
        println "db_config :       ${get_db_config()}"
        println "node_dir :        ${get_node_dir()}"
        println "test_resource :   ${get_test_resource()}"
        println "silent :          ${get_silent()}"
        println "excel_file :      ${get_excel_file()}"
        println "output_evidence : ${get_output_evidence()}"
        println "result_dir :      ${get_result_dir()}"
        println "filter_server :   ${get_filter_server()}"
        println "filter_metric :   ${get_filter_metric()}"
        println "parallel_degree : ${get_parallel_degree()}"
    }

    def accept(visitor) {
        visitor.set_environment(this)
    }
}

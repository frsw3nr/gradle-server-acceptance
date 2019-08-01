package jp.co.toshiba.ITInfra.acceptance

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.Model.RunStatus
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import org.apache.commons.io.FileUtils

import java.nio.file.Paths
import java.nio.file.Files

// import groovy.json.JsonSlurper
// import groovy.sql.Sql

// import java.sql.SQLException

@Slf4j
@Singleton
@ToString(includePackage = false)
class InventoryDB2 {

    String base_test_log_dir
    String project_test_log_dir
    String base_node_dir
    String project_node_dir
    String project_name
    String filter_node
    String filter_platform
    static final String NODE_LIST_FORMAT = "%-18s %-18s %-30s %-20s %s"
    static final String[] NODE_LIST_HEADERS = ["Directory", "Node", "Platform", 'LastUpdated', 'Commit']
    Boolean match_project_base_directory = false

    def set_environment(ConfigTestEnvironment env) {
        this.base_test_log_dir    = env.get_base_test_log_dir()
        this.project_test_log_dir = env.get_project_test_log_dir()
        this.base_node_dir        = env.get_base_node_dir()
        this.project_node_dir     = env.get_project_node_dir()
        this.project_name         = env.get_project_name()
    }

    def check_project_base_directory_match() {
        def is_match = false
        try {
            def project_node_path = Paths.get(this.project_node_dir).toRealPath()
            def base_node_path    = Paths.get(this.base_node_dir).toRealPath()
            if (project_node_path == base_node_path)
                is_match = true
        } catch (Exception e) {
            is_match = false
        }
        return is_match
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

    Boolean check_node_file_exist(String target, String platform) {
        Boolean is_exist = false
        [base_node_dir, project_node_dir].each { node_dir ->
            String node_path = "${node_dir}/${target}/${platform}.json"
            if (Files.exists(Paths.get(node_path))) {
                is_exist = true
            }
        }
        return is_exist
    }

    int print_node_list(String mode, String test_log_dir) {
        def node_updates = [:]
        def node_testeds = [:]
        def node_platforms = [:].withDefault{[]}
        def test_log_dir_file = new File(test_log_dir)
        try {
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
                    def node_exsist = check_node_file_exist(node_name, platform)
                    node_testeds[node_name] = node_exsist
                    node_platforms[node_name] << platform
                    def last_modified = new Date(platform_dir.lastModified()).format('yyyy/MM/dd HH:mm:ss')
                    node_updates[node_name] = last_modified
                }
            }
        } catch (FileNotFoundException e) {
            log.error("Not running under the project directory, '${test_log_dir}' not found.")
        }

        def node_count = 0
        // Map node_updates_sort = node_updates.sort { a, b -> 
        //     a.value.priority() <=> b.value.priority() 
        // }
        node_updates.sort().each { node_name, last_modified ->
            def platforms = node_platforms[node_name]
            def tested = node_testeds[node_name]
            println String.format(NODE_LIST_FORMAT, mode, node_name, platforms, last_modified, tested)
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
        if (this.project_test_log_dir != null &&
            this.project_node_dir != null &&
            !(this.check_project_base_directory_match())
            ) {
            row += print_node_list('Project', this.project_test_log_dir)
        }
        row += print_node_list('Common', this.base_test_log_dir)

        if (row == 0)
            println "No data"
        else 
            println "${row} rows"
    }

    def copy_base_node_dir(String target) throws IOException {
        def source_dir = new File("${this.base_node_dir}/${target}")
        def target_dir = new File("${this.project_node_dir}/${target}")
        if (source_dir.exists()) {
            target_dir.mkdirs()
            FileUtils.copyDirectory(source_dir, target_dir)
        }
    }

    def copy_base_node_json_file(String target, String platform) throws IOException {
        def json_file = "${target}__${platform}.json"
        def source_json = new File("${this.base_node_dir}/${json_file}")
        def target_json = new File("${this.project_node_dir}/${json_file}")
        if (source_json.exists())
            target_json << source_json.text
    }

    def copy_base_test_log_dir(String target, String platform) throws IOException {
        def source_dir = new File("${this.base_test_log_dir}/${target}/${platform}")
        def target_dir = new File("${this.project_test_log_dir}/${target}/${platform}")
        if (source_dir.exists()) {
            target_dir.mkdirs()
            FileUtils.copyDirectory(source_dir, target_dir)
        }
    }

    def copy_compare_target_inventory_data(TestScenario test_scenario) {
        if (this.match_project_base_directory)
            return
        def domain_metrics = test_scenario.test_metrics.get_all()
        def targets = test_scenario.test_targets.get_all()
        targets.each { target_name, domain_targets ->
            Boolean first_loop = true
            domain_targets.each { domain, test_target ->
                if (test_target.target_status == RunStatus.INIT &&
                    test_target.comparision == true) {
                    if (first_loop) {
                        copy_base_node_dir(target_name)
                        first_loop = false
                    }
                    def platform_metrics = domain_metrics[domain].get_all()
                    platform_metrics.each { platform_name, platform_metric ->
                        copy_base_node_json_file(target_name, platform_name)
                        copy_base_test_log_dir(target_name, platform_name)
                    }
                }
            }
        }

    }

}

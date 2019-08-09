package jp.co.toshiba.ITInfra.acceptance

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.Model.RunStatus
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import org.apache.commons.io.FileUtils

import java.nio.file.Paths
import java.nio.file.Files

@Slf4j
@Singleton
@ToString(includePackage = false)
class InventoryDB {

    String filter_node
    String filter_platform
    static final String NODE_LIST_FORMAT = "%-18s %-18s %-30s %-20s %s"
    static final String[] NODE_LIST_HEADERS = ["Directory", "Node", "Platform", 'LastUpdated', 'Commit']
    Boolean match_project_base_directory = false

    def set_environment(ConfigTestEnvironment env) {
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

    int print_node_list(String mode, LogStage stage) {
        def node_updates = [:]
        def node_testeds = [:]
        def node_platforms = [:].withDefault{[]}
        String test_log_dir = LogFile.getLogDir(stage)
        // def test_log_dir_file = new File(test_log_dir)
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
                    def nodePath = NodeFile.searchPath(node_name, platform)
                    node_testeds[node_name] = (nodePath) ? true : false
                    node_platforms[node_name] << platform
                    def last_modified = new Date(platform_dir.lastModified()).format('yyyy/MM/dd HH:mm:ss')
                    node_updates[node_name] = last_modified
                }
            }
        } catch (FileNotFoundException e) {
            log.error("Not running under the project directory, '${test_log_dir}' not found.")
        }

        def node_count = 0
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
        if (LogFile.defined(LogStage.PROJECT) && 
            LogFile.matchDir(LogStage.BASE, LogStage.PROJECT)) {
            row += print_node_list('Project', LogStage.PROJECT)
        }
        row += print_node_list('Common', LogStage.BASE)
        if (row == 0)
            println "No data"
        else 
            println "${row} rows"
    }

    def copy_compare_target_inventory_data(TestScenario test_scenario) {
        if (LogFile.matchDir(LogStage.BASE, LogStage.PROJECT))
            return
        def domain_metrics = test_scenario.test_metrics.get_all()
        def test_targets = TestTarget.find(test_scenario, null, [RunStatus.INIT])
        test_targets.each { test_target ->
            if (test_target.comparision == true) {
                def target = test_target.name
                def domain = test_target.domain
                NodeFile.copyTargetJsons(target, LogStage.BASE, LogStage.PROJECT)
                def platform_metrics = domain_metrics[domain].get_all()
                platform_metrics.each { platform, platform_metric ->
                    NodeFile.copyPlatform(target, platform, LogStage.BASE, LogStage.PROJECT)
                    LogFile.copyPlatform(target, platform, LogStage.BASE, LogStage.PROJECT)
                }
            }
        }

        // def targets = test_scenario.test_targets.get_all()
        // targets.each { target, domain_targets ->
        //     Boolean first_loop = true
        //     domain_targets.each { domain, test_target ->
        //         if (test_target.target_status == RunStatus.INIT &&
        //             test_target.comparision == true) {
        //             if (first_loop) {
        //                 NodeFile.copyTargetJsons(target, LogStage.BASE, LogStage.PROJECT)
        //                 first_loop = false
        //             }
        //             def platform_metrics = domain_metrics[domain].get_all()
        //             platform_metrics.each { platform, platform_metric ->
        //                 NodeFile.copyPlatform(target, platform, LogStage.BASE, LogStage.PROJECT)
        //                 LogFile.copyPlatform(target, platform, LogStage.BASE, LogStage.PROJECT)
        //             }
        //         }
        //     }
        // }

    }

}

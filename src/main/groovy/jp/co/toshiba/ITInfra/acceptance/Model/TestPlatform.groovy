package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
// import jp.co.toshiba.ITInfra.acceptance.TestLog
import jp.co.toshiba.ITInfra.acceptance.LogFile
import jp.co.toshiba.ITInfra.acceptance.NodeFile
import jp.co.toshiba.ITInfra.acceptance.LogStage

@CompileStatic
@Slf4j
@ToString(includePackage = false, excludes="test_target")
class TestPlatform extends SpecModel {
    String name
    TestTarget test_target
    RunStatus platform_status = RunStatus.INIT
    LinkedHashMap<String,TestResult> test_results  = [:]
    LinkedHashMap<String,PortList> port_lists  = [:]
    LinkedHashMap<String,TestMetric> test_metrics  = [:]
    LinkedHashMap<String,TestMetric> added_test_metrics  = [:]
    // TestRule test_rule
    boolean verify_test
    boolean dry_run
    int timeout
    boolean debug
    String project_test_log_dir
    String evidence_log_share_dir
    String current_test_log_dir

    @CompileDynamic
    def accept(visitor){
        visitor.visit_test_platform(this)
    }

    String getDomain() {
        return this.test_target?.domain
    }

    String getTarget() {
        return this.test_target?.name
    }

    String getPlatform() {
        return this.name
    }

    def count_test_result_status() {
        Map<String,Integer> counts = [:].withDefault{0}
        test_results.each { String name, TestResult test_result ->
            String status = test_result.status ?: 'Other'
            counts[status] ++
        }
        return counts
    }

    def set_environment(ConfigTestEnvironment env) {
        String platform    = this.name
        String target_name = this.test_target?.name

        this.verify_test            = env.get_verify_test()
        this.dry_run                = env.get_dry_run(platform)
        this.timeout                = env.get_timeout(platform)
        this.debug                  = env.get_debug(platform)

        this.project_test_log_dir   = LogFile.getLogDir(LogStage.PROJECT)
        this.evidence_log_share_dir = LogFile.getLogDir(LogStage.CURRENT)
        this.current_test_log_dir   = env.get_current_platform_test_log_dir(platform, target_name)
        // TestLog.getTargetLogDir(target_name)
        
        def msg = "$target_name(DryRun=${this.dry_run})"
        log.debug "Set test $platform:$msg"
    }

    def add_test_metric(String metric, String description) {
        TestMetric test_metric
        if (this.added_test_metrics.containsKey(metric)) {
            test_metric = added_test_metrics[metric]
        } else {
            def matcher = (metric =~ /^(.+?)\./).each { m0, String base_metric_name ->
                TestMetric base_metric = this.test_metrics[base_metric_name]
                if (base_metric) {
                    test_metric = base_metric.clone()
                    test_metric.name = metric
                    test_metric.description = description
                    test_metric.comment = ''
                    test_metric.enabled = false
                    test_metric.device_enabled = false
                    // this.added_test_metrics[metric] = test_metric
                    this.added_test_metrics.put(metric, test_metric)
                }
            }
            if (!test_metric) {
                test_metric = new TestMetric(name: metric, description: description,
                                             platform: this.name,
                                             enabled: true)
            }
        }
    }

    @CompileDynamic
    private static List<TestPlatform> findBase(TestScenario test_scenario, String keyword = null, 
                                       List<RunStatus> filter_status = null, Boolean exclude = false) {
        List<TestPlatform> test_platforms = new ArrayList<>()
        List<TestTarget> test_targets = TestTarget.findBase(test_scenario, keyword, filter_status, exclude)
        test_targets.each { test_target ->
            test_target.test_platforms.each { platform, TestPlatform test_platform ->
                test_platforms << test_platform
            }
        }
        return test_platforms
    }

    static List<TestPlatform> findNotStatus(TestScenario test_scenario, String keyword = null, 
                                            List<RunStatus> filter_status = null) {
        return findBase(test_scenario, keyword, filter_status, true)
    }

    // static List<TestPlatform> find(TestScenario test_scenario, String keyword = null, 
    //                                List<RunStatus> filter_status = null) {
    //     return findBase(test_scenario, keyword, filter_status, false)
    // }

    @CompileDynamic
    static List<TestPlatform> search(TestScenario test_scenario, SpecModelQuery q = null) {
        List<TestPlatform> test_platforms = new ArrayList<>()
        List<TestTarget> test_targets = TestTarget.search(test_scenario, q)
        test_targets.each { test_target ->
            test_target.test_platforms.each { platform, TestPlatform test_platform ->
                if (q?.platform && platform.indexOf(q?.platform) == -1) {
                    return
                }
                test_platforms << test_platform
            }
        }
        return test_platforms
    }
}

@Slf4j
@ToString(includePackage = false)
class TestPlatformSet extends SpecCompositeModel {
    String name

    // def children = new ConfigObject()

    // def add(test_platform) {
    //     test_platform.with {
    //         this.children[name] = it
    //     }
    // }

    def accept(visitor) {
        visitor.visit_test_platform_set(this)
    }

    // def get_all() {
    //     return this.children
    // }
}

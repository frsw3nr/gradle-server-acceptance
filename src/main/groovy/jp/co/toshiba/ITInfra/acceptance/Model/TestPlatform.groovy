package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*

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
    TestRule test_rule

    def accept(visitor){
        visitor.visit_test_platform(this)
    }

    def count_test_result_status() {
        def counts = [:].withDefault{0}
        test_results.each { name, test_result ->
            counts[test_result.status] ++
        }
        return counts
    }

    def set_environment(ConfigTestEnvironment env) {
        def platform    = this.name
        def target_name = this.test_target?.name

        this.verify_test            = env.get_verify_test()
        this.dry_run                = env.get_dry_run(platform)
        this.timeout                = env.get_timeout(platform)
        this.debug                  = env.get_debug(platform)
        this.dry_run_staging_dir    = env.get_dry_run_staging_dir(platform)
        this.evidence_log_share_dir = env.get_evidence_log_share_dir()
        this.evidence_log_dir       = env.get_evidence_log_dir(platform, target_name)

        def msg = "$target_name(DryRun=${this.dry_run})"
        log.debug "Set test $platform:$msg"
    }

    def add_test_metric(String metric, String description) {
        def test_metric
        if (this.added_test_metrics.containsKey(metric)) {
            test_metric = added_test_metrics[metric]
        } else {
            (metric =~/^(.+?)\./).each { m0, base_metric_name ->
                def base_metric = test_metrics[base_metric_name]
                if (base_metric) {
                    test_metric = base_metric.clone()
                    test_metric.name           = metric
                    test_metric.description    = description
                    test_metric.enabled        = false
                    test_metric.device_enabled = false
                    this.added_test_metrics[metric] = test_metric
                }
            }
            if (!test_metric) {
                test_metric = new TestMetric(name: metric, description: description,
                                             platform: this.name,
                                             enabled: true)
            }
        }
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

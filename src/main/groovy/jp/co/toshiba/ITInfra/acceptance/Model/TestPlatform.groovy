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
    LinkedHashMap<String,TestMetric> test_metrics  = [:]
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
        // def config = test_environment.config
        // def config_test = config.test
        // def platform    = this.name
        // def target_name = this.test_target?.name
        // def evidence_log_share_dir = config?.evidence?.staging_dir ?: './build/log/'
        // // evidence_log_share_dir += '/' + platform
        // def config_platform = config_test[platform]
        // def test_platform_configs = [
        //     'dry_run'                : config.dry_run ?: config_platform.dry_run ?: false,
        //     'timeout'                : config.timeout ?: config_platform.timeout ?: 0,
        //     'debug'                  : config.debug ?: config_platform.debug ?: false,
        //     'dry_run_staging_dir'    : config_test.dry_run_staging_dir ?:
        //                                './src/test/resources/log',
        //     'evidence_log_share_dir' : evidence_log_share_dir,
        //     'evidence_log_dir'       : evidence_log_share_dir + '/' + target_name,
        // ]

        // test_platform_configs.each { key, test_platform_config ->
        //     if (!this?."$key")
        //         this."$key" = test_platform_config
        //     // test_platform[key] = test_platform_config
        // }
        // def msg = "$target_name(DryRun=${this.dry_run})"
        // log.debug "Set test $platform:$msg"
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

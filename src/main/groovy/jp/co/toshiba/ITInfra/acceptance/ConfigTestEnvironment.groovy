package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// 見直し案

// 共通変数

// * config_test
// * platform
// * target_name
// * evidence_log_share_dir

// テストプラットフォーム用変数

// * dry_run
// * timeout
// * debug
// * dry_run_staging_dir
// * evidence_log_share_dir
// * evidence_log_dir

// エビデンス出力用変数

// json_dir
// evidence_target
// evidence_source

// 目的に合わせて環境変数設定ポリシーを変える

// テストコード内で環境変数設定

//         def test_env = ConfigTestEnvironment.instance
//         test_env.read_config('config/config.groovy')
//         test_env.json_dir = 'src/test/resources/json'
//         test_env.evidence_source = 'src/test/resources/check_sheet.xlsx'
//         test_env.evidence_target = 'build/check_sheet.xlsx'
//         後から設定された変数が優先される

// 実行オプション設定

//         def test_env = ConfigTestEnvironment.instance
//         test_env.read_test_runner_config(test_runner)
//             test_env.read_config(config_file)
//         test_runner で設定された変数が優先される
//         set_test_runner_config()内で、config_file を先に読み込む

// 以下、メソッドで必要な変数をセットする

// set_test_environment(platform_tester)
// set_evidence_environment(evidence_maker),
// set_test_environment(object)


@Slf4j
@ToString(includePackage = false)
@Singleton
// class ConfigTestEnvironment extends SpecModel {
class ConfigTestEnvironment {
    String config_file
    ConfigObject config

    def read_config(String config_file = 'config/config.groovy') {
        this.config_file = config_file
        this.config = Config.instance.read(config_file)
    }

    def read_from_test_runner(TestRunner test_runner) {
        def test_runner_config = test_runner.getProperties() as Map

        this.config_file = test_runner.config_file ?: './config/config.groovy'
        this.config = Config.instance.read(config_file)

        test_runner.getProperties().findAll{ name, value ->
            if (name == 'class')
                return
            this.config."$name" = value
        }
        println "TEST_RUNNER2"
        def json = new groovy.json.JsonBuilder()
        json(this.config)
        println json.toPrettyString()
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
            if (test_target.os_specific_password)
                os_account.password = test_target.os_specific_password
        }
    }

    def accept(visitor) {
        visitor.set_environment(this)
    }

    // def set_test_platform_environment(TestPlatform test_platform) {
    //     def config_test = config.test
    //     def platform    = test_platform.name
    //     def target_name = test_platform.test_target?.name
    //     def evidence_log_share_dir = config?.evidence?.staging_dir ?: './build/log/'
    //     // evidence_log_share_dir += '/' + platform
    //     def config_platform = config_test[platform]
    //     def test_platform_configs = [
    //         'dry_run'                : config.dry_run ?: config_platform.dry_run ?: false,
    //         'timeout'                : config.timeout ?: config_platform.timeout ?: 0,
    //         'debug'                  : config.debug ?: config_platform.debug ?: false,
    //         'dry_run_staging_dir'    : config_test.dry_run_staging_dir ?:
    //                                    './src/test/resources/log',
    //         'evidence_log_share_dir' : evidence_log_share_dir,
    //         'evidence_log_dir'       : evidence_log_share_dir + '/' + target_name,
    //     ]

    //     test_platform_configs.each { key, test_platform_config ->
    //         if (!test_platform[key])
    //             test_platform[key] = test_platform_config
    //         // test_platform[key] = test_platform_config
    //     }
    //     def msg = "$target_name(DryRun=${test_platform.dry_run})"
    //     log.debug "Set test $platform:$msg"
    // }

    // def set_test_schedule_environment(TestScheduler test_scheduler) {
    //     test_scheduler.with {
    //         it.excel_file      = config.excel_file      ?: config.evidence?.source ?:
    //                              './check_sheet.xlsx'
    //         it.output_evidence = config.output_evidence ?: config.evidence?.target ?:
    //                              './check_sheet.xlsx'
    //         it.json_dir = config?.evidence?.json_dir ?: './build/json/'

    //         log.info "Schedule options : "
    //         log.info "excel file    : " + it.excel_file
    //         log.info "output        : " + it.output_evidence
    //         if (config.filter_server) {
    //             it.filter_server = config.filter_server
    //             log.info "filter servers : " + it.filter_server
    //         }
    //         if (config.filter_metric) {
    //             it.filter_metric = config.filter_metric
    //             log.info "filter metrics : " + it.filter_metric
    //         }
    //         if (config.parallel_degree) {
    //             it.parallel_degree = config.parallel_degree
    //             log.info "\tparallel degree  : " + it.parallel_degree
    //         }
    //     }
    // }

    // def set_test_result_reader(TestResultReader test_result_reader) {
    //     test_result_reader.with {
    //         it.json_dir = config?.evidence?.json_dir ?: './build/json/'
    //     }
    // }

    // def set_evidence_environment(EvidenceMaker evidence_maker) {
    //     def config_test = config.test
    //     evidence_maker.json_dir        = config?.evidence?.json_dir ?: './build/json/'
    //     evidence_maker.evidence_target = config?.evidence?.target ?: './build/check_sheet.xlsx'
    //     evidence_maker.evidence_source = config?.evidence?.source ?: './check_sheet.xlsx'
    // }
}

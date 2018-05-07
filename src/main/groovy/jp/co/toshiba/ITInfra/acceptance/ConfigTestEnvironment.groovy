package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
class ConfigTestEnvironment {
    String config_file
    ConfigObject config

    ConfigTestEnvironment(String config_file = 'config/config.groovy') {
        this.config_file = config_file
        this.config = Config.instance.read(config_file)
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
        def config_account = config['account']
        if (!config_account) {
            def msg = "Not found parameter 'account.{platform}.{id}' in ${config_file}"
            log.error(msg)
            throw new IllegalArgumentException(msg)
        }
        test_platform.with {
            os_account = get_config_account(config_account, platform, test_target.os_account_id)
            if (test_target.os_specific_password)
                os_account.password = test_target.os_specific_password
        }
    }

    def set_test_environment(TestPlatform test_platform) {
        def config_test = config['test']
        def platform    = test_platform.name
        def target_name = test_platform.test_target.name
        def evidence_log_share_dir = config['evidence']['staging_dir'] ?: './build/log/'
        evidence_log_share_dir += '/' + platform

        def test_platform_configs = [
            'dry_run'                : config_test[platform]['dry_run'] ?: false,
            'timeout'                : config_test[platform]['timeout'] ?: 0,
            'debug'                  : config_test[platform]['debug'] ?: false,
            'dry_run_staging_dir'    : config_test['dry_run_staging_dir'] ?: './src/test/resources/log',
            'evidence_log_share_dir' : evidence_log_share_dir,
            'evidence_log_dir'       : evidence_log_share_dir + '/' + target_name,
        ]

        test_platform_configs.each { key, test_platform_config ->
            if (!test_platform[key])
                test_platform[key] = test_platform_config
        }
    }
}

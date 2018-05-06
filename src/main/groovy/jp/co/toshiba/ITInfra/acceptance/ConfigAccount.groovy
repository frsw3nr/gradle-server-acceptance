package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@Singleton
@ToString(includePackage = false)
class ConfigAccount {
    String config_file
    String server_name
    String ip
    String platform
    String os_account_id
    String remote_account_id
    String remote_alias
    String verify_id
    String compare_source = 'actual'
    String compare_server
    String evidence_log_dir
    String evidence_log_share_dir
    Boolean dry_run
    String dry_run_staging_dir
    int timeout
    boolean debug

    Map infos = [:]
    def os_account
    def remote_account

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

    def set_test_env(TestTarget test_target, config_file = 'config/config.groovy') {
        this.config_file = config_file
        def config = Config.instance.read(config_file)
        def platform = test_target.platform
        def config_test = config['test']
        test_target.dry_run = config_test[platform]['dry_run'] ?: false
        test_target.timeout = config_test[platform]['timeout'] ?: 0
        test_target.debug   = config_test[platform]['debug'] ?: false
        test_target.dry_run_staging_dir = config_test['dry_run_staging_dir'] ?: './src/test/resources/log/'
        test_target.evidence_log_share_dir  = config['evidence']['staging_dir'] ?: './build/log/'
        test_target.evidence_log_share_dir += '/' + platform
        test_target.evidence_log_dir        = evidence_log_share_dir + '/' + server_name
    }

    def set_account(TestTarget test_target, config_file = 'config/config.groovy') {
        this.config_file = config_file
        def config = Config.instance.read(config_file)
        def platform = test_target.platform
        def config_account = config['account']
        if (!config_account) {
            def msg = "Not found parameter 'account.{platform}.{id}' in ${config_file}"
            log.error(msg)
            throw new IllegalArgumentException(msg)
        }
        os_account     = get_config_account(config_account, platform,  os_account_id)
        remote_account = get_config_account(config_account, 'Remote', remote_account_id)
        if (!test_target.os_specific_password)
            test_target.os_specific_password = os_account['password']
    }

    def info() {
        "host=${server_name},ip=${ip}"
    }
}

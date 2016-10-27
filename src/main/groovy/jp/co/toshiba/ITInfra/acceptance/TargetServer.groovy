package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j

@Slf4j
class TargetServer {
    String config_file
    String server_name
    String ip
    String platform
    String os_account_id
    String remote_account_id
    String remote_alias
    String verify_id
    String evidence_log_dir
    Boolean dry_run
    String dry_run_staging_dir
    int timeout

    Map infos = [:]
    def os_account
    def remote_account

    TargetServer(Map properties) {
        final def defalut_props = ['server_name' : 1, 'ip' : 1, 'platform' : 1,
            'os_account_id' : 1, 'remote_account_id' : 1, 'remote_alias' : 1, 'verify_id' : 1]
        properties.each { name, value ->
            if (defalut_props.containsKey(name)) {
                this."${name}" = value
            }
            this.infos[name] = value
        }
    }

    private getConfigAccount(Map config_account, String platform, String id) {
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

    def setAccounts(config_file = 'config/config.groovy') {
        this.config_file = config_file
        def config = Config.instance.read(config_file)
        def config_account = config['account']
        if (!config_account) {
            def msg = "Not found parameter 'account.{platform}.{id}' in ${config_file}"
            log.error(msg)
            throw new IllegalArgumentException(msg)
        }
        os_account      = getConfigAccount(config_account, platform,  os_account_id)
        remote_account = getConfigAccount(config_account, 'Remote', remote_account_id)

        def config_test = config['test']
        dry_run = config_test[platform]['dry_run'] ?: false
        timeout = config_test[platform]['timeout'] ?: 0
        dry_run_staging_dir = config_test['dry_run_staging_dir'] ?: './test/resources/log/'
        evidence_log_dir = config['evidence']['staging_dir'] ?: './build/log/'
        evidence_log_dir += '/' + platform + '/' + server_name
    }

    def info() {
        "host=${server_name},ip=${ip}"
    }
}

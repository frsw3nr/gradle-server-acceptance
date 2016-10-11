package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j

@Slf4j
class TargetServer {
    String config_file
    String server_name
    String ip
    String platform
    String os_account_id
    String vcenter_id
    String vm
    Boolean dry_run
    int timeout

    def os_account
    def vcenter_account

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
        vcenter_account = getConfigAccount(config_account, 'vCenter', vcenter_id)

        def config_test = config['test'][platform]
        dry_run = config_test['dry_run'] ?: false
        timeout = config_test['timeout'] ?: 0
    }

    def info() {
        "host=${server_name},ip=${ip}"
    }
}

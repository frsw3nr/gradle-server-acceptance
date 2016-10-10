package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j

@Slf4j
class TargetServer {
    String server_name
    String ip
    String platform
    String os_account_id
    String vcenter_id
    String vm

    def os_account
    def vcenter_account

    private config_account
    private getConfig(String platform, String id) {
        def account = [:]
        if (id) {
            account = config_account[platform][id]
            if (!account) {
                def msg = "Not found parameter 'account.${platform}.${id}' in config file"
                log.error(msg)
                throw new IllegalArgumentException(msg)
            }
        }
        return account
    }

    def setAccounts(config_file = 'config/config.groovy') {
        config_account  = Config.instance.read(config_file)['account']
        if (!config_account) {
            def msg = "Not found parameter 'account.{platform}.{id}' in config file"
            log.error(msg)
            throw new IllegalArgumentException(msg)
        }
        os_account      = getConfig(platform,  os_account_id)
        vcenter_account = getConfig('vCenter', vcenter_id)
    }

    def info() {
        "host=${server_name},ip=${ip}"
    }
}

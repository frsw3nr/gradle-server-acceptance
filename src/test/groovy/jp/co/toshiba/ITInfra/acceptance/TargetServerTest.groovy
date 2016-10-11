import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon clean test --tests "TargetServerTest"

class TargetServerTest extends Specification {

    TargetServer test_server

    def setup() {
        test_server = new TargetServer(
            server_name   : 'ostrich',
            ip            : '192.168.10.1',
            platform      : 'Linux',
            os_account_id : 'Test',
            vcenter_id    : 'Test',
            vm            : 'ostrich',
        )
    }

    def "Linux テストサーバセット"() {
        when:
        test_server.setAccounts('src/test/resources/config.groovy')

        then:
        test_server.os_account['password'] == 'P@ssword'
        test_server.vcenter_account['password'] == 'P@ssword'
        test_server.dry_run == false
        test_server.timeout == 30
    }

    def "設定ファイルにaccountパラメータなし"() {
        when:
        test_server.setAccounts('src/test/resources/config_null.groovy')

        then:
        thrown(IllegalArgumentException)
    }

    def "Linux vCenter アカウント情報なし"() {
        setup:
        test_server.vcenter_id = null

        when:
        test_server.setAccounts('src/test/resources/config.groovy')

        then:
        test_server.vcenter_account == [:]
    }

    def "Linux OSアカウント情報なし"() {
        setup:
        test_server.os_account_id = 'Hoge'

        when:
        test_server.setAccounts('src/test/resources/config.groovy')
        println test_server.os_account

        then:
        thrown(IllegalArgumentException)
    }
}

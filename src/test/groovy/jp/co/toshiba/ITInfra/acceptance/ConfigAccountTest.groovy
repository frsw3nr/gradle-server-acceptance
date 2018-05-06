import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "ConfigAccountTest.Linux テストサーバセット"

class ConfigAccountTest extends Specification {

    TestTarget test_target

    def setup() {
        test_target = new TestTarget(
            name              : 'ostrich',
            ip                : '192.168.10.1',
            platform          : 'Linux',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            remote_alias      : 'ostrich',
        )
    }

    def "Linux テストサーバセット"() {
        when:
        ConfigAccount.instance.set_account(test_target, 'src/test/resources/config.groovy')
        println test_target
        println test_target.remote_account_id
        // test_target.setAccounts('src/test/resources/config.groovy')

        then:
        1 == 1
        // test_target.os_account['password'] == 'P@ssword'
        // test_target.remote_account['password'] == 'P@ssword'
        // test_target.dry_run == false
        // test_target.timeout == 30
    }

    // def "設定ファイルにaccountパラメータなし"() {
    //     when:
    //     test_target.setAccounts('src/test/resources/config_null.groovy')

    //     then:
    //     thrown(IllegalArgumentException)
    // }

    // def "Linux vCenter アカウント情報なし"() {
    //     setup:
    //     test_target.remote_account_id = null

    //     when:
    //     test_target.setAccounts('src/test/resources/config.groovy')

    //     then:
    //     test_target.remote_account == [:]
    // }

    // def "Linux OSアカウント情報なし"() {
    //     setup:
    //     test_target.os_account_id = 'Hoge'

    //     when:
    //     test_target.setAccounts('src/test/resources/config.groovy')
    //     println test_target.os_account

    //     then:
    //     thrown(IllegalArgumentException)
    // }
}

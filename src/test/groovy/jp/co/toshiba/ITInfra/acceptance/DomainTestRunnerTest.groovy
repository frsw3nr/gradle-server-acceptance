import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

class DomainTestRunnerTest extends Specification {

    def "ドメインテストの初期化"() {
        when:
        def test_server = new TargetServer(
            server_name   : 'ostrich',
            ip            : 'localhost',
            platform      : 'Linux',
            os_account_id : 'Test',
            vcenter_id    : 'Test',
            vm            : 'ostrich',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
        def test = new DomainTestRunner(test_server, 'Linux')

        then:
        test != null
    }

}

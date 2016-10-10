import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

class vCenterBaseTest extends Specification {

    DomainTestRunner test

    def setup() {
        def test_server = new TargetServer(
            server_name   : 'win2012',
            ip            : '192.168.0.12',
            platform      : 'vCenter',
            os_account_id : 'Test',
            vcenter_id    : 'Test',
            vm            : 'win2012.ostrich',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
        test = new DomainTestRunner(test_server, 'vCenter')
    }

    def "vCenter テスト仕様のロード"() {
        when:
        def test_item = new TestItem('vm')
        test.run(test_item)

        then:
        test_item.result.size() > 0
    }

}

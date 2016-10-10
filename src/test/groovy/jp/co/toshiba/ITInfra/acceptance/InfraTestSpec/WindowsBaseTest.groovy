import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

class WindowsBaseTest extends Specification {

    DomainTestRunner test

    def setup() {
        def test_server = new TargetServer(
            server_name   : 'win2012',
            ip            : '192.168.0.12',
            platform      : 'Windows',
            os_account_id : 'Test',
            vcenter_id    : 'Test',
            vm            : 'win2012.ostrich',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
        test = new DomainTestRunner(test_server, 'Windows')
    }

    def "Windows テスト仕様のロード"() {
        when:
        def test_item = new TestItem('cpu')
        test.run(test_item)

        then:
        test_item.result.size() > 0
    }

    def "Windows 複数テスト仕様のロード"() {
        when:
        TestItem[] test_items = [new TestItem('cpu'), new TestItem('memory') ]
        test.run(test_items)

        then:
        test_items[0].result.size() > 0
        test_items[1].result.size() > 0
    }
}

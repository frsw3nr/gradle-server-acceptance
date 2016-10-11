import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradle --daemon clean test --tests "LinuxBaseTest.Linux テスト仕様のロード"

class LinuxBaseTest extends Specification {

    DomainTestRunner test

    def setup() {
        def test_server = new TargetServer(
            server_name   : 'ostrich',
            ip            : '192.168.10.1',
            platform      : 'Linux',
            os_account_id : 'Test',
            vcenter_id    : 'Test',
            vm            : 'ostrich',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
        test = new DomainTestRunner(test_server, 'Linux')
    }

    def "Linux テスト仕様のロード"() {
        when:
        def test_item = new TestItem('hostname')
        test.run(test_item)

        then:
        test_item.results.size() > 0
    }

    def "Linux 複数テスト仕様のロード"() {
        when:
        TestItem[] test_items = [new TestItem('hostname'), new TestItem('hostname_fqdn') ]
        test.run(test_items)

        then:
        test_items[0].results.size() > 0
        test_items[1].results.size() > 0
    }
}

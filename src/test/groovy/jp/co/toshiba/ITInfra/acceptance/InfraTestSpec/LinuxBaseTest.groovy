import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradle --daemon clean test --tests "LinuxBaseTest.Linux テスト仕様のロード"

class LinuxBaseTest extends Specification {

    TargetServer test_server
    DomainTestRunner test

    def setup() {
        test_server = new TargetServer(
            server_name   : 'ostrich',
            ip            : 'localhost',
            platform      : 'Linux',
            os_account_id : 'Test',
            vcenter_id    : 'Test',
            vm            : 'ostrich',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
    }

    def "Linux テスト仕様のロード"() {
        setup:
        test = new DomainTestRunner(test_server, 'Linux')

        when:
        def test_item = new TestItem('hostname')
        test.run(test_item)

        then:
        test_item.results.size() > 0
    }

    def "Linux ドライランテスト"() {
        setup:
        test_server.dry_run = true
        test = new DomainTestRunner(test_server, 'Linux')

        when:
        def test_item = new TestItem('hostname')
        test.run(test_item)

        then:
        test_item.results.size() > 0
    }

    def "Linux 複数テスト仕様のロード"() {
        setup:
        test = new DomainTestRunner(test_server, 'Linux')

        when:
        TestItem[] test_items = [new TestItem('hostname'), new TestItem('hostname_fqdn') ]
        test.run(test_items)

        then:
        test_items[0].results.size() > 0
        test_items[1].results.size() > 0
    }
}

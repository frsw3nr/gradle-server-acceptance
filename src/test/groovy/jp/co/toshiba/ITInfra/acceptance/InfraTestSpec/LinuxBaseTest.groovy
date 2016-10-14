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
        test_server.dry_run = true
        test = new DomainTestRunner(test_server, 'Linux')

        when:
        def test_item = new TestItem('hostname')
        test.run(test_item)

        then:
        println test_item.results.toString()
        test_item.results.size() > 0
    }

    def "Linux 複数テスト仕様のロード"() {
        setup:
        test_server.dry_run = true
        test = new DomainTestRunner(test_server, 'Linux')

        when:
        TestItem[] test_items = [new TestItem('hostname'), new TestItem('hostname_fqdn'), new TestItem('cpu')]
        test.run(test_items)
        [0..2].each {
            println test_items[it].results.toString()
       }

        then:
        // println test_item.results.toString()
        test_items[0].results.size() > 0
        test_items[1].results.size() > 0
        test_items[2].results.size() > 0
    }
}

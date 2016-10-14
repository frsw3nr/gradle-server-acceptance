import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradlew --daemon clean test --tests "WindowsBaseTest.Windows テスト仕様のロード"

class WindowsBaseTest extends Specification {

    TargetServer test_server
    DomainTestRunner test

    def setup() {
        test_server = new TargetServer(
            server_name   : 'win2012',
            ip            : '192.168.0.12',
            platform      : 'Windows',
            os_account_id : 'Test',
            vcenter_id    : 'Test',
            vm            : 'win2012.ostrich',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
        test_server.dry_run = true
    }

    def "Windows テスト仕様のロード"() {
        setup:
        test = new DomainTestRunner(test_server, 'Windows')

        when:
        def test_item = new TestItem('cpu')
        test.run(test_item)

        then:
        println test_item.results.toString()
        test_item.results.size() > 0
    }


    def "Windows メモリ容量"() {
        setup:
        test = new DomainTestRunner(test_server, 'Windows')

        when:
        def test_item = new TestItem('memory')
        test.run(test_item)

        then:
        println test_item.results.toString()
        test_item.results.size() > 0
    }


    def "Windows 複数テスト仕様のロード"() {
        setup:
        test = new DomainTestRunner(test_server, 'Windows')

        when:
        TestItem[] test_items = [new TestItem('cpu'), new TestItem('memory') ]
        test.run(test_items)

        then:
        [0..1].each {
            println test_items[it].results.toString()
        }
        test_items[0].results.size() > 0
        test_items[1].results.size() > 0
    }

    def "Windows テスト項目なし"() {
        setup:
        test = new DomainTestRunner(test_server, 'Windows')

        when:
        def test_item = new TestItem('hoge')
        test.run(test_item)

        then:
        test_item.results.size() == 0
    }

}

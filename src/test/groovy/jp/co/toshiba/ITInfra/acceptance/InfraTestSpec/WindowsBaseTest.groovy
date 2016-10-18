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

        def test_ids = ['cpu', 'memory']
        def test_items = []
        test_ids.each {
            test_items << new TestItem(it)
        }

        when:
        test.run(test_items as TestItem[])
        test_items.each { test_item ->
            println test_item.results.toString()
       }

        then:
        test_items.each { test_item ->
            test_item.results.size() > 0
        }
    }

    def "Windows 全テスト仕様のロード"() {
        setup:
        test = new DomainTestRunner(test_server, 'Windows')

        def test_ids = [
            // 'cpu',
            // 'driver',
            // 'filesystem',
            // 'fips',
            // 'memory',
            'network',
            // 'virturalization',
        ]
        def test_items = []
        test_ids.each {
            test_items << new TestItem(it)
        }

        when:
        test.run(test_items as TestItem[])
        test_items.each { test_item ->
            println test_item.results.toString()
       }

        then:
        test_items.each { test_item ->
            test_item.results.size() > 0
        }
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

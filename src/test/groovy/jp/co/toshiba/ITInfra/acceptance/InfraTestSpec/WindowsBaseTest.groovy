import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradlew --daemon clean test --tests "WindowsBaseTest.Windows テスト仕様のロード"

class WindowsBaseTest extends Specification {

    TargetServer test_server
    DomainTestRunner test

    def setup() {
        test_server = new TargetServer(
            server_name       : 'win2012',
            ip                : '192.168.0.12',
            platform          : 'Windows',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            remote_alias      : 'win2012.ostrich',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
        test_server.dry_run = true
    }

    def "Windows テスト仕様 cpu"() {
        setup:
        test = new DomainTestRunner(test_server, 'Windows')

        when:
        def test_item = new TestItem('cpu')
        test.run(test_item)

        then:
        test_item.results.size() > 0
    }


    def "Windows テスト仕様 memory"() {
        setup:
        test = new DomainTestRunner(test_server, 'Windows')

        when:
        def test_item = new TestItem('memory')
        test.run(test_item)

        then:
        test_item.results.size() > 0
    }

    def "Windows テスト仕様 system"() {
        setup:
        test = new DomainTestRunner(test_server, 'Windows')

        when:
        def test_item = new TestItem('system')
        test.run(test_item)

        then:
        println test_item.results.toString()
        test_item.results.size() > 0
    }

    def "Windows テスト仕様 driver"() {
        setup:
        test = new DomainTestRunner(test_server, 'Windows')

        when:
        def test_item = new TestItem('driver')
        test.run(test_item)

        then:
        println test_item.results.toString()
        test_item.results.size() > 0
    }

    def "Windows テスト仕様 firewall"() {
        setup:
        test = new DomainTestRunner(test_server, 'Windows')

        when:
        def test_item = new TestItem('firewall')
        test.run(test_item)

        then:
        println test_item.results.toString()
        test_item.results.size() > 0
    }

    def "Windows テスト仕様 dns"() {
        setup:
        test = new DomainTestRunner(test_server, 'Windows')

        when:
        def test_item = new TestItem('dns')
        test.run(test_item)

        then:
        println test_item.results.toString()
        test_item.results.size() > 0
    }

    def "Windows テスト仕様 storage_timeout"() {
        setup:
        test = new DomainTestRunner(test_server, 'Windows')

        when:
        def test_item = new TestItem('storage_timeout')
        test.run(test_item)

        then:
        println test_item.results.toString()
        test_item.results.size() > 0
    }

    def "Windows テスト仕様 service"() {
        setup:
        test = new DomainTestRunner(test_server, 'Windows')

        when:
        def test_item = new TestItem('service')
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
            'cpu',
            'driver',
            'filesystem',
            'fips',
            'memory',
            'network',
            'virturalization',
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

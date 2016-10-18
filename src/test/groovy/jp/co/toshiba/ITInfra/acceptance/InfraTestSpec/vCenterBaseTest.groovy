import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradle --daemon clean test --tests "vCenterBaseTest.vCenter テスト仕様のロード"

class vCenterBaseTest extends Specification {

    TargetServer test_server
    DomainTestRunner test

    def setup() {
        test_server = new TargetServer(
            server_name   : 'testtestdb',
            ip            : '192.168.0.1',
            platform      : 'Linux',
            os_account_id : 'Test',
            vcenter_id    : 'Test',
            vm            : 'testtestdb',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
        test_server.dry_run = true
    }

    def "vCenter テスト仕様のロード"() {
        setup:
        test = new DomainTestRunner(test_server, 'vCenter')

        when:
        def test_item = new TestItem('vm')
        test.run(test_item)

        then:
        println test_item.results.toString()
        test_item.results.size() > 0
    }

    def "vCenter 全テスト仕様のロード"() {
        setup:
        test = new DomainTestRunner(test_server, 'vCenter')

        def test_ids = [
            'vm',
            'vmwaretool',
            'vm_iops_limit',
            'vm_storage',
            'vm_timesync',
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

}

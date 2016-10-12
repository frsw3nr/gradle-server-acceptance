import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradle --daemon clean test --tests "vCenterBaseTest.Linux テスト仕様のロード"

class vCenterBaseTest extends Specification {

    TargetServer test_server
    DomainTestRunner test

    def setup() {
        test_server = new TargetServer(
            server_name   : 'win2012',
            ip            : '192.168.0.12',
            platform      : 'vCenter',
            os_account_id : 'Test',
            vcenter_id    : 'Test',
            vm            : 'win2012.ostrich',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
    }

    def "vCenter テスト仕様のロード"() {
        setup:
        test = new DomainTestRunner(test_server, 'vCenter')

        when:
        def test_item = new TestItem('vm')
        test.run(test_item)

        then:
        test_item.results.size() > 0
    }

    def "vCenter ドライランテスト"() {
        setup:
        test_server.dry_run = true
        test = new DomainTestRunner(test_server, 'vCenter')

        when:
        def test_item = new TestItem('vm')
        test.run(test_item)
        println test_item.results.toString()

        then:
        test_item.results.size() > 0
    }
}

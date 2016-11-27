import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradlew --daemon clean test --tests "VMHostBaseTest.VMHost テスト仕様のロード"

class VMHostBaseTest extends Specification {

    TargetServer test_server
    DomainTestRunner test

    def setup() {
        test_server = new TargetServer(
            server_name       : 'esxi001',
            platform          : 'VMHost',
            remote_account_id : 'Test',
            remote_alias      : 'esxi001',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
        test_server.dry_run = true
    }

    def "VMHost テスト仕様 NetworkAdapter"() {
        setup:
        test = new DomainTestRunner(test_server, 'VMHost')

        when:
        def test_item = new TestItem('NetworkAdapter')
        test.run(test_item)

println test_item.results
        then:
        test_item.results.size() > 0
    }

}

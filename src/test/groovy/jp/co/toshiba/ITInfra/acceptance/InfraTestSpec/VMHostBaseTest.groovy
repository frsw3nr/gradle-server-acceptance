import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradlew --daemon test --tests "VMHostBaseTest.VMHost テスト仕様のロード"

class VMHostBaseTest extends Specification {

    def ダミーテスト() {
        when:
        println 'Test'

        then:
        1 == 1
    }

//     TargetServer test_server
//     DomainTestRunner test

//     def setup() {
//         test_server = new TargetServer(
//             server_name       : 'esxi001',
//             ip                : '192.168.10.100',
//             platform          : 'VMHost',
//             os_account_id     : 'Test',
//             remote_account_id : 'Test',
//             remote_alias      : 'esxi001',
//         )
//         test_server.setAccounts('src/test/resources/config.groovy')
//         test_server.dry_run = true
//     }

//         // vcenter_ip       = test_server.ip
//         // def os_account   = test_server.os_account
//         // vcenter_user     = os_account['user']
//         // vcenter_password = os_account['password']
//         // vm               = test_server.ip


//     def "VMHost テスト仕様 NetworkAdapter"() {
//         setup:
//         test = new DomainTestRunner(test_server, 'VMHost')

//         when:
//         def test_item = new TestItem('NetworkAdapter')
// println test_item.test_id
//         test.run(test_item)

// println test_item.results
//         then:
//         test_item.results.size() > 0
//     }

}

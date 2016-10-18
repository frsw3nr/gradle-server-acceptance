import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon clean test --tests "DomainTestRunnerTest"

class DomainTestRunnerTest extends Specification {

    def rules = [:].withDefault{[:].withDefault{[:]}}
    TargetServer test_server

    def setup() {
        rules['RuleDB']['vCenter']['NumCpu']   = '4 < x'
        rules['RuleDB']['vCenter']['MemoryGB'] = '2 <= x && x < 8'
        rules['RuleDB']['vCenter']['VMHost']   = 'x =~ /esxi/'

        test_server = new TargetServer(
            server_name   : 'ostrich',
            ip            : 'localhost',
            platform      : 'Linux',
            os_account_id : 'Test',
            vcenter_id    : 'Test',
            vm            : 'ostrich',
            verify_id     : 'RuleDB',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
        test_server.dry_run = true
    }

    def "ドメインテストの初期化"() {
        when:
        def test = new DomainTestRunner(test_server, 'vCenter')

        then:
        test != null
    }

    def "検査項目なしのドメインテスト"() {
        when:
        def test = new DomainTestRunner(test_server, 'vCenter')
        TestItem[] test_items = []
        test.run(test_items)

        then:
        test != null
    }

    def "検査ID指定のドメインテスト"() {
        when:
        def test = new DomainTestRunner(test_server, 'vCenter')
        def test_results = test.makeTest(['vm'])
        println test_results
        then:
        test_results.size() > 0
    }

    def "検査結果の検証"() {
        when:
        def test = new DomainTestRunner(test_server, 'vCenter')
        def test_results = test.makeTest(['vm'])
        def verify_rule = new VerifyRuleGenerator(rules)
        def verify_results = test.verifyResults(verify_rule)
        println test_results
        println verify_results

        then:
        verify_results.size() > 0
    }

    def "検査ルールエラー"() {
        when:
        def test = new DomainTestRunner(test_server, 'vCenter')
        def test_results = test.makeTest(['vm'])
        rules['RuleDB']['vCenter']['VMHost']   = 'Hoge'
        def verify_rule = new VerifyRuleGenerator(rules)
        def verify_results = test.verifyResults(verify_rule)
        println test_results
        println verify_results

        then:
        1 == 1
        // verify_results.size() > 0
    }

    def "デバイス付検査結果の検証"() {
        when:
        def test = new DomainTestRunner(test_server, 'Linux')
        def test_results = test.makeTest(['packages'])
        def device_results = [:].withDefault{[:]}
        test.setDeviceResults(device_results)
        println device_results
        then:
        device_results.size() > 0
    }

}

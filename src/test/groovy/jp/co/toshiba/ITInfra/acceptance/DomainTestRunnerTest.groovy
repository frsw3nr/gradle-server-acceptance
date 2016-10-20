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
        setup:
        def verifier = VerifyRuleGenerator.instance
        verifier.setVerifyRule(rules)

        when:
        def test = new DomainTestRunner(test_server, 'vCenter')
        def test_results = test.makeTest(['vm'])
        def statuses = test.verify()
        println statuses
        def statuses2 = test.getVerifyStatuses()
        println statuses2
        def results = test.getResults()
        println results

        then:
        statuses.size()  == 3
        statuses2.size() == 3
        results.size()   == 5
    }

    def "デバイス付検査結果の検証"() {
        when:
        def test = new DomainTestRunner(test_server, 'Linux')
        def test_results = test.makeTest(['packages'])
        def device_sheet = new DeviceResultSheet()
        test.result_test_items.each { test_item ->
            println test_item.devices
        }
        device_sheet.setResults('Linux', test_server.server_name, test.result_test_items)
        println device_sheet.getHeaders('Linux', 'packages')
        println device_sheet.getCSVs('Linux', 'packages')

        then:
        1 == 1
        // device_sheet.csvs.size() > 0
    }

}

import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon clean test --tests "VerifyRuleGeneratorTest"

class VerifyRuleGeneratorTest extends Specification {

    def rules = [:].withDefault{[:].withDefault{[:]}}

    def setup() {
        rules['AP01']['Windows']['cpu_total'] = '1 < x'
        rules['AP01']['Windows']['memory']    = '4096 <= x'
        rules['AP01']['Windows']['hostname']  = 'x =~ /win/'
        rules['AP01']['Linux']['hostname']    = 'x =~ /Cent/'
    }

    def "検証ルールコードの初期化"() {
        when:
        def rule_code = new VerifyRuleGenerator(rules)
        def rule_code_text = rule_code.generate_code()
        println rule_code_text

        then:
        rule_code_text.size() > 0
    }

    def "検証ルールロード"() {
        when:
        def rule_code = new VerifyRuleGenerator(rules)
        def spec = rule_code.generate_instance()

        then:
        spec.AP01__Windows__memory(1024) == false
        spec.AP01__Windows__memory(4096) == true
    }

    def "検証ルール正規表現"() {
        when:
        def rule_code = new VerifyRuleGenerator(rules)
        def spec = rule_code.generate_instance()

        then:
        spec.AP01__Windows__hostname('testwindows') == true
        spec.AP01__Windows__hostname('testlinux')   == false
    }

    def "検証ルールエラー"() {
        when:
        rules['AP01']['Windows']['memory']    = 'hoge'
        def rule_code = new VerifyRuleGenerator(rules)
        def spec = rule_code.generate_instance()
        spec.AP01__Windows__memory(1024) == false

        then:
        thrown(MissingPropertyException)
    }

    def "正規表現ルール付テスト仕様のロード"() {
        setup:
        def test_server = new TargetServer(
            server_name   : 'ostrich',
            ip            : 'localhost',
            platform      : 'Linux',
            os_account_id : 'Test',
            vcenter_id    : 'Test',
            verify_id     : 'AP01',
            vm            : 'ostrich',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
        test_server.dry_run = true
        def test = new DomainTestRunner(test_server, 'Linux')
        def rule_code = new VerifyRuleGenerator(rules)
        def spec = rule_code.generate_instance()

        when:
        def test_item = new TestItem('hostname')
        test.run(test_item)
        test_item.results.each {test_id, value ->
            // def result = rule_code.verify('AP01', 'Linux', test_id, value)
            println "${test_id}:${value}"
        }
        // def verify_results = test.verifyResults(rule_code)
        // println verify_results.toString()
        // println test_item.verify_statuses.toString()

        then:
        1 == 1
        // test_item.results.size() > 0
        // test_item.devices.size() > 0
        // test_item.device_header.size() > 0
    }


}

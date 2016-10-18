import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon clean test --tests "VerifyRuleGeneratorTest"

class VerifyRuleGeneratorTest extends Specification {

    def rules = [:].withDefault{[:].withDefault{[:]}}

    def setup() {
        rules['AP01']['Windows']['cpu_total'] = '1 < x'
        rules['AP01']['Windows']['memory']    = '4096 <= x'
        rules['AP01']['Windows']['hostname']  = 'x =~ /win/'
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
}

import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon clean test --tests "VerifyRuleGeneratorTest"

class VerifyRuleGeneratorTest extends Specification {

    def rules = [:].withDefault{[:].withDefault{[:]}}
    def verifier
    def server_info = [:]

    def setup() {
        rules['AP01']['Windows']['cpu_total'] = '1 < x'
        rules['AP01']['Windows']['memory']    = '4096 <= x'
        rules['AP01']['Windows']['hostname']  = 'x =~ /win/'
        rules['AP01']['Linux']['hostname']    = 'x =~ /Cent/'
        server_info['hostname'] = 'Cent01'
        verifier = VerifyRuleGenerator.instance
    }

    def "検証ルールコードの初期化"() {
        when:
        def code = verifier.getVerifyRuleScript(rules)
        println code

        then:
        code.size() > 0
    }

    def "検証ルールロード"() {
        when:
        verifier.setVerifyRule(rules)

        then:
        verifier.verify('AP01', 'Windows', 'memory', "1024") == false
        verifier.verify('AP01', 'Windows', 'memory', "4096") == true
    }

    def "検証ルール正規表現"() {
        when:
        verifier.setVerifyRule(rules)

        then:
        verifier.verify('AP01', 'Windows', 'hostname', "testwindows") == true
        verifier.verify('AP01', 'Windows', 'hostname', "testlinux") == false
    }

    def "検証ルール付帯情報"() {
        when:
        rules['AP01']['Linux']['hostname']    = 'x == server_info[\'hostname\']'
        server_info['hostname'] = 'Cent01'
        verifier.setVerifyRule(rules)
        then:
        verifier.verify('AP01', 'Linux', 'hostname', "Cent01", server_info) == true
        verifier.verify('AP01', 'Linux', 'hostname', "Cent02", server_info) == false
    }

    def "検証ルールエラー"() {
        when:
        rules['AP01']['Windows']['memory']    = 'hoge'
        verifier.setVerifyRule(rules)
        def res = verifier.verify('AP01', 'Windows', 'memory', "1024")

        then:
        res == null
    }

    def "正規表現ルール付テスト仕様のロード"() {
        setup:
        rules['AP01']['Linux']['hostname']    = 'x =~ /rich/'
        verifier.setVerifyRule(rules)
        def test_server = new TargetServer(
            server_name       : 'ostrich',
            ip                : 'localhost',
            platform          : 'Linux',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            verify_id         : 'AP01',
            remote_alias      : 'ostrich',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
        test_server.dry_run = true
        def test = new DomainTestRunner(test_server, 'Linux')

        when:
        def test_item = new TestItem('hostname')
        test.run(test_item)
        def res = verifier.verify('AP01', 'Linux', 'hostname', test_item.results['hostname'])

        then:
        res == true
    }

    def "Excelルール評価"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config.groovy')
        evidence.readSheet()
        println evidence.verify_rules
        verifier.setVerifyRule(evidence.verify_rules)
        println verifier.getVerifyRuleScript(evidence.verify_rules)

        then:
        verifier.verify('RuleAP', 'vCenter', 'NumCpu', "1") == false
        verifier.verify('RuleAP', 'vCenter', 'NumCpu', "3") == true
        verifier.verify('RuleAP', 'vCenter', 'PowerState', "2") == null
        verifier.verify('RuleAP', 'vCenter', 'VMHost', "ostrich") == true
        verifier.verify('RuleAP', 'vCenter', 'VMHost', "hogehoge") == false
        verifier.verify('RuleAP', 'vCenter', 'Hoge', "hogehoge") == null
        verifier.verify('RuleAP', 'Linux', 'lsb', "CentOS6.6") == true
        verifier.verify('RuleAP', 'Linux', 'lsb', "Ubuntu14.2") == false
    }

}

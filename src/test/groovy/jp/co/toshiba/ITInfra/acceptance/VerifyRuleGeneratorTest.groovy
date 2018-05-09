import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "VerifyRuleGeneratorTest.検証ルールコードの初期化"

class VerifyRuleGeneratorTest extends Specification {

    String config_file = 'src/test/resources/config.groovy'
    TestPlatform test_platform

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
        def code = verifier.get_verify_rule_script(rules)

        then:
        code.size() > 0
    }

    def "検証ルールロード"() {
        when:
        verifier.set_verify_rule(rules)

        then:
        verifier.verify('AP01', 'Windows', 'memory', "1024") == false
        verifier.verify('AP01', 'Windows', 'memory', "4096") == true
    }

    def "検証ルール正規表現"() {
        when:
        verifier.set_verify_rule(rules)

        then:
        verifier.verify('AP01', 'Windows', 'hostname', "testwindows") == true
        verifier.verify('AP01', 'Windows', 'hostname', "testlinux") == false
    }

    def "検証ルール付帯情報"() {
        when:
        rules['AP01']['Linux']['hostname']    = 'x == server_info[\'hostname\']'
        server_info['hostname'] = 'Cent01'
        verifier.set_verify_rule(rules)

        then:
        verifier.verify('AP01', 'Linux', 'hostname', "Cent01", server_info) == true
        verifier.verify('AP01', 'Linux', 'hostname', "Cent02", server_info) == false
    }

    def "検証ルールエラー"() {
        when:
        rules['AP01']['Windows']['memory']    = 'hoge'
        verifier.set_verify_rule(rules)
        def res = verifier.verify('AP01', 'Windows', 'memory', "1024")

        then:
        res == null
    }

    def "テスト実行結果の検証"() {
        setup:
        def test_target = new TestTarget(
            name              : 'ostrich',
            ip                : '192.168.10.1',
            domain            : 'Linux',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            remote_alias      : 'ostrich',
            verify_id         : 'AP01',
        )

        test_platform = new TestPlatform(
            name         : 'Linux',
            test_target  : test_target,
            dry_run      : true,
        )

        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file : config_file)

        rules['AP01']['Linux']['hostname'] = 'x =~ /rich/'
        verifier.set_verify_rule(rules)

        when:
        platform_tester.init()
        platform_tester.set_test_items('hostname')
        platform_tester.run()

        def result_value = test_platform.test_results['hostname'].value
        def res = verifier.verify('AP01', 'Linux', 'hostname', result_value)

        then:
        res == true
    }

    def "Excelルール評価"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_rule_set = new TestRuleSet(name: 'root')
        test_rule_set.accept(excel_parser)

        when:
        verifier.set_verify_rule(test_rule_set)
        def code = verifier.get_verify_rule_script(test_rule_set)
        // println code

        then:
        code.size() > 0
        // verifier.verify('RuleAP', 'vCenter', 'NumCpu', "1") == false
        // verifier.verify('RuleAP', 'vCenter', 'NumCpu', "3") == true
        // verifier.verify('RuleAP', 'vCenter', 'PowerState', "2") == null
        // verifier.verify('RuleAP', 'vCenter', 'VMHost', "ostrich") == true
        // verifier.verify('RuleAP', 'vCenter', 'VMHost', "hogehoge") == false
        // verifier.verify('RuleAP', 'vCenter', 'Hoge', "hogehoge") == null
        // verifier.verify('RuleAP', 'Linux', 'lsb', "CentOS6.6") == true
        // verifier.verify('RuleAP', 'Linux', 'lsb', "Ubuntu14.2") == false
    }

    def "Linux テスト結果の検証"() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        def test_target = new TestTarget(
            name              : 'ostrich',
            ip                : '192.168.10.1',
            domain            : 'Linux',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            remote_alias      : 'ostrich',
            NumCpu            : '1',
            verify_id         : 'RuleAP',
        )
        def test_metrics = test_scenario.test_metrics.get('Linux').get('Linux').get_all()
        def test_rule = test_scenario.test_rules.get('RuleAP')

        test_platform = new TestPlatform(
            name         : 'Linux',
            test_target  : test_target,
            test_metrics : test_metrics,
            test_rule    : test_rule,
            dry_run      : true,
        )

        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file : config_file)

        verifier.set_verify_rule(test_scenario.test_rules)

        when:
        platform_tester.init()
        platform_tester.run()

        then:
        test_rule.config['Linux'].each { metric, rule_test ->
            def value = test_platform.test_results[metric].value
            def target_info = test_target.asMap()
            println "SERVER_INFO: ${target_info}"
            def res = verifier.verify('RuleAP', 'Linux', metric, value, target_info)
            println "VERIFY: ${metric}, ${value}, ${res}"
            res == 1
        }
    }

}

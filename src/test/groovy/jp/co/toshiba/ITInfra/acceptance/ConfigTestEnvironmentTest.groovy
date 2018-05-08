import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "ConfigTestEnvironmentTest.Linuxアカウントセット"

class ConfigTestEnvironmentTest extends Specification {

    String config_file
    TestTarget test_target
    TestPlatform test_platform
    TestRule test_rule

    def setup() {
        config_file = 'src/test/resources/config.groovy'

        test_target = new TestTarget(
            name              : 'ostrich',
            ip                : '192.168.10.1',
            platform          : 'Linux',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            remote_alias      : 'ostrich',
            verify_id         : 'AP',
        )

        test_rule = new TestRule(name : 'AP',
                                 compare_rule : 'Actual',
                                 compare_source : 'centos7')

        test_platform = new TestPlatform(
            name        : 'Linux',
            test_target : test_target,
            test_rule   : test_rule,
        )
    }

    def "Linuxアカウントセット"() {
        when:
        def config = new ConfigTestEnvironment(config_file)
        config.set_account(test_platform)

        then:
        test_platform.os_account.user == 'someuser'
        test_platform.os_account.password == 'P@ssword'
    }

    def "Linux特定パスワード設定"() {
        setup:
        test_target.os_specific_password = 'P@ssword2'

        when:
        def config = new ConfigTestEnvironment(config_file)
        config.set_account(test_platform)

        then:
        test_platform.os_account.password == 'P@ssword2'
    }

    def "Linux環境セット"() {
        when:
        def config = new ConfigTestEnvironment(config_file)
        config.set_test_environment(test_platform)
        println "ENV:${test_platform.evidence_log_dir}"

        then:
        test_platform.evidence_log_dir == './build/log/Linux/ostrich'
        test_platform.dry_run == false
    }

    def "Windowsアカウント設定"() {
        setup:
        test_platform.name = 'Windows'

        when:
        def config = new ConfigTestEnvironment(config_file)
        config.set_account(test_platform)
        config.set_test_environment(test_platform)

        then:
        test_platform.os_account.user == 'administrator'
        test_platform.evidence_log_dir == './build/log/Windows/ostrich'
    }
}
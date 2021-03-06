import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
// import jp.co.toshiba.ITInfra.acceptance.TestLog
import jp.co.toshiba.ITInfra.acceptance.LogFile
import jp.co.toshiba.ITInfra.acceptance.NodeFile
import jp.co.toshiba.ITInfra.acceptance.LogStage
import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
// import jp.co.toshiba.ITInfra.acceptance.Model.TestRule
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import spock.lang.Specification

// gradle --daemon test --tests "ConfigTestEnvironmentTest.Linux特定パスワード設定"

class ConfigTestEnvironmentTest extends Specification {

    String config_file
    TestTarget test_target
    TestPlatform test_platform
    // TestRule test_rule
    ConfigTestEnvironment test_env

    def setup() {
        config_file = 'src/test/resources/config.groovy'
        test_env = ConfigTestEnvironment.instance

        test_target = new TestTarget(
            name           : 'ostrich',
            ip             : '192.168.10.1',
            platform       : 'Linux',
            account_id     : 'Test',
            template_id    : 'AP',
            compare_server : 'cent7',
            remote_alias   : 'ostrich',
            verify_id      : 'AP',
        )

        // test_rule = new TestRule(name : 'AP',
        //                          compare_rule : 'Actual',
        //                          compare_source : 'centos7')

        test_platform = new TestPlatform(
            name        : 'Linux',
            test_target : test_target,
            // test_rule   : test_rule,
        )
    }

    def "ログディレクトリセット"() {
        when:
        test_env.read_config(config_file)
        test_env.set_account(test_platform)

        then:
        LogFile.defined(LogStage.PROJECT) == true
        NodeFile.defined(LogStage.CURRENT) == true
    }

    def "Linuxアカウントセット"() {
        when:
        test_env.read_config(config_file)
        test_env.set_account(test_platform)

        then:
        test_platform.os_account.user == 'someuser'
        test_platform.os_account.password == 'P@ssword'
    }

    def "Linux特定パスワード設定"() {
        setup:
        test_target.specific_password = 'P@ssword2'

        when:
        test_env.read_config(config_file)
        test_env.set_account(test_platform)

        then:
        // ConfigTestEnvironment.groovy 内
        // コメントアウトしている。特定パスワードは各シナリオで使用する仕様に変更
        // test_env では使用しないため、既定の'P@ssword'が正しい
        test_platform.os_account.password == 'P@ssword'
    }

    def "Linux環境セット"() {
        when:
        test_env.read_config(config_file)
        // test_env.accept(test_platform)
        test_env.accept(test_platform)
        test_env.accept(LogFile)
        println "ENV:${test_platform.current_test_log_dir}"
        test_env.print_config()

        then:
        test_platform.current_test_log_dir == './build/log/ostrich'
        // test_platform.dry_run == false
    }

    def "Linux環境カスタム"() {
        when:
        test_env.read_config(config_file)
        test_env.config.dry_run = true
        test_env.accept(test_platform)
        println "ENV1:${test_platform.current_test_log_dir}"
        println "ENV2:${test_platform.dry_run}"

        then:
        test_platform.current_test_log_dir == './build/log/ostrich'
        // test_platform.dry_run == true
    }

    def "Windowsアカウント設定"() {
        setup:
        test_platform.name = 'Windows'

        when:
        test_env.read_config(config_file)
        test_env.set_account(test_platform)
        test_env.accept(test_platform)

        then:
        test_platform.os_account.user == 'administrator'
        test_platform.current_test_log_dir == './build/log/ostrich'
    }

}

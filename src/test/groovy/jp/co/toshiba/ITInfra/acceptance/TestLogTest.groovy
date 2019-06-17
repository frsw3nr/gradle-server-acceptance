import jp.co.toshiba.ITInfra.acceptance.TestLog
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import spock.lang.Specification

// gradle --daemon test --tests "TestLogTest.設定読み込み"

class TestLogTest extends Specification {

    def test_env

    def setup() {
        test_env = ConfigTestEnvironment.instance
        test_env.read_config('src/test/resources/config.groovy')
    }

    def 設定読み込み() {
        when:
        def test_log = new TestLog('ostrich', 'Linux')
        test_env.accept(test_log)

        then:
        test_log.base_test_log_dir == './src/test/resources/log'
    }

    def 検査ログ検索() {
        when:
        def test_log = new TestLog('ostrich', 'Linux')
        test_env.accept(test_log)
        def log_path = test_log.get_source_log_path('uname')

        then:
        log_path == './src/test/resources/log/ostrich/Linux/uname'
    }

    def ローカルディレクトリ() {
        when:
        def test_log = new TestLog('ostrich', 'Zabbix')
        test_env.accept(test_log)
        def local_dir = test_log.get_local_dir()

        then:
        local_dir == './build/log/ostrich/Zabbix'
    }

    def ログ保存先() {
        when:
        def test_log = new TestLog('ostrich', 'Linux')
        test_env.accept(test_log)
        def log_path1 = test_log.get_target_log_path('uname')
        def log_path2 = test_log.get_target_log_path('groups', true)
        def local_dir = test_log.get_local_dir()
        new File(local_dir).mkdirs()
        new File(log_path2).write('TEST')
        def log_path3 = test_log.get_source_log_path('groups', true)

        then:
        log_path1 == './build/log/ostrich/Linux/uname'
        log_path2 == './build/log/groups'
        log_path3 == './build/log/groups'
    }
}

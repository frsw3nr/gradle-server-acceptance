import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import jp.co.toshiba.ITInfra.acceptance.TestLog
import jp.co.toshiba.ITInfra.acceptance.LogStage
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import spock.lang.Specification

// gradle --daemon test --tests "TestLogTest.設定読み込み"

class TestLogTest extends Specification {

    def test_env

    def setup() {
        test_env = ConfigTestEnvironment.instance
        test_env.read_config('src/test/resources/config.groovy')
        test_env.accept(TestLog)
    }

    def 設定読み込み() {
        when:
        test_env.accept(TestLog)

        then:
        TestLog.logDirs[LogStage.BASE] == './src/test/resources/log'
    }

    def テストログセッター() {
        when:
        TestLog.setLogDirs(
            (LogStage.BASE)    : '/tmp/base',
            (LogStage.PROJECT) : '/tmp/project',
            (LogStage.CURRENT) : '/tmp/current'
        )

        then:
        TestLog.logDirs[LogStage.PROJECT] == '/tmp/project'
    }

    def ベースとプロジェクトディレクトリの一致チェック() {
        when:
        test_env.accept(TestLog)

        then:
        TestLog.directoryMatch(LogStage.BASE, LogStage.PROJECT) == true
        TestLog.directoryMatch(LogStage.BASE, LogStage.CURRENT) == false
    }

    def 検査ログ検索() {
        setup:
        test_env.accept(TestLog)

        expect:
        TestLog.getLogPath(target, platform, metric) == expectedValue

        where:
        expectedValue                                  || target    | platform | metric 
        './src/test/resources/log/ostrich/Linux'       || 'ostrich' | 'Linux'  | null   
        './src/test/resources/log/ostrich/Linux/uname' || 'ostrich' | 'Linux'  | 'uname'
        './src/test/resources/log/ostrich/Zabbix/Host' || 'ostrich' | 'Zabbix' | 'Host' 
        null                                           || 'Hoge'    | 'Zabbix' | 'Host' 
    }

    def ターゲットを指定しない検査ログ検索() {
        when:
        String logPath = TestLog.getLogPath(null, 'Linux')

        then:
        thrown(AssertionError)
        logPath == null
    }


    def 古いV1の検査ログ検索() {
        setup:
        TestLog.setLogDirs(
            (LogStage.PROJECT) : './src/test/resources/log2',
        )

        expect:
        TestLog.getLogPathV1(target, platform, metric) == expectedValue

        where:
        expectedValue                                         || target    | platform | metric
        './src/test/resources/log2/Linux/ostrich/Linux/uname' || 'ostrich' | 'Linux'  | 'uname'
        './src/test/resources/log2/Linux/ostrich/Linux'       || 'ostrich' | 'Linux'  | null
        null                                                  || 'ostrich' | 'Linux'  | 'Hoge'
    }

    def カレント検査ログ検索() {
        setup:
        test_env.accept(TestLog)

        expect:
        TestLog.getTargetPath(target, platform, metric, shared) == expectedValue

        where:
        expectedValue                     || target    | platform | metric  | shared
        './build/log/ostrich/Linux'       || 'ostrich' | 'Linux'  | null    | null
        './build/log/ostrich/Linux/uname' || 'ostrich' | 'Linux'  | 'uname' | null
        './build/log/Host'                || 'ostrich' | 'Zabbix' | 'Host'  | true
    }

    def プロジェクトからカレントへログコピー() {
        setup:
        test_env.accept(TestLog)

        when:
        TestLog.copyLogs('ostrich', 'Linux', LogStage.PROJECT, LogStage.CURRENT)

        then:
        new File('./build/log/ostrich/Linux').exists()
    }

    def プロジェクトからカレントへノード定義コピー() {
        setup:
        test_env.accept(TestLog)

        when:
        TestLog.copyNodes('ostrich', 'Linux', LogStage.PROJECT, LogStage.CURRENT)

        then:
        new File('./build/json/ostrich/Linux.json').exists()
        new File('./build/json/ostrich__Linux.json').exists()
    }
}

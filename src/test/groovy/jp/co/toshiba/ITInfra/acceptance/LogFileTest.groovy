import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import jp.co.toshiba.ITInfra.acceptance.LogFile
import jp.co.toshiba.ITInfra.acceptance.NodeFile
import jp.co.toshiba.ITInfra.acceptance.LogStage
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import spock.lang.Specification

// gradle --daemon test --tests "LogFileTest.設定読み込み"

class LogFileTest extends Specification {

    def test_env

    def setup() {
        test_env = ConfigTestEnvironment.instance
        test_env.read_config('src/test/resources/config.groovy')
        test_env.accept(LogFile)
        new File('/tmp/current').deleteDir()
    }

    def 設定読み込み() {
        when:
        test_env.accept(LogFile)

        then:
        LogFile.getLogDir(LogStage.BASE) == './src/test/resources/log'
    }

    def テストログセッター() {
        when:
        LogFile.setLogDirs(
            (LogStage.BASE)    : '/tmp/base',
            (LogStage.PROJECT) : '/tmp/project',
            (LogStage.CURRENT) : '/tmp/current'
        )

        then:
        LogFile.getLogDir(LogStage.PROJECT) == '/tmp/project'
        LogFile.defined(LogStage.PROJECT) == true
    }

    def ノードログセッター() {
        when:
        LogFile.setLogDirs((LogStage.PROJECT) : '/tmp/project')
        NodeFile.setLogDirs((LogStage.PROJECT) : '/tmp/project2')

        then:
        LogFile.getLogDir(LogStage.PROJECT) == '/tmp/project'
        NodeFile.getLogDir(LogStage.PROJECT) == '/tmp/project2'
    }

    def ベースとプロジェクトディレクトリの一致チェック() {
        when:
        test_env.accept(LogFile)

        then:
        LogFile.matchDir(LogStage.BASE, LogStage.PROJECT) == true
        LogFile.matchDir(LogStage.BASE, LogStage.CURRENT) == false
    }

    def テストログゲッター() {
        when:
        test_env.accept(LogFile)
        LogFile.setLogDirs(
            (LogStage.CURRENT) : '/tmp/current'
        )

        then:
        LogFile.getTargetDir('ostrich', LogStage.BASE)       == './src/test/resources/log/ostrich'
        LogFile.getTargetDir('ostrich', LogStage.CURRENT)    == '/tmp/current/ostrich'
        LogFile.searchTargetDir('ostrich', LogStage.BASE)    == './src/test/resources/log/ostrich'
        LogFile.searchTargetDir('ostrich', LogStage.CURRENT) == null
    }

    def 検査ログ検索() {
        expect:
        LogFile.searchPath(target, platform, metric) == expectedValue

        where:
        expectedValue                                  || target    | platform  | metric 
        './src/test/resources/log/ostrich/Linux/uname' || 'ostrich' | 'Linux'   | 'uname'
        './src/test/resources/log/ostrich/Linux/uname' || 'ostrich' | 'Linux'   | 'uname'
        './src/test/resources/log/ostrich/vCenter/vm'  || 'ostrich' | 'vCenter' | 'vm'   
    }

    def ステージ指定検査ログ検索() {
        setup:
        LogFile.setLogDirs(
            (LogStage.CURRENT) : '/tmp/current'
        )
        when:
        String path1 = LogFile.searchPath('ostrich', 'Linux', 'uname', LogStage.PROJECT)
        String path2 = LogFile.searchPath('ostrich', 'Linux', 'uname', LogStage.CURRENT)

        then:
        path1 == './src/test/resources/log/ostrich/Linux/uname'
        path2 == null
    }

    def ターゲットを指定しない検査ログ検索() {
        when:
        String logPath = LogFile.getPlatformDir(null, 'Linux')

        then:
        thrown(AssertionError)
        logPath == null
    }


    def 古いV1の検査ログ検索() {
        setup:
        LogFile.setLogDirs(
            (LogStage.PROJECT) : './src/test/resources/log2',
        )

        expect:
        LogFile.searchPathV1(target, platform, metric) == expectedValue

        where:
        expectedValue                                         || target    | platform | metric
        './src/test/resources/log2/Linux/ostrich/Linux/uname' || 'ostrich' | 'Linux'  | 'uname'
        null                                                  || 'ostrich' | 'Linux'  | 'Hoge'
    }

    def プロジェクトからカレントへログコピー() {
        setup:
        LogFile.setLogDirs(
            (LogStage.CURRENT) : '/tmp/current'
        )

        when:
        LogFile.copyTarget('ostrich', LogStage.PROJECT, LogStage.CURRENT)

        then:
        new File('/tmp/current/ostrich/Linux/uname').exists()
        new File('/tmp/current/ostrich/vCenter/vm').exists()
    }

    def プロジェクトからカレントへプラットフォーム定義コピー() {
        setup:
        LogFile.setLogDirs(
            (LogStage.CURRENT) : '/tmp/current'
        )

        when:
        LogFile.copyPlatform('ostrich', 'Linux', LogStage.PROJECT, LogStage.CURRENT)

        then:
        1 == 1
        new File('/tmp/current/ostrich/Linux/uname').exists()
        new File('/tmp/current/ostrich/vCenter/vm').exists() == false
    }
}

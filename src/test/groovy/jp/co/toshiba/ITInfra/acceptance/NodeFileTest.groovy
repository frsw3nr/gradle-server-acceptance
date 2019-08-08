import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import jp.co.toshiba.ITInfra.acceptance.NodeFile
import jp.co.toshiba.ITInfra.acceptance.LogStage
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import spock.lang.Specification

// gradle --daemon test --tests "NodeFileTest.設定読み込み"

class NodeFileTest extends Specification {

    def test_env


    def setup() {
        test_env = ConfigTestEnvironment.instance
        test_env.read_config('src/test/resources/config.groovy')
        test_env.accept(NodeFile)
        new File('/tmp/current').deleteDir()
    }

    def 設定読み込み() {
        when:
        test_env.accept(NodeFile)

        then:
        NodeFile.getLogDir(LogStage.BASE) == './node'
    }

    def テストログセッター() {
        when:
        NodeFile.setLogDirs(
            (LogStage.BASE)    : '/tmp/base',
            (LogStage.PROJECT) : '/tmp/project',
            (LogStage.CURRENT) : '/tmp/current'
        )

        then:
        NodeFile.getLogDir(LogStage.PROJECT) == '/tmp/project'
        NodeFile.defined(LogStage.PROJECT) == true
    }

    def ベースとプロジェクトディレクトリの一致チェック() {
        when:
        test_env.accept(NodeFile)

        then:
        NodeFile.matchDir(LogStage.BASE, LogStage.PROJECT) == true
        NodeFile.matchDir(LogStage.BASE, LogStage.CURRENT) == false
    }

    def テストログゲッター() {
        when:
        test_env.accept(NodeFile)
        NodeFile.setLogDirs(
            (LogStage.CURRENT) : '/tmp/current'
        )

        then:
        NodeFile.getTargetDir('ostrich', LogStage.BASE)       == './node/ostrich'
        NodeFile.getTargetDir('ostrich', LogStage.CURRENT)    == '/tmp/current/ostrich'
        NodeFile.searchTargetDir('ostrich', LogStage.BASE)    == './node/ostrich'
        NodeFile.searchTargetDir('ostrich', LogStage.CURRENT) == null
    }

    def 検査ログ検索() {
        setup:
        NodeFile.setLogDirs(
            (LogStage.CURRENT) : '/tmp/current'
        )

        expect:
        NodeFile.searchPath(target, platform, stage) == expectedValue

        where:
        expectedValue                 || target    | platform  | stage 
        './node/ostrich/Linux.json'   || 'ostrich' | 'Linux'   | LogStage.PROJECT   
        './node/ostrich/Linux.json'   || 'ostrich' | 'Linux'   | LogStage.BASE
        './node/ostrich/vCenter.json' || 'ostrich' | 'vCenter' | LogStage.BASE
        null                          || 'ostrich' | 'Linux'   | LogStage.CURRENT
    }

    def プロジェクトからカレントへログコピー() {
        setup:
        NodeFile.setLogDirs(
            (LogStage.CURRENT) : '/tmp/current'
        )

        when:
        NodeFile.copyPlatform('ostrich', 'Linux', LogStage.PROJECT, LogStage.CURRENT)

        then:
        new File('/tmp/current/ostrich/Linux.json').exists()
    }

    def プロジェクトからカレントへノード定義コピー() {
        setup:
        NodeFile.setLogDirs(
            (LogStage.CURRENT) : '/tmp/current'
        )

        when:
        NodeFile.copyTarget('ostrich', LogStage.PROJECT, LogStage.CURRENT)

        then:
        new File('/tmp/current/ostrich__Linux.json').exists()
        new File('/tmp/current/ostrich/vCenter.json').exists()
        new File('/tmp/current/ostrich/Linux.json').exists()
    }
}

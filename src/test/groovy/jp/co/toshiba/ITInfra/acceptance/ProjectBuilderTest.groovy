import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon test --tests "ProjectBuilderTest"

class ProjectBuilderTest extends Specification {

    def ダミーテスト() {
        when:
        println 'Test'

        then:
        1 == 1
    }

    // def "プロジェクトの初期化"() {
    //     setup:
    //     def getconfig_home = System.getProperty("user.dir")
    //     def target_log_dir = new File(getconfig_home + '/build/project-a')
    //     target_log_dir.deleteDir()

    //     when:
    //     def builder = new ProjectBuilder(getconfig_home, './build/project-a')
    //     builder.generate()

    //     then:
    //     builder != null
    // }

    // def "既にあるプロジェクトの初期化"() {
    //     setup:
    //     def getconfig_home = System.getProperty("user.dir")

    //     when:
    //     def builder = new ProjectBuilder(getconfig_home, './build/')
    //     builder.generate()

    //     then:
    //     thrown(IllegalArgumentException)
    // }
}

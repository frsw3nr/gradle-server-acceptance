import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon test --tests "ProjectBuilderTest"

class ProjectBuilderTest extends Specification {

    def getBlankSheetAll(File rootDir, java.util.regex.Pattern filter) {
        def results = []
        rootDir.traverse(
            type         : groovy.io.FileType.FILES,
            nameFilter   : filter
        ) { it -> results << it
            // groovy.io.FileVisitResult.TERMINATE
        }
        results
    }

    def ダミーテスト() {
        when:
        println 'Test'

        println getBlankSheetAll(new File('.'), ~/blank_.*\.xlsx/)

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

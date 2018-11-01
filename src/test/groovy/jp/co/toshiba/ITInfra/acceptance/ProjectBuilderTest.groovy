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

    def "ブランクシートのビルド"() {
        when:
        println 'Test'

        // def project_builder = new ProjectBuilder(".", "/tmp")
        def project_builder = new ProjectBuilder("/home/psadmin/work/gradle-server-acceptance", "/tmp")
        def blank_spec_sheets = project_builder.get_all_blank_sheet(project_builder.home)
        // getBlankSheetAll(new File('.'), ~/blank_.*\.xlsx/)
        // def blank_spec_sheets = getBlankSheetAll(new File('.'), ~/blank_.*\.xlsx/)
        blank_spec_sheets.each { blank_spec_sheet ->
            def target_dir  = blank_spec_sheet.getParent()
            target_dir = target_dir.replaceFirst(project_builder.home, project_builder.target)
            def target_file = blank_spec_sheet.name.replaceFirst(/blank_/, "")
            def target = new File(target_dir, target_file)
            // FileUtils.copyFile(source_log, target_log)

            println("Rebuild blank : ${blank_spec_sheet}")
            println("Rebuild blank target_dir  : ${target_dir}")
            println("Rebuild blank target_file : ${target_file}")
            println("Rebuild blank target : ${target}")
        }

        then:
        1 == 1
    }

    def "ブランクシートのコピー"() {
        when:
        println 'Test'

        // def project_builder = new ProjectBuilder(".", "/tmp")
        def project_builder = new ProjectBuilder("/home/psadmin/work/gradle-server-acceptance", "/tmp")
        project_builder.copy_all_blank_template_sheet()

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

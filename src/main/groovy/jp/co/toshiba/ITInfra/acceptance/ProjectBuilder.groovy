package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import groovy.io.FileType

@Slf4j
class ProjectBuilder {

    String home
    String target

    ProjectBuilder(String home, String target = null) {
        this.home = home
        this.target = target
    }

    def generate(String mode = null) {
        assert(home)
        assert(target)

        def target_dir = new File(target).getAbsolutePath()
        if(new File(target_dir).exists()){
            throw new IllegalArgumentException("'${target_dir}' exists.")
        }
        // Create an empty directory
        ['build', 'src/test/resource/log'].each { base ->
            def target_path =  new File("${target_dir}/${base}")
            target_path.mkdirs()
            new File("${target_path}/.gitkeep").createNewFile()
        }
        // Copy all files under the directory
        ['config', 'lib', 'image'].each { base ->
            FileUtils.copyDirectory(new File("${home}/${base}"),
                                    new File("${target_dir}/${base}"))
        }
        // Copy Excel file under home
        new File(home).eachFileMatch(FileType.FILES, ~/.+.xlsx/) {
            FileUtils.copyFile(it, new File("${target_dir}/${it.name}"))
        }
        // Copy by specifying file name
        ['.gitignore', 'Changes.txt', 'Readme.md', 'LICENSE.txt'].each { base ->
            FileUtils.copyFile(new File("${home}/${base}"),
                               new File("${target_dir}/${base}"))
        }
    }

    def xport(String xport_file) {
        assert(home)
        assert(xport_file)

        // Drop and create working directory
        def target_path = new File("${home}/build/xport_tmp").getAbsolutePath()
        def target_dir  = new File(target_path)
        if(target_dir.exists()){
            target_dir.deleteDir()
        }
        target_dir.mkdirs()

        // Create '{target}/docs/{project_name}' directory and copy Changes.txt,Readme.md.
        def project_name  = new File(home).getName()
        def document_path = "${target_path}/docs/${project_name}"
        def document_dir  = new File(document_path)
        document_dir.mkdirs()
        ['Changes.txt', 'Readme.md'].each { file ->
            def source = new File("${home}/${file}")
            if (source.exists()) {
                FileUtils.copyFile(source, new File("${document_path}/${file}"))
            }
        }

        // Copy '{home}/config/config*.groovy'
        def config_path = "${target_path}/config"
        def config_dir  = new File(config_path)
        config_dir.mkdirs()
        new File("${home}/config").eachFileMatch(~/config.*.groovy/) { file ->
            def file_name = file.getName()
            FileUtils.copyFile(file, new File("${config_path}/${file_name}"))
        }

        // Copy all files under the directory; 'lib/InfraTestSpec/*', 'script/*'
        new File("${target_path}/lib").mkdirs()
        ['lib/InfraTestSpec', 'script'].each { base ->
            def source = new File("${home}/${base}")
            if (source.exists()) {
                FileUtils.copyDirectory(source, new File("${target_path}/${base}"))
            }
        }
        // Copy '{home}/*.xlsx'
        new File(home).eachFileMatch(~/.*.xlsx/) { file ->
            def file_name = file.getName()
            FileUtils.copyFile(file, new File("${target_path}/${file_name}"))
        }

        // Archive working directory.
        new AntBuilder().zip(destfile: xport_file, basedir: target_path)
    }
}

package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

@Slf4j
class ProjectBuilder {

    String home
    String target

    ProjectBuilder(String home, String target) {
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
        ['config', 'lib'].each { base ->
            FileUtils.copyDirectory(new File("${home}/${base}"),
                                    new File("${target_dir}/${base}"))
        }
        // Copy by specifying file name
        ['サーバーチェックシート.xlsx', '.gitignore', 'Changes.txt', 'Readme.md', 'LICENSE.txt'].each { base ->
            FileUtils.copyFile(new File("${home}/${base}"),
                               new File("${target_dir}/${base}"))
        }
    }

}

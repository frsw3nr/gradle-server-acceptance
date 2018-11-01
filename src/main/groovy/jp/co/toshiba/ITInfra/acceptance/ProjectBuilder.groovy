package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import groovy.io.FileType

@Slf4j
class ProjectBuilder {

    String home
    String target

    ProjectBuilder(String home, String target = null) {
        this.home = home.replaceAll(/[\s\/\\]+\z/,"")
        this.target = target.replaceAll(/[\s\/\\]+\z/,"")
    }

    def generate(String mode = null) {
        assert(home)
        assert(target)

        def target_dir = new File(target).getAbsolutePath()
        if(new File(target_dir).exists()){
            // プロジェクト機能検討
            // ====================

            // Changes.txt の 1行目を読んで、バージョン確認
            // バージョンが古い場合はアップデートする

            // アップデート対象
            //     サーバチェックシート.xslx
            //     libの下
            // 更新対象のファイルは、ファイル名.{日付}にリネームする

            // getconfig -g {ディレクトリ}オプションと同じにする

            throw new IllegalArgumentException("'${target_dir}' exists.")
        }
        // Create an empty directory
        def create_directories = ['config', 'build', 'node']
        if (mode == 'detail') {
            create_directories << 'src/test/resources/log'
        }
        // ['config', 'build', 'node', 'src/test/resources/log'].each { base ->
        create_directories.each { base ->
            def target_path =  new File("${target_dir}/${base}")
            target_path.mkdirs()
            new File("${target_path}/.gitkeep").createNewFile()
        }
        // Copy Config file under home
        ['config.groovy'].each {base ->
            FileUtils.copyFile(new File("${home}/config/${base}"),
                               new File("${target_dir}/config/${base}"))
        }
        // Copy all files under the directory
        def copy_directories = ['lib', 'image', 'template']
        if (mode == 'detail') {
            copy_directories << 'src/test/resources/log'
        }
        // ['config', 'build', 'node', 'src/test/resources/log'].each { base ->
        copy_directories.each { base ->
        // ['lib', 'image', 'template', 'src/test/resources/log'].each { base ->
            FileUtils.copyDirectory(new File("${home}/${base}"),
                                    new File("${target_dir}/${base}"))
        }
        // Copy Excel file under home
        if (mode == 'detail') {
            new File(home).eachFileMatch(FileType.FILES, ~/.+.xlsx/) {
                FileUtils.copyFile(it, new File("${target_dir}/${it.name}"))
            }
        } else {
            // Search "blank_*.xlsx" and copy it with the file name excluding "blank_".
            this.copy_all_blank_template_sheet()
            new File(home).eachFileMatch(FileType.FILES, ~/blank_.+.xlsx/) {
                def target_name = it.name.replaceFirst(/blank_/, "")
                FileUtils.copyFile(it, new File("${target_dir}/${target_name}"))
            }
        } 
        // Copy by specifying file name
        ['.gitignore', 'Changes.txt', 'Readme.md', 'LICENSE.txt'].each { base ->
            FileUtils.copyFile(new File("${home}/${base}"),
                               new File("${target_dir}/${base}"))
        }
    }

    def get_all_blank_sheet(String rootDir) {
        def results = []
        new File(rootDir).traverse(
            type         : groovy.io.FileType.FILES,
            nameFilter   : ~/blank_.*\.xlsx/
        ) { 
            it -> results << it
        }
        return results
    }

    def copy_all_blank_template_sheet() throws IOException {
        def template_dir = new File(this.home, 'template')
        log.debug("template_dir: ${template_dir}")
        template_dir.traverse(
            type        : groovy.io.FileType.FILES,
            nameFilter  : ~/blank_.*\.xlsx/) { source ->
            // target_path = target_path.replaceFirst(this.home, this.target)
            def target_dir_path = source.getParent().replace(this.home, this.target)
            log.debug("target_dir: ${target_dir_path}")
            def target_dir  = new File(target_dir_path)
            if (!target_dir.exists()){
                target_dir.mkdirs()
            } else {
                def unused_sheet = new File(target_dir_path, source.name)
                log.debug("Remove: ${unused_sheet}")
                if (unused_sheet.exists()) {
                    unused_sheet.delete()
                }
            }
            def target_file = source.name.replaceFirst(/blank_/, "")
            def target = new File(target_dir, target_file)
            log.debug("Copy ${source} ${target}")
            FileUtils.copyFile(source, target)
        }
    }

    def xport(String xport_file) {
        assert(home)
        assert(xport_file)
        long start = System.currentTimeMillis()

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
        def project_suffix = project_name
        (project_name =~ /^(.+)-(.+?)$/).each { m0, m1, m2 ->
            project_suffix = m2
        }
        println "Export project : $project_name, suffix : $project_suffix"
        document_dir.mkdirs()
        ['Changes.txt', 'Readme.md'].each { file ->
            def source = new File("${home}/${file}")
            if (source.exists()) {
                FileUtils.copyFile(source, new File("${document_path}/${file}"))
            }
        }

        // Copy all files under the directory; 'config/', lib/*/'
        ['config', 'lib/InfraTestSpec', 'lib/script', 'lib/template'].each { base ->
            def source = new File("${home}/${base}")
            if (source.exists()) {
                def target = "${target_path}/${base}"
                new File(target).mkdirs()
                source.eachFile { file ->
                    def file_name = file.getName()
                    (file_name =~ /(?i)${project_suffix}/).each {m0 ->
                        println "Copy ${base}/${file_name}"
                        FileUtils.copyFile(file, new File("${target}/${file_name}"))
                    }
                }
            }
        }
        // Copy all files under the directory
        ['src', 'node'].each { base ->
            FileUtils.copyDirectory(new File("${home}/${base}"),
                                    new File("${target_dir}/${base}"))
        }
        // Copy '{home}/*.xlsx'
        new File(home).eachFileMatch(~/.*.xlsx/) { file ->
            def file_name = file.getName()
            println "Copy ${file_name}"
            FileUtils.copyFile(file, new File("${target_path}/${file_name}"))
        }

        // Archive working directory.
        println "Archive ${xport_file}"
        new AntBuilder().zip(destfile: xport_file, basedir: target_path)
        long elapse = System.currentTimeMillis() - start
        println "Finish archive, Elapse : ${elapse} ms"
    }
}

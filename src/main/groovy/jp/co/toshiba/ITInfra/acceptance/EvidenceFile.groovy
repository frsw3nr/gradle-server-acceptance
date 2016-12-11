package jp.co.toshiba.ITInfra.acceptance

import groovy.io.FileType
import static groovy.json.JsonOutput.*
import groovy.json.*
import groovy.util.logging.Slf4j

@Slf4j
class EvidenceFile {

    String home
    String last_run_config

    EvidenceFile(String home) {
        this.home = home
    }

    def generate() throws IOException {
        def last_run_json = new File("$home/build/.last_run").text
        def last_run = new JsonSlurper().parseText(last_run_json)

        println last_run
        new File(last_run.node_dir).eachFileRecurse(FileType.FILES) {
            println it
        }
        // new File('./build/log').eachFile { println it.name }

// build/log/_node の下を順に操作
// ファイルがあれば、ディレクトリとファイル名を抽出
// nodeの下にディレクトリ作成。あれば何もしない
// 作成したディレクトリにコピー

    }
}

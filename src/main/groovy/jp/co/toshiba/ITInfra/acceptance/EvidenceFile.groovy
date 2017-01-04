package jp.co.toshiba.ITInfra.acceptance

import groovy.io.FileType
import org.apache.commons.io.FileUtils
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
        def node_path = new File("./node").getAbsolutePath()
        FileUtils.copyDirectory(new File(last_run.node_dir), new File(node_path))
    }
}

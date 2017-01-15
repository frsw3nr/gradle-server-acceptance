package jp.co.toshiba.ITInfra.acceptance

import groovy.io.FileType
import groovy.util.logging.Slf4j
import static groovy.json.JsonOutput.*
import groovy.json.*
import org.apache.commons.io.FileUtils
import groovy.sql.Sql
import java.sql.*

@Slf4j
class EvidenceFile {

    final static def current_build = 1
    String home
    String project_name
    String tenant_name
    String last_run_config
    String db_config
    def cmdb

    EvidenceFile(Map params) {
        assert params.home
        this.home = params.home
        this.project_name = new File(this.home).getName()
        this.tenant_name = '_Default'
        this.last_run_config = params.last_run_config ?: "${params.home}/build/.last_run"
        this.db_config  = params.db_config ?: 'config/config_db.groovy'
    }

    def getNodeDirSource() throws IOException {
        def last_run_json = new File(last_run_config).text
        def last_run = new JsonSlurper().parseText(last_run_json)
        def node_dir_source = last_run?.node_dir
        if (!node_dir_source) {
            def msg = "Config not found node_dir : ${last_run_config}"
            throw new IOException(msg)
        }
        return node_dir_source
    }

    def generate() throws IOException {
        def last_run_json = new File("$home/build/.last_run").text
        def last_run = new JsonSlurper().parseText(last_run_json)
        def node_path = new File("./node").getAbsolutePath()
        FileUtils.copyDirectory(new File(last_run.node_dir), new File(node_path))

        def evidence = last_run?.evidence
        def config_file = last_run?.config_file
        if (evidence && config_file) {
            def config = new ConfigSlurper().parse(new File(config_file).getText("MS932"))
            def target = config?.evidence?.target
            println target
        }
    }

    def exportCMDB() throws IOException, SQLException {
        def node_dir_source = getNodeDirSource()
        def db = new ConfigManageDB(home: home, db_config: db_config)
        db.export(node_dir_source)
    }

    def exportCMDBAll() throws IOException, SQLException {
        def db = new ConfigManageDB(home: home, db_config: db_config)
        db.export("./node")
    }
}

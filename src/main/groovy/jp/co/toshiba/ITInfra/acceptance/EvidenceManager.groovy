package jp.co.toshiba.ITInfra.acceptance

import groovy.io.FileType
import groovy.util.logging.Slf4j
import static groovy.json.JsonOutput.*
import groovy.json.*
import org.apache.commons.io.FileUtils
import groovy.sql.Sql
import java.sql.*

@Slf4j
class EvidenceManager {

    final static def current_build = 1
    final String archive_dir  = './build/archive'
    String getconfig_home
    String project_home
    String project_name
    String tenant_name
    String last_run_config
    String db_config
    String node_dir

    EvidenceManager(Map params) {
        assert params.project_home
        this.getconfig_home  = params.getconfig_home
        this.project_home    = params.project_home
        this.project_name    = new File(this.project_home).getName()
        this.tenant_name     = '_Default'
        this.last_run_config = params.last_run_config ?: "${params.project_home}/build/.last_run"
        this.db_config  = params.db_config ?: "${params.getconfig_home}/config/cmdb.groovy"
        this.node_dir   = params.node_dir ?: this.project_home + '/node'
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

    def archiveEvidence(String last_evidence, String backup_source)
        throws IOException {
        def archive = new File(archive_dir)
        archive.mkdirs()
        def last_evidence_name = new File(last_evidence).name
        (backup_source =~ /<date>/).with {
            def backup = new File(backup_source)
            def regexp = backup.name.replaceAll(/<date>/, '(.+)')
            new File(backup.parent).eachFile {
                if (it.name =~ /^${regexp}/ && it.name != last_evidence_name) {
                    it.renameTo(new File(archive, it.name))
                    log.info "Archive to ${archive_dir} : ${it.name}"
                }
            }
        }
    }

    def exportNodeDirectory() throws IOException {
        def last_run_json = new File(last_run_config).text
        def last_run = new JsonSlurper().parseText(last_run_json)
        def node_path = new File("./node").getAbsolutePath()
        assert(last_run.node_dir)
        FileUtils.copyDirectory(new File(last_run.node_dir), new File(node_path))
        try {
            archiveEvidence(last_run.evidence, last_run.target)
        } catch (IOException | NullPointerException e) {
            log.info "Skip evidence archive : " + e
        }
    }

    def exportCMDB() throws IOException, SQLException {
        def node_dir_source = getNodeDirSource()
        CMDBModel.instance.initialize(this)
        CMDBModel.instance.export(new File(node_dir_source).getAbsolutePath())
    }

    def exportCMDBAll() throws IOException, SQLException {
        CMDBModel.instance.initialize(this)
        CMDBModel.instance.export(new File(this.node_dir).getAbsolutePath())
    }
}

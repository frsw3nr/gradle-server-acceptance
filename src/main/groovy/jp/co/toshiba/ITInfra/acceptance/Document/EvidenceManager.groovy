package jp.co.toshiba.ITInfra.acceptance

import groovy.io.FileType
import groovy.util.logging.Slf4j
import static groovy.json.JsonOutput.*
import groovy.json.*
import org.apache.commons.io.FileUtils
import groovy.sql.Sql
import java.sql.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.Document.*

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
    String result_dir
    String node_dir
    String test_resource
    String evidence_log_share_dir
    Boolean silent

    // EvidenceManager(Map params = null) {
    //     // assert params.project_home
    //     // this.getconfig_home  = params.getconfig_home
    //     // this.project_home    = params.project_home
    //     // this.project_name    = new File(this.project_home).getName()
    //     // this.tenant_name     = '_Default'
    //     // this.last_run_config = params.last_run_config ?: "${params.project_home}/build/.last_run"
    //     // this.db_config       = params.db_config ?: "${params.getconfig_home}/config/cmdb.groovy"
    //     // this.node_dir        = params.node_dir ?: this.project_home + '/node'
    //     // this.test_resource   = params.test_resource ?: './src/test/resources/log'
    //     // this.silent          = params.silent
    // }

    def set_environment(ConfigTestEnvironment env) {
        this.getconfig_home         = env.get_getconfig_home()
        this.project_home           = env.get_project_home()
        this.project_name           = env.get_project_name()
        this.tenant_name            = env.get_tenant_name()
        this.last_run_config        = env.get_last_run_config()
        this.db_config              = env.get_db_config()
        this.result_dir             = env.get_result_dir()
        this.node_dir               = env.get_node_dir()
        this.test_resource          = env.get_test_resource()
        this.silent                 = env.get_silent()
        this.evidence_log_share_dir = env.get_evidence_log_share_dir()
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
        log.info "Archive log from './build/log' to '$test_resource'"
        FileUtils.copyDirectory(new File('./build/log'), new File(test_resource))
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

    def export_json(TestScenario test_scenario) throws IOException, SQLException {
        def result_writer = new TestResultWriter(result_dir: this.result_dir)
        test_scenario.accept(result_writer)
    }

    def copy_directory(String source_dir, String target_dir) throws IOException {
        assert(source_dir)
        assert(target_dir)
        def target_path = new File(target_dir).getAbsolutePath()
        FileUtils.copyDirectory(new File(source_dir), new File(target_path))
    }

    def archive_json() {
        copy_directory(this.result_dir, this.node_dir)
    }

    def update_evidence_log() {
        log.info "Copy test evidence to '${this.test_resource}'"
        copy_directory(this.evidence_log_share_dir, this.test_resource)
    }

    def update_db(TestScenario test_scenario) throws SQLException {
        def targets = test_scenario.test_targets.get_all()
        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                test_target.test_platforms.each { platform_name, test_platform ->
                    println "(TBD)Export DB : $target_name, $domain, $platform_name"
                }
            }
        }
    }

    def update(String export_type) {
        switch (export_type) {
            case 'local' :
                update_evidence_log()
                archive_json()
                break

            case 'db' :
                def test_env = ConfigTestEnvironment.instance
                def test_scheduler = new TestScheduler()
                test_env.accept(test_scheduler)
                test_scheduler.init()
                def test_scenario = test_scheduler.test_scenario
                def test_result_reader = new TestResultReader(
                                             result_dir: this.result_dir)
                test_scenario.accept(test_result_reader)
                this.update_db(test_scenario)
                break

            default :
                def msg = "Unkown export type : ${export_type}"
                throw new IllegalArgumentException(msg)
                break
        }
    }
}

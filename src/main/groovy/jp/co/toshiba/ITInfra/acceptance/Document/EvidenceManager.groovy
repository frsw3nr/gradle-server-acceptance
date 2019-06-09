package jp.co.toshiba.ITInfra.acceptance


import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

import java.sql.SQLException

@Slf4j
class EvidenceManager {

    String result_dir
    String node_dir
    String test_resource
    String evidence_log_share_dir

    def set_environment(ConfigTestEnvironment env) {
        this.result_dir             = env.get_result_dir()
        this.node_dir               = env.get_node_dir()
        this.test_resource          = env.get_test_resource()
        this.evidence_log_share_dir = env.get_evidence_log_share_dir()
    }

    def export_cmdb() throws IOException, SQLException {
        CMDBModel.instance.initialize()
        CMDBModel.instance.export(new File(this.result_dir).getAbsolutePath())
    }

    def export_cmdb_all() throws IOException, SQLException {
        CMDBModel.instance.initialize()
        CMDBModel.instance.export(new File(this.node_dir).getAbsolutePath())
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

    def update(String export_type) {
        switch (export_type) {
            case 'local' :
                this.update_evidence_log()
                this.archive_json()
                break

            case 'db' :
                this.export_cmdb()
                break

            case 'db-all' :
                this.export_cmdb_all()
                break

            default :
                def msg = "Unkown export type : ${export_type}"
                throw new IllegalArgumentException(msg)
                break
        }
    }
}

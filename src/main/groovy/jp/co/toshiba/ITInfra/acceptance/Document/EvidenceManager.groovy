package jp.co.toshiba.ITInfra.acceptance

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

import java.sql.SQLException

@Slf4j
@ToString(includePackage = false)
class EvidenceManager {

    String current_node_dir
    String project_node_dir
    String base_node_dir
    String current_test_log_dir
    String project_test_log_dir
    String base_test_log_dir

    def set_environment(ConfigTestEnvironment env) {
        this.current_node_dir     = env.get_current_node_dir()
        this.project_node_dir     = env.get_project_node_dir()
        this.base_node_dir        = env.get_base_node_dir()
        this.current_test_log_dir = env.get_current_test_log_dir()
        this.project_test_log_dir = env.get_project_test_log_dir()
        this.base_test_log_dir    = env.get_base_test_log_dir()
    }

    def export_cmdb() throws IOException, SQLException {
        CMDBModel.instance.initialize()
        CMDBModel.instance.export(new File(this.current_node_dir).getAbsolutePath())
    }

    def export_cmdb_all() throws IOException, SQLException {
        CMDBModel.instance.initialize()
        CMDBModel.instance.export(new File(this.project_node_dir).getAbsolutePath())
    }

    def copy_directory(String source_dir, String target_dir) throws IOException {
        assert(source_dir)
        assert(target_dir)
        def target_path = new File(target_dir).getAbsolutePath()
        FileUtils.copyDirectory(new File(source_dir), new File(target_path))
    }

    def archive_json() {
        copy_directory(this.current_node_dir, this.project_node_dir)
        if (this.project_node_dir != this.base_node_dir)
            copy_directory(this.current_node_dir, this.base_node_dir)
    }

    def update_evidence_log() {
        log.info "Copy test evidence to '${this.project_test_log_dir}'"
        copy_directory(this.current_test_log_dir, this.project_test_log_dir)
        if (this.project_test_log_dir != this.base_test_log_dir)
            copy_directory(this.current_test_log_dir, this.base_test_log_dir)
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

package jp.co.toshiba.ITInfra.acceptance

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
@ToString(includePackage = false)
class TestLog {

    String server_name
    String platform
    String base_test_log_dir
    String project_test_log_dir
    String current_test_log_dir

    def set_environment(ConfigTestEnvironment env) {
        this.base_test_log_dir    = env.get_base_test_log_dir()
        this.project_test_log_dir = env.get_project_test_log_dir()
        this.current_test_log_dir = env.get_current_test_log_dir()
    }

    TestLog(String server_name, String platform) {
        this.server_name = server_name
        this.platform = platform
    }

    def get_log_path_v1(String test_log_dir, String test_id, Boolean shared = false) {
        def log_path = null
        def staging_dir = new File(test_log_dir)
        if (!staging_dir.exists())
            return
        staging_dir.eachDir { old_domain ->
            def old_log_path = test_log_dir + '/' + old_domain.name
            if (!shared) {
                old_log_path += "/${server_name}/${platform}"
            }
            old_log_path += '/' + test_id
            if (new File(old_log_path).exists()) {
                log_path = old_log_path
            }
        }
        return log_path
    }

    def get_log_path(String test_log_dir, String test_id, Boolean shared = false) {
        def log_path = test_log_dir
        if (!shared) {
            log_path += "/${this.server_name}/${this.platform}"
        }
        log_path += '/' + test_id
        if (new File(log_path).exists()) {
            return log_path
        }
    }

    def get_source_log_path(String test_id, Boolean shared = false) {
        return this.get_log_path(this.project_test_log_dir, test_id, shared) ?:
               this.get_log_path(this.base_test_log_dir, test_id, shared) ?:
               this.get_log_path(this.current_test_log_dir, test_id, shared) ?:
               this.get_log_path_v1(this.project_test_log_dir, test_id, shared) ?: 
               this.get_log_path_v1(this.base_test_log_dir, test_id, shared) ?:
               this.get_log_path_v1(this.current_test_log_dir, test_id, shared)
    }

    def get_local_dir() {
        return "${this.current_test_log_dir}/${this.server_name}/${this.platform}"
    }

    def get_target_log_path(String test_id, Boolean shared = false) {
        def target_path = this.current_test_log_dir
        if (shared == false) {
            target_path += "/${this.server_name}/${this.platform}"
        }
        target_path += '/' + test_id
        return target_path
    }
}

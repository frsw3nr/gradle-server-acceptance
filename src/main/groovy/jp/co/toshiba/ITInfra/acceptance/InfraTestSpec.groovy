package jp.co.toshiba.ITInfra.acceptance

// import java.nio.file.Files
// import java.nio.file.Path
// import java.nio.file.Paths
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
import org.apache.commons.io.FileUtils

public enum RunMode {
    prepare,
    run,
    finish,
}

@Slf4j
@ToString(includePackage = false)
class InfraTestSpec {

    def config
    TestPlatform test_platform
    String server_name
    String platform
    String domain
    String title
    String project_test_log_dir
    String current_test_log_dir
    String local_dir
    int timeout
    Boolean debug
    Boolean dry_run
    Boolean verify_test
    Boolean skip_exec
    RunMode mode
    def server_info = [:]
    def dry_run_file_not_founds = []

    def InfraTestSpec(TestPlatform test_platform) {
        this.test_platform        = test_platform
        this.server_name          = test_platform?.test_target?.name
        this.platform             = test_platform.name
        this.domain               = test_platform?.test_target?.domain
        this.title                = domain + '(' + server_name + ')'
        this.current_test_log_dir = test_platform.current_test_log_dir
        // this.local_dir            = "${current_test_log_dir}/${platform}"
        this.local_dir            = test_platform.local_dir
        this.dry_run              = test_platform.dry_run
        this.verify_test          = test_platform.verify_test
        this.project_test_log_dir = test_platform.project_test_log_dir
        this.timeout              = test_platform.timeout ?: 0
        this.debug                = test_platform.debug
        this.mode                 = RunMode.prepare
        this.server_info          = test_platform?.test_target?.asMap()
    }

    def prepare = { Closure closure ->
        return closure.call()
    }

    def run_script = { String command, Closure closure ->
        if (mode == RunMode.prepare) {
            // Trim line endings
            command = command.replaceAll(/(\s|\r|\n)*$/, "")
            log.debug "Invoke WMI command : ${command}"
            return command
        } else if (mode == RunMode.run) {
            return closure.call()
        }
    }

    def run = { Closure closure ->
        if (mode == RunMode.run) {
            return closure.call()
        }
    }

    def get_log_path_v1(String test_id, Boolean shared = false) {
        def log_path = null
        def staging_dir = new File(project_test_log_dir)
        if (!staging_dir.exists())
            return
        staging_dir.eachDir { old_domain ->
            def old_log_path = project_test_log_dir + '/' + old_domain.name
            if (shared == false) {
                old_log_path += "/${server_name}/${platform}"
            }
            old_log_path += '/' + test_id
            // println "CHECK:${old_log_path}"
            if (new File(old_log_path).exists()) {
                log_path = old_log_path
            }
        }
        return log_path
    }

    def get_log_path(String test_id, Boolean shared = false) {
        def log_path = project_test_log_dir
        if (shared == false) {
            log_path += "/${server_name}/${platform}"
        }
        log_path += '/' + test_id
        // new File(log_path).exists() == false
        if (new File(log_path).exists()) {
            return log_path
        }
        return get_log_path_v1(test_id, shared) ?: log_path
    }

    def get_target_path(String test_id, Boolean shared = false) {
        def target_path = (shared) ? current_test_log_dir : local_dir
        target_path += '/' + test_id
        return target_path
    }

    def exec = { HashMap settings = [:], String test_id, Closure closure ->
        Boolean shared = settings['shared'] ?: false
        String  encode = settings['encode'] ?: null
        def log_path = get_log_path(test_id, shared)
        def target_path = get_target_path(test_id, shared)
        // def log_path = project_test_log_dir
        // if (shared == false) {
        //     log_path += "/${server_name}/${platform}"
        // }
        // log_path += '/' + test_id
        // def target_path = (shared) ? current_test_log_dir : local_dir
        // target_path += '/' + test_id
        if (dry_run) {
            log.debug "[DryRun] Read dummy log '${log_path}'"
            try {
                def source_log = new File(log_path)
                if (!source_log.exists()) {
                    println "NOT FOUND: ${log_path}"
                    return '{}'
                }
                def target_log = new File(target_path)
                if (!target_log.exists())
                    FileUtils.copyFile(source_log, target_log)
                return (encode) ? source_log.getText(encode) : source_log.text
            } catch (FileNotFoundException e) {
                dry_run_file_not_founds << test_id
                def message = "[DryRun] Not found : ${log_path}"
                // log.warn(message)
                throw new FileNotFoundException(message)
            }
        } else {
            def target_log = new File(target_path)
            if (shared == true && target_log.exists()) {
                return (encode) ? target_log.getText(encode) : target_log.text
            } else {
                return closure.call()
            }
        }
    }

    def execPowerShell(String script_path, String cmd) throws IOException {
        if (!dry_run) {
            def sout = new StringBuilder()
            def serr = new StringBuilder()
            def process = cmd.execute()
            process.consumeProcessOutput(sout, serr)

            if (this.timeout == 0) {
                process.waitFor()
            } else {
                process.waitForOrKill(1000 * this.timeout)
            }
            def rc = process.exitValue()
            if (this.debug) {
                println "[command]\n" + cmd
                println "[output]\n" + sout
            }
            if (rc != 0) {
                throw new IOException("Powershell return '${rc}' exit code : " + serr)
            }
            if (serr) {
                throw new IOException("Powershell script error : " + serr)
            }
        }
    }

    def runPowerShellTest(String template_dir, String domain, String cmd, TestItem[] test_items) {
        def code = new TestScriptGenerator(template_dir, domain)
        mode = RunMode.prepare
        test_items.each {
            def method = this.metaClass.getMetaMethod(it.test_id, TestItem)
            if (method) {
                def command = method.invoke(this, it)
                log.debug "fetch command ${method.name} : ${command}"
                code.addCommand(it.test_id, command)
            }
        }
        def ncommand = code.commands.size()
        if (ncommand > 0) {
            long start = System.currentTimeMillis();
            new File(script_path).write(code.generate())
            try {
                execPowerShell(script_path, cmd)
            } catch (IOException e) {
                log.error "[PowershellTest] Powershell script faild.\n" + e
                // Detect WinRM connection error from message
                if (e =~/(WinRM|Core_OutputHelper_WriteNotFoundError)/)
                    return
            }
            long elapsed = System.currentTimeMillis() - start
            log.info "Finish PowerShell script '${this.server_name}', Elapse : ${elapsed} ms"
            log.debug "\ttest : " + code.test_ids.toString()
            mode = RunMode.run
            def log_not_founds = []
            test_items.each {
                def log_path = get_log_path(it.test_id)
                def target_path = get_target_path(it.test_id)
                if (dry_run) {
                    try {
                        def source_log = new File(log_path)
                        if (!source_log.exists()) {
                            log_not_founds << it.test_id
                            return
                        } else {
                            def target_log = new File(target_path)
                            if (!target_log.exists())
                                FileUtils.copyFile(source_log, target_log)
                        }
                    } catch (FileNotFoundException e) {
                        log_not_founds << it.test_id
                        return
                    }
                }
                if (!new File(target_path).exists()) {
                    log_not_founds << it.test_id
                    // log.info "Log not found, skip : ${it.test_id}"
                    return
                }
                def method = this.metaClass.getMetaMethod(it.test_id, TestItem)
                if (method) {
                    log.debug "parse command ${method.name}"
                    try {
                        method.invoke(this, it)
                        // it.succeed = 1
                    } catch (Exception e) {
                        log.warn "[${domain}Test] '${method.name}()' faild, skip.\n" + e
                        e.printStackTrace()
                        it.status(false)
                        it.error_msg("${e}")
                    }
                }
            }
            if (log_not_founds.size() > 0) {
                log.info "Log not found, skip : ${log_not_founds}"
            }
        } else {
            log.warn "Test command is empty, skip domain test '${this.title}'."
        }
    }

    def init() {
        log.debug("Initialize infra test spec ${title}")

        def target_log_dir = new File(local_dir)
        if (target_log_dir.exists()) {
            def result = target_log_dir.deleteDir()
            Thread.sleep(1000)
        }
        int retry = 5
        Boolean success = false
        while (retry > 0 && (!success)) {
            try {
                success = target_log_dir.mkdirs()
            } catch (Exception e) {
                log.warn "Create dir error : " + e
                Thread.sleep(1000)
            } finally {
                retry --
            }
        }
    }

    def finish() {
        log.debug("Finish infra test spec ${title}")
    }

    def setup_exec() {
        log.debug("Start infra test spec '${server_name}'")
    }

    def cleanup_exec() {
        log.debug("Cleanup infra test spec '${server_name}'")
    }

    def summary_text(String test_id, Map results, Map result_labels) {
        def result_summarys = [:]
        result_labels.each { label_key, result_label ->
            [label_key, "${test_id}.${label_key}"].find { result_label_key ->
                def found = results?."${result_label_key}"
                if (found != null) {
                    result_summarys[result_label] = results[result_label_key]
                    return true
                }
            }
        }
        return (result_summarys) ? "${result_summarys}" : "Not Found"
    }

    def add_new_metric(String id, String description, value, Map results) {
        this.test_platform.add_test_metric(id, description)
        results[id] = value
    }
}

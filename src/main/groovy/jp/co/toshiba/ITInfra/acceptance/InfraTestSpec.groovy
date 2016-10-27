package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import groovy.transform.ToString

public enum RunMode {
    prepare,
    run,
    finish,
}

@Slf4j
class InfraTestSpec {

    def config
    TargetServer test_server
    String server_name
    String platform
    String domain
    String title
    String dry_run_staging_dir
    String local_dir
    int timeout
    Boolean dry_run
    Boolean skip_exec
    RunMode mode
    def server_info = [:]

    def InfraTestSpec(TargetServer test_server, String domain) {
        this.test_server         = test_server
        this.server_name         = test_server.server_name
        this.platform            = test_server.platform
        this.domain              = domain
        this.title               = domain + '('     + test_server.info() + ')'
        this.local_dir           = "${test_server.evidence_log_dir}/${domain}"
        this.dry_run             = test_server.dry_run
        this.dry_run_staging_dir = test_server.dry_run_staging_dir
        this.timeout             = test_server.timeout
        this.mode                = RunMode.prepare
        this.server_info         = test_server.infos
    }

    def prepare = { Closure closure ->
        return closure.call()
    }

    def run_script = { String command, Closure closure ->
        if (mode == RunMode.prepare) {
            // 文末の改行コードは取り除く
            command = command.replaceAll(/(\s|\r|\n)*$/, "")
            log.debug "Invoke WMI command : ${command}"
            return command
        } else {
            return closure.call()
        }
    }

    def run = { Closure closure ->
        if (mode == RunMode.run) {
            return closure.call()
        }
    }

    def exec = { String test_id, Closure closure ->
        if (dry_run) {
            def log_path = "${dry_run_staging_dir}/${platform}/${server_name}/${domain}/${test_id}"
            log.debug "[DryRun] Read dummy log '${log_path}'"
            try {
                def source_log = new File(log_path)
                def target_log = new File("${local_dir}/${test_id}")
                FileUtils.copyFile(source_log, target_log)
                return source_log.text
            } catch (FileNotFoundException e) {
                def message = "[DryRun] Not found dummy log : ${log_path}"
                log.error(message)
                throw new FileNotFoundException(message)
            }
        } else {
            return closure.call()
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
                log.error "[PowershellTest] Powershell script '${script_path}' faild, skip parse.\n" + e
                return
            }
            long elapsed = System.currentTimeMillis() - start
            log.info "Finish powershell script '${this.server_name}', Command : ${ncommand}, Elapsed : ${elapsed} ms"
            log.info "\ttest : " + code.test_ids.toString()
            mode = RunMode.run
            test_items.each {
                def method = this.metaClass.getMetaMethod(it.test_id, TestItem)
                if (method) {
                    log.debug "parse command ${method.name}"
                    try {
                        method.invoke(this, it)
                        it.succeed = 1
                    } catch (Exception e) {
                        log.error "[${domain}Test] Parser of '${method.name}()' faild, skip.\n" + e
                    }
                }
            }
        } else {
            log.warn "Test command is empty, skip domain test '${this.title}'."
        }
    }

    def init() {
        log.info("Initialize infra test spec ${title}")
        if (dry_run) {
            log.info("DryRun : 'Y'")
        }
        def target_log_dir = new File(local_dir)
        target_log_dir.deleteDir()
        target_log_dir.mkdirs()
    }

    def finish() {
        log.info("Finish infra test spec ${title}")
    }

    def setup_exec() {
        log.info("Start infra test spec '${server_name}'")
    }

    def cleanup_exec() {
        log.info("Cleanup infra test spec '${server_name}'")
    }
}

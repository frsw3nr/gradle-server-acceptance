package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString

public enum RunMode {
    prepare_script,
    run_script,
}

@Slf4j
class InfraTestSpec {

    def config
    TargetServer test_server
    String server_name
    String domain
    String title
    Boolean dry_run
    String dry_run_staging_dir
    int timeout
    Boolean skip_exec
    def mode

    def InfraTestSpec(TargetServer test_server, String domain) {
        this.test_server         = test_server
        this.server_name         = test_server.server_name
        this.domain              = domain
        this.title               = domain + '(' + test_server.info() + ')'
        this.dry_run             = test_server.dry_run
        this.dry_run_staging_dir = test_server.dry_run_staging_dir
        this.timeout             = test_server.timeout
        this.mode                = RunMode.prepare_script
    }

    def prepare = { Closure closure ->
        return closure.call()
    }

    def run_script = { String command, Closure closure ->
        if (mode == RunMode.prepare_script) {
            // コマンドは1行で実行するため、改行は取り除く
            command = command.replaceAll(/(\r|\n)/, "")
            log.debug "Invoke WMI command : ${command}"
            return command
        } else {
            return closure.call()
        }
    }

    def run = { Closure closure ->
        if (mode == RunMode.run_script) {
            return closure.call()
        }
    }

    def exec = { String test_id, Closure closure ->
        if (dry_run) {
            // test/resources/log/{サーバ名}/{検査項目}
            def log_path = "${dry_run_staging_dir}/${domain}/${server_name}/${test_id}"
            log.debug "[DryRun] Read dummy log '${log_path}'"
            try {
                return new File(log_path).text
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
        def code = new CodeGenerator(template_dir, domain)
        mode = RunMode.prepare_script
        test_items.each {
            def method = this.metaClass.getMetaMethod(it.test_id, TestItem)
            if (method) {
                def command = method.invoke(this, it)
                log.debug "fetch command ${method.name} : ${command}"
                code.addCommand(it.test_id, command)
            } else {
                log.warn "Test spec method '${it.test_id}(TestItem)' not found, skip."
            }
        }
        def ncommand = code.commands.size()
        if (ncommand > 0) {
            long start = System.currentTimeMillis();
            new File(script_path).write(code.generate())
            try {
                execPowerShell(script_path, cmd)
            } catch (IOException e) {
                log.error "[vCenterTest] Powershell script '${script_path}' faild, skip parse.\n" + e
                return
            }
            long elapsed = System.currentTimeMillis() - start
            log.info "Finish powershell script '${this.server_name}', Command : ${ncommand}, Elapsed : ${elapsed} ms"
            log.info "\ttest : " + code.test_ids.toString()
            mode = RunMode.run_script
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
        def local_dir = new File(test_server.evidence_log_dir)
        local_dir.deleteDir()
        local_dir.mkdirs()
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

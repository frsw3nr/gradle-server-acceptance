package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

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
    TargetServer test_server
    String server_name
    String platform
    String domain
    String title
    String dry_run_staging_dir
    String evidence_log_dir
    String evidence_log_share_dir
    String local_dir
    int timeout
    Boolean debug
    Boolean dry_run
    Boolean skip_exec
    RunMode mode
    def server_info = [:]

    def InfraTestSpec(TestPlatform test_platform) {
        // this.test_server            = test_server
        this.test_platform          = test_platform
        this.server_name            = test_platform.test_target.name
        this.platform               = test_platform.name
        this.domain                 = test_platform.test_target.domain
        this.title                  = domain + '(' + server_name + ')'
        this.evidence_log_dir       = test_platform.evidence_log_dir
        this.evidence_log_share_dir = test_platform.evidence_log_share_dir
        this.local_dir              = "${evidence_log_dir}/${platform}"
        this.dry_run                = test_platform.dry_run
        this.dry_run_staging_dir    = test_platform.dry_run_staging_dir
        this.timeout                = test_platform.timeout ?: 0
        this.debug                  = test_platform.debug
        this.mode                   = RunMode.prepare
        this.server_info            = test_platform?.test_target?.asMap()
    }

    // def InfraTestSpec(TargetServer test_server, String domain) {
    //     this.test_server            = test_server
    //     this.server_name            = test_server.server_name
    //     this.platform               = test_server.platform
    //     this.domain                 = domain
    //     this.title                  = domain + '(' + test_server.info() + ')'
    //     this.evidence_log_dir       = test_server.evidence_log_dir
    //     this.evidence_log_share_dir = test_server.evidence_log_share_dir
    //     this.local_dir              = "${evidence_log_dir}/${domain}"
    //     this.dry_run                = test_server.dry_run
    //     this.dry_run_staging_dir    = test_server.dry_run_staging_dir
    //     this.timeout                = test_server.timeout
    //     this.debug                  = test_server.debug
    //     this.mode                   = RunMode.prepare
    //     this.server_info            = test_server.infos
    // }

    def prepare = { Closure closure ->
        return closure.call()
    }

    def run_script = { String command, Closure closure ->
        if (mode == RunMode.prepare) {
            // Trim line endings
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

    def exec = { HashMap settings = [:], String test_id, Closure closure ->
        def log_path = "${dry_run_staging_dir}/${domain}"
        Boolean shared = settings['shared'] ?: false
        String  encode = settings['encode'] ?: null
        if (shared == false) {
            log_path += "/${server_name}/${platform}"
        }
        log_path += '/' + test_id
        def target_path = (shared) ? evidence_log_share_dir : local_dir
        target_path += '/' + test_id
        if (dry_run) {
            log.debug "[DryRun] Read dummy log '${log_path}'"
            try {
                def source_log = new File(log_path)
                def target_log = new File(target_path)
                if (!target_log.exists())
                    FileUtils.copyFile(source_log, target_log)
                return (encode) ? source_log.getText(encode) : source_log.text
            } catch (FileNotFoundException e) {
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

    def addAdditionalTestItem(TestItem test_item, String test_id, 
                              String test_name = null, String desc = null) {
        test_item.additional_test_items[test_id] = [
            'test_name': test_name, 
            'domain':    this.domain, 
            'desc':      desc,
        ]
    }

    def target_info(String item, String platform = null) {
        if (!platform)
            platform = this.platform
        if (server_info.containsKey(item))
            return server_info[item]
        if (!server_info.containsKey(platform) ||
            !server_info[platform].containsKey(item))
            return
        return server_info[platform][item]
    }

    def verify_data(Map infos, Closure check_closure) {
        def checks = [:]
        infos.each { info_name, info_value ->
            def test_value = target_info(info_name)
            if (test_value) {
                def check = check_closure(info_value, test_value)
                println "CHECK:$info_name, $info_value, $test_value, $check"
                checks[info_name] = check
            }
        }
        return checks
    }

    def verify_data_match(Map infos) {
        Closure intermediate_match = { String a, String b ->
            return (a =~ /$b/) as boolean
        }
        return verify_data(infos, intermediate_match)
    }

    def verify_data_equal_number(Map infos) {
        Closure  equal_number = { a, b -> ("${a * 1.0}" == "${b * 1.0}") }
        return verify_data(infos, equal_number)
    }

    def verify_data_error_range(Map infos, double error_rate = 0.1) {
        Closure  error_range = { a, b ->
            int value_a = a as Integer
            int value_b = b as Integer
            def max_value = Math.max(value_a, value_b)
            def differ = Math.abs(value_a - value_b)
            return ((1.0 * differ / max_value) < error_rate) as boolean
        }
        return verify_data(infos, error_range)
    }

    def convert_array(element) {
        return (element.getClass() == String) ? [element] : element
    }

    def verify_map(Object target_checks, Map infos, String prefix = null) {
        target_checks = convert_array(target_checks)
        def validate = true
        target_checks.each { target_check ->
            (target_check =~ /(.+):(.+)/).each {m0, device, check_value ->
                if (prefix)
                    device = prefix + '.' + device
                println infos
                def status = infos[device]
                if (!status || status != check_value)
                    validate = false
                println "CHECK: $status, $device, $check_value, $validate"
            }
        }
        return validate
    }

    def verify_list(Object target_checks, Map infos, String prefix = null) {
        target_checks = convert_array(target_checks)
        def validate = true
        println infos
        target_checks.each { device ->
            if (prefix)
                device = prefix + '.' + device
            if (!infos.containsKey(device))
                validate = false
            println "CHECK: $device, $validate"
        }
        return validate
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
            }
            long elapsed = System.currentTimeMillis() - start
            log.debug "Finish PowerShell script '${this.server_name}', Command : ${ncommand}, Elapsed : ${elapsed} ms"
            log.debug "\ttest : " + code.test_ids.toString()
            mode = RunMode.run
            test_items.each {
                def method = this.metaClass.getMetaMethod(it.test_id, TestItem)
                if (method) {
                    log.debug "parse command ${method.name}"
                    try {
                        method.invoke(this, it)
                        // it.succeed = 1
                    } catch (Exception e) {
                        it.status(false)
                        log.warn "[${domain}Test] Parser of '${method.name}()' faild, skip.\n" + e
                    }
                }
            }
        } else {
            log.warn "Test command is empty, skip domain test '${this.title}'."
        }
    }

    def init() {
        log.debug("Initialize infra test spec ${title}")

        def target_log_dir = new File(local_dir)
        target_log_dir.deleteDir()
        target_log_dir.mkdirs()
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
}

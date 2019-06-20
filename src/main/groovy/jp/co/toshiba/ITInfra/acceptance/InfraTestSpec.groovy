package jp.co.toshiba.ITInfra.acceptance

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
    String dry_run_staging_dir
    String evidence_log_dir
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
        this.server_name          = test_platform.test_target.name
        this.platform             = test_platform.name
        this.domain               = test_platform.test_target.domain
        this.title                = domain + '(' + server_name + ')'
        this.evidence_log_dir     = test_platform.evidence_log_dir
        // this.current_test_log_dir = test_platform.current_test_log_dir
        this.current_test_log_dir = this.evidence_log_dir
        println "EVIDENCE_LOG_DIR:$evidence_log_dir"
        println "CURRENT_TEST_LOG_DIR:$current_test_log_dir"
        // this.local_dir            = "${evidence_log_dir}/${platform}"
        this.local_dir            = "${evidence_log_dir}/${platform}"
        this.dry_run              = test_platform.dry_run
        this.verify_test          = test_platform.verify_test
        this.dry_run_staging_dir  = test_platform.dry_run_staging_dir
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
        def staging_dir = new File(dry_run_staging_dir)
        if (!staging_dir.exists())
            return
        staging_dir.eachDir { old_domain ->
            def old_log_path = dry_run_staging_dir + '/' + old_domain.name
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
        def log_path = dry_run_staging_dir
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
        println "LOG_PATH:${log_path}"
        def target_path = get_target_path(test_id, shared)
        println "TARGET_PATH:${target_path}"
        // def log_path = dry_run_staging_dir
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

    // def target_info(String item, String platform = null) {
    //     if (!platform)
    //         platform = this.platform
    //     if (server_info.containsKey(item)) {
    //         def value = server_info[item]
    //         if (value != null && !nullList.empty)
    //             return server_info[item]
    //     }
    //     if (!server_info.containsKey(platform) ||
    //         !server_info[platform].containsKey(item)) {
    //         return
    //     }
    //     return server_info[platform][item]
    // }

    // def verify_text_search(String item_name, String value) {
    //     def test_value = target_info(item_name)
    //     if (test_value) {
    //         def check = (value =~ /$test_value/) as boolean
    //         log.info "Check ${item_name}, '${value}'' =~ /${test_value}/, OK : ${check}"
    //     }
    // }

    // def verify_data(Map infos, Closure check_closure) {
    //     def checks = [:]
    //     infos.each { info_name, info_value ->
    //         def test_value = target_info(info_name)
    //         if (test_value) {
    //             def check = check_closure(info_value, test_value)
    //             log.info "CHECK:$info_name, $info_value, $test_value, $check"
    //             checks[info_name] = check
    //         }
    //     }
    //     return checks
    // }

    // def verify_data_match(Map infos) {
    //     Closure intermediate_match = { String a, String b ->
    //         if (NumberUtils.isNumber(a) && NumberUtils.isNumber(b))
    //             return (NumberUtils.toDouble(a) == NumberUtils.toDouble(b)) as boolean
    //         else
    //             return (a =~ /$b/) as boolean
    //     }
    //     return verify_data(infos, intermediate_match)
    // }

    // def verify_data_equal_number(Map infos) {
    //     Closure  equal_number = { a, b -> ("${a * 1.0}" == "${b * 1.0}") }
    //     return verify_data(infos, equal_number)
    // }

    // def verify_data_error_range(Map infos, double error_rate = 0.1) {
    //     Closure  error_range = { a, b ->
    //         double value_a = a as Double
    //         double value_b = b as Double
    //         def max_value = Math.max(value_a, value_b)
    //         def differ = Math.abs(value_a - value_b)
    //         println "verify_data_error_range:$value_a,$value_b,$differ"
    //         return ((1.0 * differ / max_value) < error_rate) as boolean
    //     }
    //     return verify_data(infos, error_range)
    // }

    // def convert_array(element) {
    //     return (element.getClass() == String) ? [element:1] : element
    // }

    // def verify_map(Map target_checks, Map infos, String prefix = null) {
    //     target_checks = convert_array(target_checks)
    //     def validate = true
    //     target_checks.each { device, check_value ->
    //         if (prefix)
    //             device = prefix + '.' + device
    //         def status = infos[device]
    //         if (!status) {
    //             validate = false
    //         } else if (status.getClass() =~ /String/) {
    //             if (status != check_value)
    //                 validate = false
    //         } else if (status instanceof Number) {
    //             if (status as Double != check_value as Double)
    //                 validate = false
    //         } else {
    //             log.warn "Unkown verify data : $status"
    //             validate = false
    //         }
    //         log.info "CHECK: $status, $device, $check_value, $validate"
    //     }
    //     return validate
    // }

    // def verify_list(Object target_checks, Map infos, String prefix = null) {
    //     target_checks = convert_array(target_checks)
    //     def validate = true
    //     target_checks.each { device, check_value ->
    //         if (prefix)
    //             device = prefix + '.' + device
    //         if (!infos.containsKey(device))
    //             validate = false
    //         log.info "CHECK: $device, $validate"
    //     }
    //     return validate
    // }

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

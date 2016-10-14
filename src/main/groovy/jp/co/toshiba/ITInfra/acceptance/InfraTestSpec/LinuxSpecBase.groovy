package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.session.execution.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
@InheritConstructors
class LinuxSpecBase extends InfraTestSpec {

    String ip
    String os_user
    String os_password
    String work_dir

    def init() {
        super.init()

        this.ip          = test_server.ip
        def os_account   = test_server.os_account
        this.os_user     = os_account['user']
        this.os_password = os_account['password']
        this.work_dir    = os_account['work_dir']
    }

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()

        def ssh = Ssh.newService()
        ssh.remotes {
            ssh_host {
                host = this.ip
                port = 22
                user = this.os_user
                password = this.os_password
                knownHosts = allowAnyHosts
            }
        }
        ssh.settings {
            dryRun = this.dry_run
        }
        ssh.run {
            session(ssh.remotes.ssh_host) {
                try {
                    execute "mkdir -vp ${work_dir}"
                } catch (Exception e) {
                    log.error "[SSH Test] Working directory in ${this.server_name} faild, skip.\n" + e
                    return
                }

                test_items.each {
                    def method = this.metaClass.getMetaMethod(it.test_id, Object, TestItem)
                    if (method) {
                        log.debug "Invoke command '${method.name}()'"
                        try {
                            long start = System.currentTimeMillis();
                            method.invoke(this, delegate, it)
                            long elapsed = System.currentTimeMillis() - start
                            log.info "Finish test method '${method.name}()' in ${this.server_name}, Elapsed : ${elapsed} ms"
                            it.succeed = 1
                        } catch (Exception e) {
                            log.error "[SSH Test] Test method '${method.name}()' faild, skip.\n" + e
                        }
                    } else {
                        log.warn "Test method '${it.test_id}(TestItem)' not found, skip."
                    }
                }
                remove work_dir
            }
        }
    }

    def run_ssh_command(session, command, test_id) {
        session.execute "${command} > ${work_dir}/${test_id}"
        session.get from: "${work_dir}/${test_id}", into: local_dir
        new File("${local_dir}/${test_id}").text
    }

    def hostname(session, test_item) {
        def lines = exec('hostname') {
            run_ssh_command(session, 'hostname -s', 'hostname')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def hostname_fqdn(session, test_item) {
        def lines = exec('hostname_fqdn') {
            run_ssh_command(session, 'hostname --fqdn', 'hostname_fqdn')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }

    def cpu(session, test_item) {
        def lines = exec('cpu') {
            run_ssh_command(session, 'cat /proc/cpuinfo', 'cpu')
        }

        def cpuinfo    = [:].withDefault{0}
        def real_cpu   = [:].withDefault{0}
        def cpu_number = 0
        lines.eachLine {
            (it =~ /processor\s+:\s(.+)/).each {m0,m1->
                cpu_number += 1
            }
            (it =~ /physical id\s+:\s(.+)/).each {m0,m1->
                real_cpu[m1] = true
            }
            (it =~ /cpu cores\s+:\s(.+)/).each {m0,m1->
                cpuinfo["cores"] = m1
            }
            (it =~ /model name\s+:\s(.+)/).each {m0,m1->
                cpuinfo["model_name"] = m1
            }
            (it =~ /cpu MHz\s+:\s(.+)/).each {m0,m1->
                cpuinfo["mhz"] = m1
            }
            (it =~ /cache size\s+:\s(.+)/).each {m0,m1->
                cpuinfo["cache_size"] = m1
            }
        }
        cpuinfo["cpu_total"] = cpu_number
        cpuinfo["cpu_real"] = real_cpu.size()
        cpuinfo["cpu_cores"] = real_cpu.size() * cpuinfo["cpu_cores"].toInteger()

        test_item.results(cpuinfo)
    }

}

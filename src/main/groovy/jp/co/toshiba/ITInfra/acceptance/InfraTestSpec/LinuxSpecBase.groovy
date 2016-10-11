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

    String server_name
    String ip
    String os_user
    String os_password
    String work_dir

    def init() {
        super.init()
        this.server_name = test_server.server_name
        this.ip          = test_server.ip

        def os_account   = test_server.os_account
        this.os_user     = os_account['user']
        this.os_password = os_account['password']
        this.work_dir    = os_account['work_dir']

        println "test1 ${server_name} ${ip} ${os_user}"
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
            // dryRun = true
        }

        ssh.run {
            session(ssh.remotes.ssh_host) {
                test_items.each {
                    def method = this.metaClass.getMetaMethod(it.test_id, Object, TestItem)
                    println "method : ${method.name}"
                    method.invoke(this, delegate, it)
                }
            }
        }
    }

    def hostname(session, test_item) {
        def lines = exec {
            session.execute "hostname -s > ${work_dir}/hostname"
            session.get from: "${work_dir}/hostname"
        }
        test_item.results(lines)
        println "parse hostname : ${lines}"
    }

    def hostname_fqdn(session, test_item) {
        def lines = exec {
            session.execute "hostname --fqdn > ${work_dir}/hostname_fqdn";
            session.get from: "${work_dir}/hostname_fqdn"
        }
        test_item.results(lines)
        println "parse hostname_fqdn : ${lines}"
    }

    def cpu(session, test_item) {
        def lines = exec {
            session.execute "cat /proc/cpuinfo > ${work_dir}/cpu"
            session.get from: "${work_dir}/cpu"
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
        cpuinfo["total"] = cpu_number
        cpuinfo["real"] = real_cpu.size()
        cpuinfo["cores"] = real_cpu.size() * cpuinfo["cores"].toInteger()

        test_item.results(cpuinfo)
    }

}

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
class WindowsSpecBase extends InfraTestSpec {

    String server_name
    String ip
    String os_user
    String os_password
    String local_dir

    def init() {
        super.init()
        this.server_name = test_server.server_name
        this.ip          = test_server.ip

        def os_account   = test_server.os_account
        this.os_user     = os_account['user']
        this.os_password = os_account['password']

        // 実行ログのローカル保存先ディレクトリを初期化
        this.local_dir = "build/log/windows/${this.server_name}"
        def local_dirs = new File(this.local_dir)
        local_dirs.deleteDir()
        local_dirs.mkdirs()

        println "test1 ${server_name} ${ip} ${os_user}"
    }


    def exec_windows_shell(String script) {
        log.info("")
        def cmd = "powershell -NonInteractive ${script}"
        cmd    += " -ip ${this.ip} -server ${this.server_name} -user ${this.os_user} -password ${this.os_password}"
        def sout = new StringBuilder()
        def serr = new StringBuilder()
        def proc = cmd.execute()
        proc.consumeProcessOutput(sout, serr)
        proc.waitForOrKill(20000)
        println "out> $sout err> $serr"
    }

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()
        test_items.each {
            def method = this.metaClass.getMetaMethod(it.test_id, TestItem)
            println "method : ${method.name}"
            method.invoke(this, it)
        }
    }

    def exec = { Closure closure ->
        if (this.mode == RunMode.prepare_script) {
            closure.call()
        }
    }

    def cpu(TestItem test_item) {
        def lines = exec {
            exec_windows_shell('lib/script/windows_cpu.ps1')
            new File("${this.local_dir}/cpu")
        }

        def cpuinfo    = [:].withDefault{0}
        def cpu_number = 0
        lines.eachLine {
            (it =~ /DeviceID\s+:\s(.+)/).each {m0, m1->
                cpu_number += 1
            }
            (it =~ /Name\s+:\s(.+)/).each {m0, m1->
                cpuinfo["model_name"] = m1
            }
            (it =~ /MaxClockSpeed\s+:\s(.+)/).each {m0, m1->
                cpuinfo["mhz"] = m1
            }
        }
        cpuinfo["total"] = cpu_number
        test_item.results(cpuinfo)
    }

    def memory(TestItem test_item) {
        exec_windows_shell('lib/script/windows_memory.ps1')
        def res = new File("${this.local_dir}/memory")
        test_item.results('テスト中')
    }

}

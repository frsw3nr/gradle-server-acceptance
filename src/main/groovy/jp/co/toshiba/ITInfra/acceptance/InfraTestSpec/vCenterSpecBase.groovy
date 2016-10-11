package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import jp.co.toshiba.ITInfra.acceptance.*

public enum RunMode {
    prepare_script,
    run_script,
}

@Slf4j
@InheritConstructors
class vCenterSpecBase extends InfraTestSpec {

    String server_name
    String vcenter_ip
    String vcenter_user
    String vcenter_password
    String vm
    String local_dir
    String script_path
    String script_buffer
    def mode = RunMode.prepare_script

    def init() {
        super.init()
        this.server_name = test_server.server_name

        def vcenter_account   = test_server.vcenter_account
        this.vcenter_ip       = vcenter_account['server']
        this.vcenter_user     = vcenter_account['user']
        this.vcenter_password = vcenter_account['password']
        this.vm               = test_server.vm

        // 実行ログのローカル保存先ディレクトリを初期化
        this.local_dir = "build/log/vcenter/${this.server_name}"
        def local_dirs = new File(this.local_dir)
        local_dirs.deleteDir()
        local_dirs.mkdirs()
        script_path = "${this.local_dir}/vmware_get_vm.ps1"
        script_buffer = '''\
            |Param(
            |    [string]$vm
            |  , [string]$server
            |  , [string]$vcenter
            |  , [string]$user
            |  , [string]$password
            |)
            |Add-PSSnapin VMware.VimAutomation.Core
            |$log_file = "./build/log/vcenter/" + $server + "/vm"
            |Connect-VIServer -User $user -Password $password -Server $vcenter
        '''.stripMargin()

        println "test1 ${server_name} ${vcenter_ip} ${vcenter_user}"
    }

    def exec_vcenter_shell(String script) {
        log.info("")
        def cmd = "powershell -NonInteractive ${script}"
        cmd    += " -vm ${this.vm} -server ${this.server_name} -user ${this.vcenter_user} -password ${this.vcenter_password} -vcenter ${this.vcenter_ip}"

        def sout = new StringBuilder()
        def serr = new StringBuilder()
        def proc = cmd.execute()
        proc.consumeProcessOutput(sout, serr)
        proc.waitForOrKill(20000)
        println "out> $sout err> $serr"
    }

    def setup_exec(TestItem[] test_items) {
        mode = RunMode.prepare_script
        test_items.each {
            def method = this.metaClass.getMetaMethod(it.test_id, TestItem)
            println "prepare : ${method.name}"
            method.invoke(this, it)
        }
        new File(script_path).write(script_buffer)
        exec_vcenter_shell(script_path)

        mode = RunMode.run_script
        test_items.each {
            def method = this.metaClass.getMetaMethod(it.test_id, TestItem)
            println "parse : ${method.name}"
            method.invoke(this, it)
        }

    }

    def prepare = { Closure closure ->
        if (this.mode == RunMode.prepare_script) {
            closure.call()
        }
    }

    def vm(test_item) {

        prepare {
            append_test_script('''\
                |$log_file = "./build/log/vcenter/" + $server + "/vm"
                |get-vm $vm | select NumCpu, PowerState, MemoryGB, VMHost | Out-File $log_file -Encoding UTF8
            '''.stripMargin()
            )
        }

        def lines = exec {
            new File("$local_dir/vm")
        }

        def res = [:]
        lines.eachLine {
            println "LINE:${it}"
            // (it =~  /^\s+(\d+.+)$/).each { m0,m1->
            //     def arr = m1.split(/\s+/)
            //     res['NumCPU']     = arr[0]
            //     res['PowerState'] = arr[1]
            //     res['MemoryGB']   = arr[2]
            //     res['VMHost']     = arr[3]
            // }
        }
        test_item.results(res)
    }

}

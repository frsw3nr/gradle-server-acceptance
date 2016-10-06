package jp.co.toshiba.ITInfra.acceptance.TestSpec

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import org.hidetake.groovy.ssh.Ssh

@Slf4j
class Windows {

    def execCommand {

        def sout = new StringBuilder()
        def serr = new StringBuilder()

        def cmd = "powershell -NonInteractive -Command ./script/windows_cpu.ps1 -ip"

        def proc = cmd.execute()
        proc.consumeProcessOutput(sout, serr)
        // proc.getOut().close()
        proc.waitForOrKill(30000)
        // 10000(10秒)だと、タイムアウトエラー(RC=1)になる
        def rc = proc.exitValue()

        println "out> $sout err> $serr rc> $rc"

    }

}

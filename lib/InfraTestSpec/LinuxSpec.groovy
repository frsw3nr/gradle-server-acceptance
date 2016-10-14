package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.hidetake.groovy.ssh.Ssh
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

@Slf4j
@InheritConstructors
class LinuxSpec extends LinuxSpecBase {

    def init() {
        super.init()
    }

    def finish() {
    }

    def hostname(session) {
        exec {
            session.execute "hostname -s > ${work_dir}/hostname"
        }
        parse {
            def result = session.get from: "${work_dir}/hostname"
            println "parse hostname : ${result}"
        }
    }

    // def hostname(ses) {
    //     // def session = ssh.run.session(ssh.remotes.ssh_host)
    //     ses.execute "hostname -s > ${work_dir}/hostname2"
    // }
}

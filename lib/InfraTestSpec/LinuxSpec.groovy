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
        super.finish()
    }

    def vncserver(session, test_item) {
        def lines = exec('vncserver') {
            run_ssh_command(session, '/sbin/chkconfig --list|grep vncserver', 'vncserver')
        }
        def vncserver = 'off'
        lines.eachLine {
            ( it =~ /\s+3:(.+?)\s+4:(.+?)\s+5:(.+?)\s+/).each {m0,m1,m2,m3->
                if (m1 == 'on' && m2 == 'on' && m3 == 'on') {
                    vncserver = 'on'
                }
            }
        }
        test_item.results(vncserver)
    }

    // def hostname(ses) {
    //     // def session = ssh.run.session(ssh.remotes.ssh_host)
    //     ses.execute "hostname -s > ${work_dir}/hostname2"
    // }
}

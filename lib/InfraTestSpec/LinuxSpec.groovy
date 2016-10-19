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

    def packages(session, test_item) {
        super.packages(session, test_item)

        def lines = new File("${local_dir}/packages").text
        def packages = [:].withDefault{0}
        def requiements = [:]
        ['compat-libcap1','compat-libstdc++-33','libstdc++-devel', 'gcc-c++','ksh','libaio-devel'].each {
            requiements[it] = 1
        }
        def n_requiements = 0
        lines.eachLine {
            def arr = it.split(/\t/)
            def packagename = arr[0]
            if (requiements[packagename])
                n_requiements ++
        }
        packages['requiement_for_oracle'] = (requiements.size() == n_requiements) ? 'OK' : 'NG'

        test_item.results(packages)
    }


    def oracle_module(session, test_item) {
        def lines = exec('oracle_module') {
            def command = "ls /root/sfw/* >> ${work_dir}/oracle_module"
            session.executeSudo command
            session.get from: "${work_dir}/oracle_module", into: local_dir
            new File("${local_dir}/oracle_module").text
        }
        test_item.results(lines)
    }
}

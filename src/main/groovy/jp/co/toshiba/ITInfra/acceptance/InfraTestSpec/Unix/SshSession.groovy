package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.Unix

import javax.xml.bind.*
import static groovy.json.JsonOutput.*
import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors

// For ssh session
// import org.hidetake.groovy.ssh.Ssh
import ch.ethz.ssh2.Connection

import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
class SshSession {

    int timeout = 30
    String evidence_log_share_dir
    String local_dir

    Connection ssh

    SshSession(test_spec) {
        this.timeout   = test_spec?.timeout ?: 30
        this.local_dir = test_spec?.local_dir
        this.evidence_log_share_dir = test_spec?.evidence_log_share_dir
    }

    def init_session(String ip, String user, String password) {
        ssh = new Connection(ip, 22)
        try {
            ssh.connect()
            def result = ssh.authenticateWithPassword(user, password)
            if (!result) {
                throw new IOException("Connect failed")
            }
        } catch (Exception e) {
            log.error "[SSH Test] Init test faild.\n" + e
            throw new IllegalArgumentException(e)
        }
    }

    def run_command(command, test_id, share = false) {
        try {
            def log_path = (share) ? evidence_log_share_dir : local_dir

            def session = ssh.openSession()
            session.execCommand command
            def result = session.stdout.text
            new File("${log_path}/${test_id}").text = result
            session.close()
            return result

        } catch (Exception e) {
            log.error "[SSH Test] Command error '$command' in ${this.server_name} faild, skip.\n" + e
        }
    }

    def close() {
        ssh.close()
    }
}

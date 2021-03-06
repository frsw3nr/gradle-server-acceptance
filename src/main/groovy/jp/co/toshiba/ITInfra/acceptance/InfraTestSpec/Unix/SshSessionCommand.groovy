package jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.Unix

import ch.ethz.ssh2.Connection
import groovy.util.logging.Slf4j

// For ssh session
// import org.hidetake.groovy.ssh.Ssh

@Slf4j
class SshSessionCommand {

    int timeout = 30
    String current_test_log_dir
    String local_dir

    Connection ssh

    SshSessionCommand(test_spec) {
        this.timeout   = test_spec?.timeout ?: 30
        this.local_dir = test_spec?.local_dir
        this.current_test_log_dir = test_spec?.current_test_log_dir
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
            def log_path = (share) ? current_test_log_dir : local_dir

            def session = ssh.openSession()
            println "TEST1:$command"
            session.execCommand command
            // session.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA | ChannelCondition.EOF, 1000 * timeout);
            println "TEST2:"
            def result = session.stdout.text
            println "TEST3:$result"
            new File("${log_path}/${test_id}").text = result
            session.close()
            return result

        } catch (Exception e) {
            log.error "[SSH Test] Command error '$command', skip.\n" + e
        }
    }

    def close() {
        ssh.close()
    }
}

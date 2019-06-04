@GrabConfig( systemClassLoader=true )
@Grapes( 
@Grab('ch.ethz.ganymed:ganymed-ssh2:262')
)

import javax.xml.bind.*
import static groovy.json.JsonOutput.*
import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors


// For ssh session
// import org.hidetake.groovy.ssh.Ssh
import ch.ethz.ssh2.Connection
import ch.ethz.ssh2.StreamGobbler
import ch.ethz.ssh2.Session

// TODO
// 複数コマンドの実装
// sh実行
// LANG=Cに変更
// コマンド実行
// http://d.hatena.ne.jp/n_shuyo/20060714/1152899236
// 接続ユーザは /bin/csh で、LANG=EUC_JPの場合を想定

def ses = new SshSession()
ses.init_session('192.168.10.3', 'someuser', 'P@ssw0rd')
def res0 = ses.run_command('hostname', 'uname')
println "RES0:$res0<EOF>"

class SshSession {

    int timeout = 30
    String evidence_log_share_dir = '/tmp'
    String local_dir = '/tmp'

    Connection ssh

    def init_session(String ip, String user, String password) {
        ssh = new Connection(ip, 22)
        try {
            ssh.connect()
            def result = ssh.authenticateWithPassword(user, password)
            if (!result) {
                throw new IOException("Connect failed")
            }
        } catch (Exception e) {
            // log.error "[SSH Test] Init test faild.\n" + e
            throw new IllegalArgumentException(e)
        }
    }

    String readUntil(Session session, String pattern) {
        InputStream stdout = new StreamGobbler(session.getStdout());
        BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

        StringBuffer sb = new StringBuffer();
        while (true) {
            String line = br.readLine();
            println "READY:${br.ready()}"
            println "BUFFER:$line<EOL>"
            sb.append(line);
            if (br.ready() == false || line == null)
                break;
            // System.out.println(line);
        }
        return sb.toString()
    }

    def run_command(command, test_id, share = false) {
        try {
            def log_path = (share) ? evidence_log_share_dir : local_dir

            def session = ssh.openSession()
            session.startShell()
            OutputStream sin = session.getStdin();
            println "TEST1"
            sin.write("sh\n".getBytes());
            // session.execCommand("sh")
            println "TEST2"
            sin.write("LANG=C\n".getBytes());
            // session.execCommand("LANG=C")

            // sleep(1000);
            println "TEST3:$command"
            sin.write("${command}\n".getBytes());
            println "TEST4"

            // session.execCommand command
            def result = readUntil(session, '% ')
            println "TEST5:$result"
            new File("${log_path}/${test_id}").text = result
            session.close()
            return result

        } catch (Exception e) {
            println "[SSH Test] Command error '$command', skip.\n" + e
        }
    }

    def close() {
        ssh.close()
    }
}


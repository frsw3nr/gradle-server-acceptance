@GrabConfig( systemClassLoader=true )
@Grapes( [
@Grab('ch.ethz.ganymed:ganymed-ssh2:262'),
@Grab('net.sf.expectit:expectit-core:0.9.0'),
])

// For ssh session
// import org.hidetake.groovy.ssh.Ssh
import ch.ethz.ssh2.Connection
import net.sf.expectit.Expect
import net.sf.expectit.ExpectBuilder
import static net.sf.expectit.matcher.Matchers.contains

// TODO
// 複数コマンドの実装
// sh実行
// LANG=Cに変更
// コマンド実行
// http://d.hatena.ne.jp/n_shuyo/20060714/1152899236
// 接続ユーザは /bin/csh で、LANG=EUC_JPの場合を想定

def ip          = System.getenv("TEST_IP") ?: '192.168.0.13'
def os_user     = System.getenv("TEST_OS_USER") ?: 'someuser'
def os_password = System.getenv("TEST_OS_PASSWORD") ?: 'P@ssw0rd'

def con
def session
def result

con = new Connection(ip, 22)
println "Connect CIMC"
con.connect()
println "Open ssh session"
result = con.authenticateWithPassword(os_user, os_password)
if (!result) {
    println "connect failed"
    return
}
session = login_session(con)
result = run_ssh_command(session, 'df')
println "RESULT:$result"

def login_session(con) {
    try {
        def session = con.openSession()
        session.requestDumbPTY();
        session.startShell();
        def prompts = ["ok" : '% ', "sh" : '$ ']
        Expect expect = new ExpectBuilder()
                .withOutput(session.getStdin())
                .withInputs(session.getStdout(), session.getStderr())
                        .withEchoOutput(System.out)
                        .withEchoInput(System.out)
                .build();
        expect.expect(contains(prompts['ok'])); 
        expect.sendLine("sh");  //  c: Login to  CLI shell
        expect.expect(contains(prompts['sh'])); 
        expect.close()

        return session
    } catch (Exception e) {
        println "[SSH Test] login faild, skip.\n" + e
    }
}

def run_ssh_command(session, command) {
    def ok_prompt = '$ ';
    try {
        Expect expect = new ExpectBuilder()
                .withOutput(session.getStdin())
                .withInputs(session.getStdout(), session.getStderr())
                        // .withEchoOutput(System.out)
                        // .withEchoInput(System.out)
                .build();

        expect.sendLine("LANG=C")
        expect.expect(contains(ok_prompt))
        expect.sendLine(command)
        String result = expect.expect(contains(ok_prompt)).getBefore(); 
        expect.close()
        return result
    } catch (Exception e) {
        println "[SSH Test] Command error '$commands' faild, skip.\n" + e
    }
}

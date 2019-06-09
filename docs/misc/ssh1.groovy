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
import static net.sf.expectit.matcher.Matchers.regexp

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
result = run_ssh_command(session, 'uname -n')
println "RESULT:$result<EOF>"

def command = '''\
|(
|awk \'/^domain/ {print \$2}\' /etc/resolv2.conf 2>/dev/null
|if [ \$? != 0 ]; then
|   echo 'Not Found'
|fi
|)
'''.stripMargin()
result = run_ssh_command(session, command)
println "RESULT:$result<EOF>"
result = run_ssh_command(session, "df")
println "RESULT:$result<EOF>"

def login_session(con) {
    try {
        def session = con.openSession()
        session.requestDumbPTY();
        session.startShell();
        def prompt = '.*[%|$|#|>] $'
        Expect expect = new ExpectBuilder()
                .withOutput(session.getStdin())
                .withInputs(session.getStdout(), session.getStderr())
                        .withEchoOutput(System.out)
                        .withEchoInput(System.out)
                .build();
        // expect.expect(contains(prompts['ok'])); 
        expect.expect(regexp(prompt)); 
        expect.sendLine("sh");  //  c: Login to  CLI shell
        expect.expect(regexp(prompt)); 
        expect.sendLine("LANG=C");  //  c: Login to  CLI shell
        // expect.expect(contains(prompts['sh'])); 
        expect.expect(regexp(prompt)); 
        expect.close()

        return session
    } catch (Exception e) {
        println "[SSH Test] login faild, skip.\n" + e
    }
}

String truncate_first_line(String message) {
    String result = message
    int truncate_size = message.indexOf('\n');
    if (truncate_size > 0) {
        result = message.substring(truncate_size)
    }
    return result
}

def run_ssh_command(session, command) {
    def ok_prompt = '.*[%|$|#|>] ';
    try {
        Expect expect = new ExpectBuilder()
                .withOutput(session.getStdin())
                .withInputs(session.getStdout(), session.getStderr())
                        .withEchoOutput(System.out)
                        // .withEchoInput(System.out)
                .build();

        expect.sendLine("LANG=C")
        println "TEST1"
        expect.expect(regexp(ok_prompt))
        println "TEST2"
        def row = 0
        def lines = command.readLines()
        def max_row = lines.size()
        lines.each { line ->
            println "TEST3:$line"
            expect.sendLine(line)
            println "TEST4:$line"
            if (0 < row && row < max_row) {
                expect.expect(regexp(ok_prompt))
            }
            row ++
        }
        // expect.expect(regexp(ok_prompt))
        // String result = expect.expect(regexp(ok_prompt)).getInput(); 
        def res = expect.expect(regexp(ok_prompt))
        String result = res.getBefore(); 
        // println "RES1:${res.getBefore()}<EOF>"
        // println "RES2:${res.getInput()}<EOF>"
        // println "RES3:${res.toString()}<EOF>"

        expect.close()
        return truncate_first_line(result)
    } catch (Exception e) {
        println "[SSH Test] Command error '$command' faild, skip.\n" + e
    }
}

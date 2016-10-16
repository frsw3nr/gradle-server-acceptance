package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.util.CliBuilder

// gradlew run -Pargs="ls -alt *.groovy"

// gradlew shadowJar
// java -jar build/libs/gradle-server-acceptance-0.1.0-all.jar -a

@Slf4j
class TestRunner {

    final created
    String test_resource
    String config_file
    String sheet_file
    String target_servers
    String test_id
    Boolean dry_run

    EvidenceSheet evidence

    TestRunner() {
        created = new Date().format("yyyyMMdd-HHmmss")
    }

    Boolean readEvidence() {
        //
    }

    Boolean writeEvidence() {
        //
    }

    Boolean runTest(String[] args) {
        def cli = new CliBuilder(usage:'getspec')
        cli.a('all')
        cli.c( longOpt:'config',   args: 1, 'config.groovy');
        cli.e( longOpt:'excel',    args: 1, 'excel spec file');
        cli.s( longOpt:'server',   args: 1, 'keyword of server name');
        cli.t( longOpt:'test',     args: 1, 'keyword of test id');
        cli.t( longOpt:'resource', args: 1, 'test resource dir');
        cli._( longOpt:'dry-run', 'dry run test')
        def options = cli.parse(args)

println args.toString()

        log.info('===========')
        log.info("opt a=" + options.a)
        log.info("opt c=" + options.c)
        cli.usage()
    }

    static void main(String[] args) {
        def test = new TestRunner()
        test.runTest(args)
        // def v=args[0]
        // println System.getenv()[v]
        // println test.created
    }
}

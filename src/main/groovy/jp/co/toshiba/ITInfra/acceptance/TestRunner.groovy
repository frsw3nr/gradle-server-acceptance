package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.util.CliBuilder
import org.apache.commons.cli.Option

// gradlew run -Pargs="ls -alt *.groovy"

// gradlew shadowJar
// java -jar build/libs/gradle-server-acceptance-0.1.0-all.jar -a

@Slf4j
class TestRunner {

    String test_resource
    String config_file
    String sheet_file
    int parallel_degree
    def target_servers
    def test_ids
    Boolean dry_run
    Boolean verify_test

    def parse(String[] args) {
        def cli = new CliBuilder(usage:'getspec')
        cli.with {
            c longOpt:'config',   args: 1, 'Config file'
            e longOpt:'excel',    args: 1, 'Excel spec file'
            s longOpt:'server',   args: Option.UNLIMITED_VALUES, valueSeparator: ',' as char, 'Server list filter'
            t longOpt:'test',     args: Option.UNLIMITED_VALUES, valueSeparator: ',' as char, 'Test id list filter'
            r longOpt:'resource', args: 1, 'Test resource dir'
            p longOpt:'parallel', args: 1, 'Parallel test degree'
            d longOpt:'dry-run', 'Dry run test'
            v longOpt:'verify',  'Enable verify test'
        }
        def options = cli.parse(args)
        if (!options) {
            cli.usage()
            throw new IllegalArgumentException('Parse error')
        }

        def env_test_resource = System.getenv()['TEST_RESOURCE']
        def default_test_resource = (env_test_resource) ?: './src/test/resources/'

        target_servers  = options.ss
        test_ids        = options.ts
        test_resource   = (options.r) ?: default_test_resource
        parallel_degree = (options.p) ?: 1
        dry_run         = options.d ?: false
        verify_test     = options.v ?: false

        config_file = './config/config.groovy'
        if (options.c) {
            config_file = options.c
        } else if (options.r || env_test_resource) {
            config_file = "${test_resource}/config.groovy"
        }

        def config = Config.instance.read(config_file)
        sheet_file = config['evidence']['source'] ?: './check_sheet.xlsx'

        log.info "Parse Arguments : " + args.toString()
        log.info "\ttest_resource : " + test_resource
        log.info "\tconfig_file   : " + config_file
        log.info "\tsheet_file    : " + sheet_file
        log.info "\tdry_run       : " + dry_run
        log.info "\tverify_test   : " + verify_test
        log.info "\tfilter option : "
        log.info "\t\ttarget servers : " + target_servers.toString()
        log.info "\t\ttest_ids       : " + test_ids.toString()

    }

    static void main(String[] args) {
        def test_runner = new TestRunner()
        test_runner.parse(args)
        def test_scheduler = new TestScheduler(test_runner)
        try {
            test_scheduler.runTest()
        } catch (Exception e) {
            log.error "some error " + e
        }
    }
}

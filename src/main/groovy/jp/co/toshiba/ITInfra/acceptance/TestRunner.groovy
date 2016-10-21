package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.lang.math.NumberUtils
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
            c longOpt: 'config',   args: 1, 'Config file path : ./config/config.groovy'
            e longOpt: 'excel',    args: 1, 'Excel test spec file path : check_sheet.xlsx'
            s longOpt: 'server',   args: Option.UNLIMITED_VALUES,
                valueSeparator: ',' as char, 'Filtering list of servers : svr1,svr2,...'
            t longOpt: 'test',     args: Option.UNLIMITED_VALUES,
                valueSeparator: ',' as char, 'Filtering list of test_ids : vm,cpu,...'
            r longOpt: 'resource', args: 1, 'Dry run test resource : ./src/test/resources/log/'
            p longOpt: 'parallel', args: 1, 'Degree of test runner processes'
            d longOpt: 'dry-run', 'Enable Dry run test'
            v longOpt: 'verify',  'Disable verify test'
            h longOpt: 'help',    'Print usage'
        }
        def options = cli.parse(args)
        if (options.h) {
            cli.usage()
            System.exit(0)
        }
        if (!options) {
            cli.usage()
            throw new IllegalArgumentException('Parse error')
        }

        def env_test_resource = System.getenv()['TEST_RESOURCE']
        def default_test_resource = (env_test_resource) ?: './src/test/resources/'

        target_servers = [:]
        if (options.ss) {
            options.ss.each {
                target_servers[it] = true
            }
        }
        test_ids = [:]
        if (options.ts) {
            options.ts.each {
                test_ids[it] = true
            }
        }
        parallel_degree = 1
        dry_run         =  options.d ?: false
        verify_test     = !options.v ?: true

        config_file = './config/config.groovy'
        if (options.c) {
            config_file = options.c
        } else if (options.r || env_test_resource) {
            config_file = "${test_resource}/config.groovy"
        }

        def config = Config.instance.read(config_file)
        sheet_file = config['evidence']['source'] ?: './check_sheet.xlsx'
        if (options.e) {
            sheet_file = options.e
        }

        if (options.p) {
            String degree = options.p
            if (NumberUtils.isDigits(degree)) {
                parallel_degree = NumberUtils.toInt(degree)
            } else {
                log.error "Invarid parallel degree options : -p ${degree}"
                cli.usage()
                System.exit(1)
            }
        }

        test_resource = (options.r) ?: default_test_resource + '/log/'
        config['test']['dry_run_staging_dir'] = test_resource

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
            log.error "Fatal error : " + e
            System.exit(1)
        }
    }
}

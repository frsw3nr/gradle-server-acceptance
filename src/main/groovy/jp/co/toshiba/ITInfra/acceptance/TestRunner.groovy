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

    String getconfig_home
    String test_resource
    String config_file
    String sheet_file
    String export_file
    int parallel_degree
    def target_servers
    def test_ids
    Boolean dry_run
    Boolean verify_test

    def parse(String[] args) {
        getconfig_home = System.getProperty("user.dir")
        def cli = new CliBuilder(usage:'getspec')
        cli.with {
            c longOpt: 'config',   args: 1, 'Config file path : ./config/config.groovy'
            g longOpt: 'generate', args: 1, 'Generate project by specifying a directory'
            x longOpt: 'xport',    args: 1, 'Export project to specifying a file'
            e longOpt: 'excel',    args: 1, 'Excel test spec file path : check_sheet.xlsx'
            s longOpt: 'server',   args: Option.UNLIMITED_VALUES,
                valueSeparator: ',' as char, 'Filtering list of servers : svr1,svr2,...'
            t longOpt: 'test',     args: Option.UNLIMITED_VALUES,
                valueSeparator: ',' as char, 'Filtering list of test_ids : vm,cpu,...'
            u longOpt: 'update',   'Update node config direc from the log : ./build/log,...'
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
        if (options.g) {
            def base_home = System.getProperty("getconfig_home")
            def site_home = options.g
            new ProjectBuilder(base_home, site_home).generate()
            System.exit(0)
        }
        if (options.x) {
            def xport_file = options.x
            new ProjectBuilder(getconfig_home).xport(xport_file)
            System.exit(0)
        }
        if (options.u) {
            new EvidenceFile(getconfig_home).generate()
            System.exit(0)
        }

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

        test_resource = (options.r) ?: config['test']['dry_run_staging_dir'] ?: './src/test/resources/log'
        config['test']['dry_run_staging_dir'] = test_resource
        log.info "Parse Arguments : " + args.toString()
        log.info "\thome          : " + getconfig_home
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
        try {
            def test_runner = new TestRunner()
            test_runner.parse(args)
            def test_scheduler = new TestScheduler(test_runner)
            test_scheduler.runTest()
        } catch (Exception e) {
            log.error "Fatal error : " + e
            System.exit(1)
        }
    }
}

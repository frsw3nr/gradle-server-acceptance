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
    String project_home
    String db_config_file
    String test_resource
    String config_file
    String sheet_file
    String export_file
    String server_config_script
    int parallel_degree
    def target_servers
    def test_ids
    Boolean dry_run
    Boolean verify_test
    EvidenceManager evidence_manager

    def parse(String[] args) {
        getconfig_home = System.getProperty("getconfig_home") ?: '.'
        project_home   = System.getProperty("user.dir")
        db_config_file = new File(getconfig_home, "/config/cmdb.groovy").getAbsolutePath()

        def cli = new CliBuilder(usage:'getconfig -c ./config/config.groovy')
        cli.with {
            c longOpt: 'config',   args: 1, 'Config file path',
                argName: 'config.groovy'
            g longOpt: 'generate', args: 1, 'Generate project directory',
                argName: '/work/project'
            x longOpt: 'xport',    args: 1, 'Export project zip file',
                argName: '/work/project.zip'
            _ longOpt: 'excel',    args: 1, 'Excel sheet path',
                argName: 'check_sheet.xlsx'
            i longOpt: 'input',    args: 1, 'Target server config script',
                argName: 'test_servers.groovy'
            s longOpt: 'server',   args: Option.UNLIMITED_VALUES,
                valueSeparator: ',' as char, 'Filtering list of servers',
                argName: 'svr1,svr2,...'
            t longOpt: 'test',     args: Option.UNLIMITED_VALUES,
                valueSeparator: ',' as char, 'Filtering list of test_ids',
                argName: 'vm,cpu,...'
            u longOpt: 'update',   args: 1, 'Update node config',
                argName:'local|db|db-all'
            r longOpt: 'resource', args: 1, 'Dry run test resource directory'
                argName : './src/test/resources/log/'
            _ longOpt: 'parallel', args: 1, 'Degree of test runner processes'
                argName:'n'
            _ longOpt: 'encode',   args: 1, 'Encode config file',
                argName : 'config.groovy'
            _ longOpt: 'decode',   args: 1, 'Decode config file',
                argName : 'config.groovy-encrypted'
            k longOpt: 'keyword',  args: 1, 'Config file password',
                argName : 'password'
            d longOpt: 'dry-run', 'Enable Dry run test'
            _ longOpt: 'verify',  'Disable verify test'
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
            def site_home = options.g
            new ProjectBuilder(getconfig_home, site_home).generate()
            System.exit(0)
        }
        if (options.x) {
            def xport_file = options.x
            new ProjectBuilder(project_home).xport(xport_file)
            System.exit(0)
        }
        evidence_manager = new EvidenceManager(getconfig_home : getconfig_home,
                                               project_home : project_home,
                                               db_config_file : db_config_file)

        if (options.u) {
            if (options.u == 'local') {
                evidence_manager.exportNodeDirectory()
            } else if (options.u == 'db') {
                evidence_manager.exportCMDB()
            } else if (options.u == 'db-all') {
                evidence_manager.exportCMDBAll()
            } else {
                cli.usage()
                throw new IllegalArgumentException('--update option must be local or db or db-all')
            }
            System.exit(0)
        }
        def keyword = options.k?:null
        if (options.encode) {
            Config.instance.encrypt(options.encode, keyword)
            System.exit(0)
        }
        if (options.decode) {
            Config.instance.decrypt(options.decode, keyword)
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
        verify_test     = !options.verify ?: true

        config_file = './config/config.groovy'
        if (options.c) {
            config_file = options.c
        }

        server_config_script = null
        if (options.i) {
            server_config_script = options.i
        }

        def config = Config.instance.read(config_file, keyword)
        sheet_file = config['evidence']['source'] ?: './check_sheet.xlsx'
        if (options.excel) {
            sheet_file = options.excel
        }

        if (options.parallel) {
            String degree = options.parallel
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
        log.info "\thome          : " + project_home
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
            e.printStackTrace()
            System.exit(1)
        }
    }
}

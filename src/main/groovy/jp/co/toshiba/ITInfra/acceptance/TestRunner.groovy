package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import org.apache.commons.lang.math.NumberUtils
import groovy.util.CliBuilder
import org.apache.commons.cli.Option

// gradle shadowJar
// java -jar build/libs/gradle-server-acceptance-0.1.0-all.jar -a

enum RunnerCommand {
  SCHEDULER, EXPORT, ARCHIVE, GENERATE
}

@Slf4j
@ToString(includePackage = false)
class TestRunner {

    static final String main_version = '0.1.21'
    RunnerCommand command = RunnerCommand.SCHEDULER
    String getconfig_home
    String project_home
    String config_file
    String excel_file
    String output_evidence
    String filter_server
    String filter_metric
    String export_type
    int parallel_degree
    Boolean dry_run
    Boolean verify_test
    Boolean silent

    def get_application_title() {
        return "Getconfig Inventory collector v${main_version}"
    }

    def parse(String[] args) {
        getconfig_home = System.getProperty("getconfig_home") ?: '.'
        project_home   = System.getProperty("user.dir")

        def cli = new CliBuilder(usage:'getconfig -c ./config/config.groovy')
        cli.with {
            c longOpt: 'config',   args: 1, 'Config file path',
                argName: 'config.groovy'
            g longOpt: 'generate', args: 1, 'Generate project directory',
                argName: '/work/project'
            a longOpt: 'archive',   args: 1, 'Archive project zip file',
                argName: '/work/project.zip'
            e longOpt: 'excel',    args: 1, 'Excel sheet path',
                argName: 'check_sheet.xlsx'
            o longOpt: 'output',   args: 1, 'Output evidence path',
                argName: 'build/check_sheet.xlsx'
            s longOpt: 'server',   args: 1, 'Keyword of target server'
            t longOpt: 'test',     args: 1, 'Keyword of test metric'
            u longOpt: 'update',   args: 1, 'Update node config',
                argName:'local|db|db-all'
            _ longOpt: 'parallel', args: 1, 'Degree of test runner processes'
                argName:'n'
            _ longOpt: 'encode',   args: 1, 'Encode config file',
                argName : 'config.groovy'
            _ longOpt: 'decode',   args: 1, 'Decode config file',
                argName : 'config.groovy-encrypted'
            p longOpt: 'password',  args: 1, 'Config file password',
                argName : 'password'
            d longOpt: 'dry-run', 'Enable Dry run test'
            v longOpt: 'verify-disable', 'Disable value verification'
            h longOpt: 'help',    'Print usage'
            _ longOpt: 'silent', 'Silent mode'
        }
        def options = cli.parse(args)
        if (!options) {
            System.exit(1)
        }
        if (options?.h) {
            println get_application_title()
            cli.usage()
            System.exit(0)
        }
        if (options.g) {
            def site_home = options.g
            new ProjectBuilder(getconfig_home, site_home).generate()
            System.exit(0)
        }
        if (options.u) {
            this.export_type = options.u
            this.command = RunnerCommand.EXPORT
        }
        if (options.archive) {
            def archive_file = options.archive
            new ProjectBuilder(project_home).xport(archive_file)
            System.exit(0)
        }

        def password = options.p ?: null
        if (options.encode) {
            Config.instance.encrypt(options.encode, password)
            System.exit(0)
        }
        if (options.decode) {
            Config.instance.decrypt(options.decode, password)
            System.exit(0)
        }

        if (options.s)
            this.filter_server = options.s

        if (options.t)
            this.filter_metric = options.t

        if (options.d)
            this.dry_run       = options.d

        if (options.v)
            this.verify_test   = !options.v

        if (options.silent)
            this.silent        = options.silent

        config_file = './config/config.groovy'
        if (options.c) {
            config_file = options.c
        }

        if (options.excel) {
            this.excel_file = options.excel
        }

        if (options.output) {
            this.output_evidence = options.output
        }

        this.parallel_degree = 0
        if (options.parallel) {
            String degree = options.parallel
            if (NumberUtils.isDigits(degree)) {
                this.parallel_degree = NumberUtils.toInt(degree)
            } else {
                log.error "Invarid parallel degree options : -p ${degree}"
                cli.usage()
                System.exit(1)
            }
        }

        // log.info "Parse Arguments : " + args.toString()
        // log.info "\thome          : " + this.project_home
        // // log.info "\ttest_resource : " + this.test_resource
        // log.info "\tconfig_file   : " + this.config_file
        // log.info "\tsheet_file    : " + this.sheet_file
        // log.info "\tdry_run       : " + this.dry_run
        // log.info "\tverify_test   : " + this.verify_test
        // log.info "\tuse_redmine   : " + this.use_redmine
        // log.info "\tfilter option : "
        // log.info "\t\ttarget servers : " + this.filter_server
        // log.info "\t\tmetrics        : " + this.filter_metric
    }

    static void main(String[] args) {
        long start = System.currentTimeMillis()
        def test_runner = new TestRunner()
        def test_env = ConfigTestEnvironment.instance
        try {
            test_runner.parse(args)
            test_env.read_from_test_runner(test_runner)
        } catch (Exception e) {
            log.error "Fatal error : " + e
            System.exit(1)
        }
        if (test_runner.command == RunnerCommand.SCHEDULER) {
            def test_scheduler = new TestScheduler()
            test_env.accept(test_scheduler)
            try {
                test_scheduler.init()
                test_scheduler.run()
                test_scheduler.finish()
            } catch (Exception e) {
                log.error "Fatal error : " + e
                 e.printStackTrace()
            }
        } else if (test_runner.command == RunnerCommand.EXPORT) {
            def evidence_manager = new EvidenceManager()
            test_env.get_cmdb_config()
            test_env.accept(evidence_manager)
            try {
                if (test_runner.export_type =~ /db/) {
                    test_env.accept(CMDBModel.instance)
                }
                evidence_manager.update(test_runner.export_type)
            } catch (Exception e) {
                log.error "Fatal error : " + e
                System.exit(1)
            }
        }
        long elapsed = System.currentTimeMillis() - start
        log.info "Total, Elapsed : ${elapsed} ms"
    }

    // String getconfig_home
    // String project_home
    // // String db_config_file
    // // String test_resource
    // String config_file
    // String sheet_file
    // String export_files
    // // String server_config_script
    // int parallel_degree
    // String filter_server
    // String filter_metric
    // // def target_servers
    // // def test_ids
    // Boolean dry_run
    // Boolean verify_test
    // Boolean use_redmine
    // Boolean silent
    // // EvidenceManager evidence_manager

    // def parse(String[] args) {
    //     getconfig_home = System.getProperty("getconfig_home") ?: '.'
    //     project_home   = System.getProperty("user.dir")
    //     // db_config_file = new File(getconfig_home, "/config/cmdb.groovy").getAbsolutePath()

    //     def cli = new CliBuilder(usage:'getconfig -c ./config/config.groovy')
    //     cli.with {
    //         c longOpt: 'config',   args: 1, 'Config file path',
    //             argName: 'config.groovy'
    //         g longOpt: 'generate', args: 1, 'Generate project directory',
    //             argName: '/work/project'
    //         a longOpt: 'archive',   args: 1, 'Archive project zip file',
    //             argName: '/work/project.zip'
    //         e longOpt: 'excel',    args: 1, 'Excel sheet path',
    //             argName: 'check_sheet.xlsx'
    //         // i longOpt: 'input',    args: 1, 'Target server config script',
    //         //     argName: 'test_servers.groovy'
    //         s longOpt: 'server',   args: 1, 'Keyword of target server'
    //         t longOpt: 'test',     args: 1, 'Keyword of test metric'
    //         u longOpt: 'update',   args: 1, 'Update node config',
    //             argName:'local|db|db-all'
    //         // _ longOpt: 'resource', args: 1, 'Dry run test resource directory'
    //         //     argName : './src/test/resources/log/'
    //         _ longOpt: 'parallel', args: 1, 'Degree of test runner processes'
    //             argName:'n'
    //         _ longOpt: 'encode',   args: 1, 'Encode config file',
    //             argName : 'config.groovy'
    //         _ longOpt: 'decode',   args: 1, 'Decode config file',
    //             argName : 'config.groovy-encrypted'
    //         p longOpt: 'password',  args: 1, 'Config file password',
    //             argName : 'password'
    //         d longOpt: 'dry-run', 'Enable Dry run test'
    //         x longOpt: 'exclude-verify',  'Exclude value verification'
    //         h longOpt: 'help',    'Print usage'
    //         // x longOpt: 'export',   args: 1, 'Export csv from test result excel',
    //         //     argName : 'check_sheet.xlsx,...'
    //         // r longOpt: 'use-redmine', 'Get test targets from Redmine'
    //         _ longOpt: 'silent', 'Silent mode'
    //     }
    //     def options = cli.parse(args)
    //     if (options.h || ! options.arguments().isEmpty()) {
    //         cli.usage()
    //         System.exit(0)
    //     }
    //     if (!options) {
    //         cli.usage()
    //         throw new IllegalArgumentException('Parse error')
    //     }
    //     if (options.g) {
    //         def site_home = options.g
    //         new ProjectBuilder(getconfig_home, site_home).generate()
    //         System.exit(0)
    //     }
    //     if (options.backup) {
    //         def xport_file = options.backup
    //         new ProjectBuilder(project_home).xport(xport_file)
    //         System.exit(0)
    //     }

    //     def password = options.p?:null
    //     if (options.encode) {
    //         Config.instance.encrypt(options.encode, password)
    //         System.exit(0)
    //     }
    //     if (options.decode) {
    //         Config.instance.decrypt(options.decode, password)
    //         System.exit(0)
    //     }

    //     if (options.s)
    //         this.filter_server = options.s

    //     if (options.t)
    //         this.filter_metric = options.t

    //     if (options.d)
    //         this.dry_run       = options.d

    //     if (options.x)
    //         this.verify_test   = !options.x

    //     if (options.silent)
    //         this.silent        = options.silent

    //     config_file = './config/config.groovy'
    //     if (options.c) {
    //         config_file = options.c
    //     }

    //     // server_config_script = null
    //     // if (options.i) {
    //     //     server_config_script = options.i
    //     // }

    //     // def config = Config.instance.read(config_file, keyword)
    //     // test_resource = (options.resource) ?: config['test']['dry_run_staging_dir'] ?: './src/test/resources/log'
    //     // config['test']['dry_run_staging_dir'] = test_resource
    //     // evidence_manager = new EvidenceManager(getconfig_home : getconfig_home,
    //     //                                        project_home : project_home,
    //     //                                        db_config_file : db_config_file,
    //     //                                        test_resource: test_resource,
    //     //                                        silent: silent)

    //     // if (options.u) {
    //     //     if (options.u == 'local') {
    //     //         evidence_manager.exportNodeDirectory()
    //     //     } else if (options.u == 'db') {
    //     //         evidence_manager.exportCMDB()
    //     //     } else if (options.u == 'db-all') {
    //     //         evidence_manager.exportCMDBAll()
    //     //     } else {
    //     //         cli.usage()
    //     //         throw new IllegalArgumentException('--update option must be local or db or db-all')
    //     //     }
    //     //     System.exit(0)
    //     // }

    //     // if (options.export) {
    //     //     this.export_files = options.export
    //     //     new CSVExporter(this).run()
    //     //     System.exit(0)
    //     // }

    //     // sheet_file = config['evidence']['source'] ?: './check_sheet.xlsx'
    //     this.sheet_file = './check_sheet.xlsx'
    //     if (options.excel) {
    //         this.sheet_file = options.excel
    //     }

    //     this.parallel_degree = 1
    //     if (options.parallel) {
    //         String degree = options.parallel
    //         if (NumberUtils.isDigits(degree)) {
    //             this.parallel_degree = NumberUtils.toInt(degree)
    //         } else {
    //             log.error "Invarid parallel degree options : -p ${degree}"
    //             cli.usage()
    //             System.exit(1)
    //         }
    //     }

    //     log.info "Parse Arguments : " + args.toString()
    //     log.info "\thome          : " + this.project_home
    //     // log.info "\ttest_resource : " + this.test_resource
    //     log.info "\tconfig_file   : " + this.config_file
    //     log.info "\tsheet_file    : " + this.sheet_file
    //     log.info "\tdry_run       : " + this.dry_run
    //     log.info "\tverify_test   : " + this.verify_test
    //     log.info "\tuse_redmine   : " + this.use_redmine
    //     log.info "\tfilter option : "
    //     log.info "\t\ttarget servers : " + this.filter_server
    //     log.info "\t\tmetrics        : " + this.filter_metric
    // }

}

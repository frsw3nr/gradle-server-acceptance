package jp.co.toshiba.ITInfra.acceptance

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.apache.commons.lang.math.NumberUtils

// import jp.co.toshiba.ITInfra.acceptance.*
// import jp.co.toshiba.ITInfra.acceptance.TicketRegistor
// import jp.co.toshiba.ITInfra.acceptance.TicketRegistor
// gradle shadowJar
// java -jar build/libs/gradle-server-acceptance-0.1.0-all.jar -a

enum RunnerCommand {
  SCHEDULER, EXPORT, ARCHIVE, GENERATE, REGIST_REDMINE, LIST_NODE
}

@Slf4j
@ToString(includePackage = false)
class TestRunner {

    static final String main_version = '0.2.2'
    RunnerCommand command = RunnerCommand.SCHEDULER
    String getconfig_home
    String project_home
    String config_file
    String excel_file
    String output_evidence
    String filter_server
    String filter_metric
    String export_type
    String list_filter_server
    String list_filter_platform
    String redmine_project_name
    int parallel_degree
    int snapshot_level
    int cluster_size
    Boolean dry_run
    Boolean auto_tag
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
            gd longOpt: 'generate-dryrun', args: 1, 'Generate project directory with test',
                argName: '/work/project'
            a longOpt: 'archive',   args: 1, 'Archive project zip file',
                argName: '/work/project.zip'
            at longOpt: 'auto-tag', 'Auto tag generation'
            atn longOpt: 'auto-tag-number', args: 1, 'Auto tag generation specified cluster size'
            e longOpt: 'excel',    args: 1, 'Excel sheet path',
                argName: 'check_sheet.xlsx'
            o longOpt: 'output',   args: 1, 'Output evidence path',
                argName: 'build/check_sheet.xlsx'
            _ longOpt: 'level', args: 1, 'Level of the test item to filter',
                argName: 'level'
            r longOpt: 'redmine', 'Regist redmine ticket'
            rp longOpt: 'redmine-project', args: 1, 'Regist redmine ticket specified project'
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
            lp longOpt: 'list-platform',   args: 1, type: GString,
                argName: 'platform' , 'Print an inventory list of the keyword\'s platform'
            ln longOpt: 'list-node',   args: 1, type: GString,
                argName: 'node' , 'Print an inventory list of the keyword\'s node'
            l longOpt: 'list',   'Print all inventory list'
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
        if (options.gd) {
            def site_home = options.gd
            new ProjectBuilder(getconfig_home, site_home).generate('detail')
            System.exit(0)
        }
        if (options.u) {
            this.export_type = options.u
            this.command = RunnerCommand.EXPORT
        }
        if (options.r || options.rp) {
            if (options.rp) {
                this.redmine_project_name = options.rp
            }
            this.command = RunnerCommand.REGIST_REDMINE
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

        if (options.at)
            this.auto_tag      = options.at

        if (options.atn) {
            this.auto_tag      = true
            String degree = options.atn
            if (NumberUtils.isDigits(degree)) {
                this.cluster_size = NumberUtils.toInt(degree)
            } else {
                log.error "Invarid auto tag cluser size : -atn ${degree}"
                cli.usage()
                System.exit(1)
            }
        }

        if (options.l || options.lp || options.ln) {
            this.command = RunnerCommand.LIST_NODE
            if (options.lp)
                this.list_filter_platform = options.lp

            if (options.ln)
                this.list_filter_server = options.ln
        }

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

        this.snapshot_level = -1
        if (options.'snapshot-level') {
            String level = options.'snapshot-level'
            if (NumberUtils.isDigits(level)) {
                this.snapshot_level = NumberUtils.toInt(level)
                if (this.snapshot_level < 0) {
                    log.error "Invarid level 'level < 0' : -l ${level}"
                    System.exit(1)
                }
            } else {
                log.error "Invarid parallel level options : -l ${level}"
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
        // log.info "\t\tsnapshot_level : ${this.snapshot_level}"
    }

    static void main(String[] args) {
        long start = System.currentTimeMillis()
        def exit_code = 0
        def test_runner = new TestRunner()
        def test_env = ConfigTestEnvironment.instance
        test_runner.parse(args)
        test_env.read_from_test_runner(test_runner)
        if (test_runner.command == RunnerCommand.SCHEDULER) {
            def test_scheduler = new TestScheduler()
            test_env.accept(test_scheduler)
            try {
                test_scheduler.init()
                test_scheduler.run()
                exit_code = test_scheduler.finish()
            } catch (Exception e) {
                log.error "Fatal error : " + e
                 e.printStackTrace()
                System.exit(1)
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
                 e.printStackTrace()
                System.exit(1)
            }
        } else if (test_runner.command == RunnerCommand.REGIST_REDMINE) {
            def ticket_registor = new TicketRegistor()
            test_env.get_cmdb_config()
            test_env.accept(ticket_registor)
            try {
                ticket_registor.run()
            } catch (Exception e) {
                log.error "Fatal error : " + e
                 e.printStackTrace()
                System.exit(1)
            }
        } else if (test_runner.command == RunnerCommand.LIST_NODE) {
            def inventory_db = InventoryDB.instance
            test_env.get_cmdb_config()
            test_env.accept(inventory_db)
            try {
                inventory_db.export(test_runner.list_filter_server, 
                                    test_runner.list_filter_platform)
            } catch (Exception e) {
                log.error "Fatal error : " + e
                 e.printStackTrace()
                System.exit(1)
            }
        }
        long elapsed = System.currentTimeMillis() - start
        log.info "Total, Elapsed : ${elapsed} ms"
        System.exit(exit_code)
    }
}

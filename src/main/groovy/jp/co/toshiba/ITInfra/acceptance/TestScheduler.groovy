package jp.co.toshiba.ITInfra.acceptance


import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.RunStatus
import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import jsr166y.ForkJoinPool

@Slf4j
class TestScheduler {

    ExcelParser excel_parser
    TestScenario test_scenario
    PlatformTester platform_tester
    String excel_file
    String output_evidence
    String filter_server
    String filter_metric
    String base_node_dir
    String project_node_dir
    String current_node_dir
    String node_dir
    Boolean verify_test
    Boolean auto_tag
    def serialize_platforms = [:]
    def sheet_prefixes = [:]
    int parallel_degree = 0
    int snapshot_level = -1
    int exit_code = 0
    def test_platform_tasks = [:].withDefault{[:]}

    def set_environment(ConfigTestEnvironment env) {
        this.excel_file       = env.get_excel_file()
        this.output_evidence  = env.get_output_evidence()

        // this.base_node_dir    = env.get_base_node_dir()
        // this.project_node_dir = env.get_project_node_dir()
        // this.current_node_dir = env.get_current_node_dir()
        // this.node_dir         = env.get_node_dir()

        this.base_node_dir    = TestLog.getNodeDir(LogStage.BASE)
        this.project_node_dir = TestLog.getNodeDir(LogStage.PROJECT)
        this.current_node_dir = TestLog.getNodeDir(LogStage.CURRENT)
        this.node_dir         = TestLog.getNodeDir(LogStage.PROJECT)

        this.filter_server    = env.get_filter_server()
        this.filter_metric    = env.get_filter_metric()
        this.parallel_degree  = env.get_parallel_degree()
        this.snapshot_level   = env.get_snapshot_level()
        this.verify_test      = env.get_verify_test()
        this.auto_tag         = env.get_auto_tag()
        this.sheet_prefixes   = env.get_sheet_prefixes()
    }

    def init() {
        this.excel_parser = new ExcelParser(this.excel_file, this.sheet_prefixes)
        this.excel_parser.scan_sheet()

        this.test_scenario = new TestScenario(name: 'root')
        this.test_scenario.accept(this.excel_parser)

        try {
            def inventory_db = InventoryDB.instance
            ConfigTestEnvironment.instance.accept(inventory_db)
            inventory_db.copy_compare_target_inventory_data(this.test_scenario)

            // def base_result_reader = new TestResultReader('node_dir': this.base_node_dir)
            // base_result_reader.read_compare_target_result(this.test_scenario)
            def project_result_reader = new TestResultReader('node_dir': this.project_node_dir)
            project_result_reader.read_compare_target_result(this.test_scenario)
        } catch (Exception e) {
            log.warn "Faild read test results : " + e
        }
    }

    def run() {
        this.test_scenario.accept(this)
    }

    def finish() {
        def test_config = ConfigTestEnvironment.instance
        def tag_generator = (this.auto_tag) ? new TagGenerator() : new TagGeneratorManual()
        test_config.accept(tag_generator)
        this.test_scenario.accept(tag_generator)
        def data_comparator = new DataComparator()
        this.test_scenario.accept(data_comparator)
        def evidence_maker = new EvidenceMaker()
        this.test_scenario.accept(evidence_maker)
        def report_maker = new ReportMaker()
        test_config.accept(report_maker)
        this.test_scenario.accept(report_maker)
        def excel_sheet_maker = new ExcelSheetMaker(
                                    excel_parser: this.excel_parser,
                                    evidence_maker: evidence_maker,
                                    report_maker: report_maker)
        excel_sheet_maker.output(this.output_evidence)
        def test_result_writer = new TestResultWriter('node_dir': this.current_node_dir)
        this.test_scenario.accept(test_result_writer)

        return this.test_scenario.exit_code
    }

    def make_test_platform_tasks(test_scenario) {
        def domain_metrics = test_scenario.test_metrics.get_all()
        def targets = test_scenario.test_targets.search_all(this.filter_server)
        // def rules = test_scenario.test_rules.get_all()

        targets.find { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                if (test_target.target_status == RunStatus.INIT)
                    return
                def platform_metrics = domain_metrics[domain].get_all()
                platform_metrics.each { platform, platform_metric ->
                    def metrics = platform_metric.search_all(this.filter_metric, this.snapshot_level)
                    if (metrics.size() == 0)
                        return
                    // def test_rule = rules[test_target.verify_id]
                    def test_platform = new TestPlatform(name         : platform,
                                                         test_target  : test_target,
                                                         test_metrics : metrics)
                    test_target.test_platforms[platform] = test_platform
                    test_target.target_status = RunStatus.RUN
                    this.test_platform_tasks[platform][target_name] = test_platform
                }
            }
            return
        }
        return this.test_platform_tasks
    }

    def visit_test_scenario(test_scenario) {
        def test_platform_tasks = this.make_test_platform_tasks(test_scenario)
        test_platform_tasks.each { platform, test_platforms ->
            def n_test_platforms = test_platforms.size()
            log.info "Start platform test(${platform}) : ${n_test_platforms} targets"
            if (this.parallel_degree > 1 && !this.serialize_platforms[platform]) {
                log.info "Parallel execute : ${this.parallel_degree}"
                GParsPool.withPool(this.parallel_degree) { ForkJoinPool pool ->
                    test_platforms.collectParallel { target_name, test_platform ->
                        test_platform.accept(this)
                    }
                }
            } else {
                test_platforms.each { target_name, test_platform ->
                    test_platform.accept(this)
                }
            }
        }
    }

    def visit_test_platform(test_platform) {
        def target_name = test_platform.test_target.name
        def test_label = "${test_platform.name}:${target_name}"
        long start = System.currentTimeMillis()
        log.info "Test $test_label"
        def platform_tester = new PlatformTester(test_platform : test_platform)
        platform_tester.init()
        platform_tester.run()
        def counts = test_platform.count_test_result_status()
        long elapse = System.currentTimeMillis() - start
        log.info "Finish test ${test_label}, ${counts}, Elapse : ${elapse} ms"
        def test_results = test_platform.test_results
    }
}

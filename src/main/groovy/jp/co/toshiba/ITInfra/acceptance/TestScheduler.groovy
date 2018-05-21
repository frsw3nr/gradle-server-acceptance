package jp.co.toshiba.ITInfra.acceptance

import groovy.json.*
import static groovy.json.JsonOutput.*
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import jsr166y.ForkJoinPool
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
class TestScheduler {

    ExcelParser excel_parser
    TestScenario test_scenario
    PlatformTester platform_tester
    String excel_file
    String output_evidence
    String filter_server
    String filter_metric
    Boolean verify_test
    def serialize_platforms = [:]
    int parallel_degree = 0
    def test_platform_tasks = [:].withDefault{[:]}

    def init() {
        this.excel_parser = new ExcelParser(this.excel_file)
        this.excel_parser.scan_sheet()
        this.test_scenario = new TestScenario(name: 'root')
        this.test_scenario.accept(this.excel_parser)
    }

    def run() {
        this.test_scenario.accept(this)
    }

    def finish() {
        def evidence_maker = new EvidenceMaker()
        this.test_scenario.accept(evidence_maker)
        def excel_sheet_maker = new ExcelSheetMaker(
                                    excel_parser: this.excel_parser,
                                    evidence_maker: evidence_maker)
        excel_sheet_maker.output(this.output_evidence)
    }

    def make_test_platform_tasks(test_scenario) {
        def domain_metrics = test_scenario.test_metrics.get_all()
        def targets = test_scenario.test_targets.search_all(this.filter_server)
        def rules = test_scenario.test_rules.get_all()

        targets.find { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                def platform_metrics = domain_metrics[domain].get_all()
                platform_metrics.each { platform, platform_metric ->
                    println "TEST:$domain, $target_name, $platform"
                    def metrics = platform_metric.search_all(this.filter_metric)
                    if (metrics.size() == 0)
                        return
                    def test_rule = rules[test_target.verify_id]
                    def test_platform = new TestPlatform(name         : platform,
                                                         test_target  : test_target,
                                                         test_metrics : metrics,
                                                         test_rule    : test_rule,
                                                         dry_run      : true)
                    test_target.test_platforms[platform] = test_platform
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
            log.info "Prepare platform test(${platform}) : ${n_test_platforms} targets"
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
        def test_label = "${test_platform.name}(${target_name})"
        long start = System.currentTimeMillis()
        // log.info "Start  test $test_label"
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: './src/test/resources/config.groovy')
        platform_tester.init()
        platform_tester.run()

        def counts = test_platform.count_test_result_status()
        long elapse = System.currentTimeMillis() - start
        log.info "Finish test ${test_label}, ${counts}, Elapse : ${elapse} ms"

        def test_results = test_platform.test_results
    }
}

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

    TestRunner test_runner
    TestScenario test_scenario
    PlatformTester platform_tester
    String filter_server
    String filter_metric
    Boolean verify_test
    def serialize_platforms = [:]
    int parallel_degree = 0
    def test_platform_tasks = [:].withDefault{[:]}

    def init() {
        def config = Config.instance.read(this.test_runner.config_file)
        println config
        // evidence:[source:./src/test/resources/check_sheet.xlsx
        def excel = config?.evidence?.source
        // def excel_parser = new ExcelParser(config?.evidence?.sourc)
        def excel_parser = new ExcelParser(config?.evidence?.source)
        excel_parser.scan_sheet()
        this.test_scenario = new TestScenario(name: 'root')
        this.test_scenario.accept(excel_parser)
    }

    def make_test_platform_tasks(test_scenario) {
        def domain_metrics = test_scenario.test_metrics.get_all()
        def targets = test_scenario.test_targets.search_all(this.filter_server)
        def rules = test_scenario.test_rules.get_all()

        targets.find { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                def platform_metrics = domain_metrics[domain].get_all()
                platform_metrics.each { platform, platform_metric ->
                    def metrics = platform_metric.search_all(this.filter_metric)
                    if (metrics.size() == 0)
                        return
                    def test_rule = rules[test_target.verify_id]
                    def test_platform = new TestPlatform(name: platform,
                                                         test_target: test_target,
                                                         test_metrics: metrics,
                                                         test_rule: test_rule)
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
        log.info "visit_test_platform : ${test_platform.name}"
        this.platform_tester.run(test_platform)
        // def test_domain_template = test_domain_templates[]
    }
}

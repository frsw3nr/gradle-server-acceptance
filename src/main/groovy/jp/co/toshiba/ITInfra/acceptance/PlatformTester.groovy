package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
class PlatformTester {

    String name
    static final user_lib = './lib'
    static final user_package = 'InfraTestSpec'
    private test_spec

    TestRunner   test_runner
    TestPlatform test_platform
    TestItem[]   test_items
    String config_file
    def server_info = [:]

    def init_test_script() {
        def loader = new GroovyClassLoader()
        loader.addClasspath(user_lib)
        loader.clearCache()

        def user_script = "${user_lib}/${user_package}/${test_platform.name}Spec.groovy"
        log.debug "Load ${user_script}"
        def clazz = loader.parseClass(new File(user_script))
        test_spec = clazz.newInstance(this.test_platform)
    }

    def init() {
        def test_env = ConfigTestEnvironment.instance
        // println "CONFIG: ${test_env.config_file}, ${this.config_file}"
        // if (!test_env.config_file)
        //     test_env.read_config(this.config_file)
        test_env.set_account(this.test_platform)
        test_env.accept(this.test_platform)
        this.init_test_script()
        def metric_names = this.test_platform.test_metrics.keySet() as String[]
        this.set_test_items(metric_names)
    }

    def set_test_items(String[] metric_names = null) {
        def test_items = []
        def server_info = this.test_platform?.test_target?.asMap()

        metric_names.each { metric_name ->
            def test_item = new TestItem(platform: this.test_platform?.name,
                                         test_id : metric_name,
                                         server_info : server_info,
                                         test_results : this.test_platform?.test_results)
            test_items << test_item
        }
        this.test_items = test_items
    }

    def run() {
        test_spec.init()
        try {
            test_spec.setup_exec(test_items)
            // log.debug "\tresults : " + summaryReport(test_items)
        } catch (Exception e) {
            log.error "[Test] Failed to run ${test_spec.title}, skip.\n" + e
        }
        test_spec.cleanup_exec()

        // test_platform.test_metrics.each { metric_name, metric ->
        //     println "METRIC: ${metric_name}"
        // }
    }

    // PlatformTestRunner(TargetServer test_server, String platform) throws IOException {
    //     this.test_server = test_server
    //     this.verify_id   = test_server.verify_id
    //     this.platform      = platform
    //     this.server_info = test_server.infos

    //     def loader = new GroovyClassLoader()
    //     loader.addClasspath(user_lib)
    //     loader.clearCache()

    //     def user_script = "${user_lib}/${user_package}/${platform}Spec.groovy"
    //     log.debug "Load ${user_script}"
    //     def clazz = loader.parseClass(new File(user_script))
    //     test_spec = clazz.newInstance(test_server, platform)
    // }

    // def summaryReport(TestItem[] test_items) {
    //     def ok = 0
    //     def test_count = 0
    //     test_items.each {
    //         if (it.succeed == 1)
    //             ok ++
    //         test_count += it.results.size()
    //     }
    //     def ng = test_items.size() - ok

    //     return "OK : ${ok}, NG : ${ng}, Test: ${test_count}"
    // }

    // def run(TestItem[] test_items) {
    //     test_spec.init()
    //     try {
    //         test_spec.setup_exec(test_items)
    //         log.debug "\tresults : " + summaryReport(test_items)
    //     } catch (Exception e) {
    //         log.error "[Test] Failed to run ${test_spec.title}, skip.\n" + e
    //     }
    //     test_spec.cleanup_exec()
    // }

    // def makeTest(List test_ids) {
    //     test_ids.each {
    //         result_test_items.add(new TestItem(it))
    //     }
    //     def test_items = result_test_items as TestItem[]
    //     run(test_items)
    //     def test_results = [:]
    //     test_items.each {
    //         test_results << it.results
    //     }
    //     return test_results
    // }

    // def verify() {
    //     def verifier = VerifyRuleGenerator.instance
    //     def statuses = [:]
    //     result_test_items.each { test_item ->
    //         test_item.with {
    //             results.each { id, test_value ->
    //                 def status = verifier.verify(verify_id, domain, id, test_value, server_info)
    //                 if (status != null) {
    //                     verify_status[id] = status
    //                     statuses[id] = status
    //                 }
    //             }
    //         }
    //     }
    //     return statuses
    // }

    // def getVerifyStatuses() {
    //     def statuses = [:]
    //     result_test_items.each { test_item ->
    //         statuses << test_item.verify_status
    //     }
    //     return statuses
    // }

    // def getResults() {
    //     def results = [:]
    //     result_test_items.each { test_item ->
    //         results << test_item.results
    //     }
    //     return results
    // }

    // def getAdditionalTestItems() {
    //     def additional_test_items = [:]
    //     result_test_items.each { test_item ->
    //         additional_test_items << test_item.additional_test_items
    //     }
    //     return additional_test_items
    // }

}

package jp.co.toshiba.ITInfra.acceptance

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.Model.RunStatus
import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform

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
        def code = new File(user_script).getText('UTF-8')
        def clazz = loader.parseClass(code)
        test_spec = clazz.newInstance(this.test_platform)
    }

    def init() {
        def test_env = ConfigTestEnvironment.instance
        if (!test_env.config && this.config_file)
            test_env.read_config(this.config_file)
        test_env.set_account(this.test_platform)
        test_env.accept(this.test_platform)
        this.init_test_script()
        def metric_names = this.test_platform.test_metrics.keySet() as String[]
        this.set_test_items(metric_names)
        this.test_platform.platform_status = RunStatus.READY
    }

    def set_test_items(String[] metric_names = null) {
        def test_items = []
        def server_info = this.test_platform?.test_target?.asMap()

        metric_names.each { metric_name ->
            def test_item = new TestItem(platform: this.test_platform?.name,
                                         verify_test : this.test_platform?.verify_test,
                                         test_id : metric_name,
                                         server_info : server_info,
                                         test_results : this.test_platform?.test_results,
                                         port_lists : this.test_platform?.port_lists)
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
            this.test_platform.platform_status = RunStatus.ERROR
            log.error "[Test] Failed to run ${test_spec.title}, skip.\n" + e
            // e.printStackTrace()
        }
        test_spec.cleanup_exec()
        this.test_platform.platform_status = RunStatus.FINISH
    }

}

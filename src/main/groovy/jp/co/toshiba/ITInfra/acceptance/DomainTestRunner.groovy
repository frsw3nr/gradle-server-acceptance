package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString

// ToDo

// 定数の定義、user_lib, user_package
// DomainTestRunner に変更
// run(test_id) 実装
// run(TestSpec[]) 実装

@Slf4j
class DomainTestRunner {

    static final user_lib = './lib'
    static final user_package = 'InfraTestSpec'

    TargetServer test_server
    String     domain
    TestItem[] results

    private spec

    DomainTestRunner(TargetServer test_server, String domain) {
        this.test_server = test_server
        this.domain      = domain

        def loader = new GroovyClassLoader()
        loader.addClasspath(user_lib)
        loader.clearCache()

        def user_script = "${user_lib}/${user_package}/${domain}Spec.groovy"
        def clazz = loader.parseClass(new File(user_script))
        spec = clazz.newInstance(test_server, domain)
    }

    def summaryReport(TestItem[] test_items) {
        def ok = 0
        def test_count = 0
        test_items.each {
            if (it.succeed == 1)
                ok ++
            test_count += it.results.size()
        }
        def ng = test_items.size() - ok

        return "OK : ${ok}, NG : ${ng}, Test: ${test_count}"
    }

    def run(TestItem[] test_items) {
        spec.init()
        spec.setup_exec(test_items)
        log.info "\tresults : " + summaryReport(test_items)
        spec.cleanup_exec()
    }

    // def run(TestItem test_item) {
    //     spec.init()
    //     spec.setup_exec(test_item)
    // }
}

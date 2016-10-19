package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString

// ToDo

// 定数の定義、user_lib, user_package
// DomainTestRunner に変更
// run(test_id) 実装
// run(Testtest_Spec[]) 実装

@Slf4j
class DomainTestRunner {

    static final user_lib = './lib'
    static final user_package = 'InfraTestSpec'

    TargetServer test_server
    String     domain
    String     verify_id
    def result_test_items = []
    private test_spec

    DomainTestRunner(TargetServer test_server, String domain) {
        this.test_server = test_server
        this.verify_id   = test_server.verify_id
        this.domain      = domain

        def loader = new GroovyClassLoader()
        loader.addClasspath(user_lib)
        loader.clearCache()

        def user_script = "${user_lib}/${user_package}/${domain}Spec.groovy"
        def clazz = loader.parseClass(new File(user_script))
        test_spec = clazz.newInstance(test_server, domain)

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
        test_spec.init()
        test_spec.setup_exec(test_items)
        log.info "\tresults : " + summaryReport(test_items)
        test_spec.cleanup_exec()
    }

    def makeTest(List test_ids) {
        test_ids.each {
            result_test_items.add(new TestItem(it))
        }
        def test_items = result_test_items as TestItem[]
        run(test_items)
        def test_results = [:]
        test_items.each {
            test_results << it.results
        }
        return test_results
    }

    def setDeviceResults(Map device_results) {
        def server_name = test_server.server_name
        result_test_items.each { test_item ->
            if (test_item.devices.size() > 0) {
                device_results[test_item.test_id][server_name] = test_item.devices
            }
        }
    }

    def verifyResults(VerifyRuleGenerator verify_rule) {
        def rule = verify_rule.generate_instance()
println verify_rule.generate_code()
        def verify_results = [:]
        result_test_items.each { test_item ->
println test_item.test_id
            test_item.results.each { test_id, test_value ->
println test_id
                def verify_func = "${verify_id}__${domain}__${test_id}"
                log.info "Evaluate '${verify_func}'"
                def method = rule.metaClass.getMetaMethod(verify_func, Object)
                if (method) {
                    try {
                        def verify_result = method.invoke(rule, test_value)
                        log.info "Verify_rule ${method.name}(${test_value}) = ${verify_result}"
                        test_item.verify_statuses[test_id] = verify_result
                    } catch (Exception e) {
                        log.error "[Verify] Failed to evaluate rule '${test_id}' : " + e
                    }
                }
            }
            verify_results << test_item.verify_statuses
        }
        return verify_results
    }
}

package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString

@Slf4j
class DomainTestRunner {

    static final user_lib = './lib'
    static final user_package = 'InfraTestSpec'

    TargetServer test_server
    String     domain
    String     verify_id
    def result_test_items = []
    private test_spec
    def server_info = [:]

    DomainTestRunner(TargetServer test_server, String domain) throws IOException {
        this.test_server = test_server
        this.verify_id   = test_server.verify_id
        this.domain      = domain
        this.server_info = test_server.infos

        def loader = new GroovyClassLoader()
        loader.addClasspath(user_lib)
        loader.clearCache()

        def user_script = "${user_lib}/${user_package}/${domain}Spec.groovy"
        log.debug "Load ${user_script}"
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
        try {
            test_spec.setup_exec(test_items)
            log.debug "\tresults : " + summaryReport(test_items)
        } catch (Exception e) {
            log.error "[Test] Failed to run ${test_spec.title}, skip.\n" + e
        }
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

    def verify() {
        def verifier = VerifyRuleGenerator.instance
        def statuses = [:]
        result_test_items.each { test_item ->
            test_item.with {
                results.each { id, test_value ->
                    def status = verifier.verify(verify_id, domain, id, test_value, server_info)
                    if (status != null) {
                        verify_status[id] = status
                        statuses[id] = status
                    }
                }
            }
        }
        return statuses
    }

    def getVerifyStatuses() {
        def statuses = [:]
        result_test_items.each { test_item ->
            statuses << test_item.verify_status
        }
        return statuses
    }

    def getResults() {
        def results = [:]
        result_test_items.each { test_item ->
            results << test_item.results
        }
        return results
    }
}

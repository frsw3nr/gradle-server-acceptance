package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString(includePackage = false)
class TestPlatform extends SpecModel {
    String name
    TestTarget test_target
    LinkedHashMap<String,TestResult> test_results  = [:]
    LinkedHashMap<String,TestMetric> test_metrics  = [:]
    TestRule test_rule

    def accept(visitor){
        visitor.visit_test_platform(this)
    }

    def count_test_result_status() {
        def counts = [:].withDefault{0}
        test_results.each { name, test_result ->
            counts[test_result.status] ++
        }
        return counts
    }
}

@Slf4j
@ToString(includePackage = false)
class TestPlatformSet extends SpecCompositeModel {
    String name

    // def children = new ConfigObject()

    // def add(test_platform) {
    //     test_platform.with {
    //         this.children[name] = it
    //     }
    // }

    def accept(visitor) {
        visitor.visit_test_platform_set(this)
    }

    // def get_all() {
    //     return this.children
    // }
}

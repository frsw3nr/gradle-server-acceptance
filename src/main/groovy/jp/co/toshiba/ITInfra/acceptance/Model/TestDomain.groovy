package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString
class TestDomain extends SpecModel {
    String name
    TestResult test_results = [:]

    def accept(visitor){
        visitor.visit_test_domain(this)
    }
}

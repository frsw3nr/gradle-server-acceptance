package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString
class TestTarget extends SpecModel {
    String name
    String domain
    String ip
    String os_account_id
    LinkedHashMap<String,TestDomain> test_domains = [:]
    LinkedHashMap<String,TestRule> test_rules = [:]
}

@Slf4j
class TestTargetSet extends TestTarget {
    def children = new ConfigObject()

    def add(test_target) {
        test_target.with {
            this.children[name][domain] = it
        }
    }

    def accept(visitor){
        visitor.visit_test_target(this)
    }

    def get_all() {
        return this.children
    }
}

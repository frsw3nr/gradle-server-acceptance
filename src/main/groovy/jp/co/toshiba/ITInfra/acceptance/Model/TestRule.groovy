package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString
class TestRule extends SpecModel {
    String name
    String compare_rule
    String compare_server
    ConfigObject config

    def accept(visitor){
        visitor.visit_test_rule(this)
    }
}

@Slf4j
class TestRuleSet extends TestRule {
    def children = new ConfigObject()

    def add(test_rule) {
        test_rule.with {
            this.children[name] = it
        }
    }

    def accept(visitor){
        visitor.visit_test_rule(this)
    }

    def get_all() {
        return this.children
    }
}

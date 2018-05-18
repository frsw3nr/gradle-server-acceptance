package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString(includePackage = false)
class TestScenario extends SpecModel {
    String name
    TestTargetSet test_targets
    TestMetricSet test_metrics
    TestRuleSet test_rules
    TestTemplateSet test_templates

    def accept(visitor){
        visitor.visit_test_scenario(this)
    }

    def get_domain_targets() {
        def new_targets = [:].withDefault{[:]}
        test_targets.get_all().each { target, domain_targets ->
            domain_targets.each {domain, test_target ->
                new_targets[domain][target] = test_target
            }
        }
        return new_targets
    }
}

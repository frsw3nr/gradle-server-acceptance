package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.transform.ToString
import groovy.util.logging.Slf4j

@Slf4j
@ToString(includePackage = false)
class TestScenario extends SpecModel {
    String name
    TestTargetSet test_targets
    TestMetricSet test_metrics
    // TestRuleSet test_rules
    TestTemplateSet test_templates
    TestReportSet test_reports
    int exit_code = 0

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

    def get_test_platform(String target_name, String platform) {
        def test_target = this.test_targets.get(target_name)
        TestPlatform test_platform = null
        test_target.each { domain, domain_target ->
            test_platform = domain_target?.test_platforms?."$platform"
            if (test_platform) {
                return test_platform
            }
        }
        return test_platform
    }
}

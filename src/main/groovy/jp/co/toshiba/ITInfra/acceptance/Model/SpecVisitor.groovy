package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment

@CompileStatic
abstract class SpecVisitor {
    def set_environment(SpecModel model) {}
    def visit_test_platform(SpecModel model) {}
    def visit_test_platform_set(SpecModel model) {}
    def visit_test_report_set(SpecModel model) {}
    def visit_test_rule(SpecModel model) {}
    def visit_test_rule_set(SpecModel model) {}
    def visit_test_scenario(SpecModel model) {}
    def visit_test_target_set(SpecModel model) {}
    def visit_test_template(SpecModel model) {}
    def visit_test_template_set(SpecModel model) {}
    def visit_test_error_report_set(SpecModel model) {}
    def visit_test_metric_set(SpecModel model) {}
}

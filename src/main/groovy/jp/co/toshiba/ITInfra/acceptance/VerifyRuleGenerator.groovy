package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.Model.TestRuleSet

@Slf4j
@Singleton
class VerifyRuleGenerator {

    final String template_path = './lib/template/VerifyRule.template'
    def spec

    def get_verify_rule_script(Map verify_rules) {
        def f = new File(template_path)
        def engine = new groovy.text.GStringTemplateEngine()
        def binding = ['verify_rules': verify_rules]
        def template = engine.createTemplate(f).make(binding)
        return template.toString()
    }

    def set_verify_rule(Map verify_rules) {
        def rule_code_text = get_verify_rule_script(verify_rules)
        spec = new GroovyClassLoader().parseClass(rule_code_text).newInstance()
    }

    def test_rule_set_as_map(TestRuleSet test_rule_set) {
        def rule_sets = test_rule_set.get_all()
        def rule_map = [:]
        rule_sets.each { rule_id, rule_set ->
            rule_map[rule_id] = rule_set.config
        }
        return rule_map
    }

    def get_verify_rule_script(TestRuleSet test_rule_set) {
        def rule_map = this.test_rule_set_as_map(test_rule_set)
        return this.get_verify_rule_script(rule_map)
    }

    def set_verify_rule(TestRuleSet test_rule_set) {
        def rule_map = this.test_rule_set_as_map(test_rule_set)
        return this.set_verify_rule(rule_map)
    }

    def verify(String verify_id, String domain, String test_id, Object test_value, Map server_info = null) {
        def verify_func = "${verify_id}__${domain}__${test_id}"
        def method = spec.metaClass.getMetaMethod(verify_func, Object, Map)
        if (method) {
            try {
                def result = method.invoke(spec, test_value, server_info)
                log.debug "Verify_rule ${method.name}(${test_value}) = ${result}"
                return result
            } catch (Exception e) {
                log.error "[Verify] Failed to evaluate rule '${test_id}' : " + e
            }
        }
    }
}

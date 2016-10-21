package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import groovy.text.GStringTemplateEngine

@Slf4j
@Singleton
class VerifyRuleGenerator {

    final String template_path = './lib/template/VerifyRule.template'
    def spec

    def getVerifyRuleScript(Map verify_rules) {
        def f = new File(template_path)
        def engine = new groovy.text.GStringTemplateEngine()
        def binding = ['verify_rules': verify_rules]
        def template = engine.createTemplate(f).make(binding)
        return template.toString()
    }

    def setVerifyRule(Map verify_rules) {
        def rule_code_text = getVerifyRuleScript(verify_rules)
        spec = new GroovyClassLoader().parseClass(rule_code_text).newInstance()
    }

    def verify(String verify_id, String domain, String test_id, Object test_value) {
        def verify_func = "${verify_id}__${domain}__${test_id}"
        def method = spec.metaClass.getMetaMethod(verify_func, Object)
        if (method) {
            try {
                def result = method.invoke(spec, test_value)
                log.debug "Verify_rule ${method.name}(${test_value}) = ${result}"
                return result
            } catch (Exception e) {
                log.error "[Verify] Failed to evaluate rule '${test_id}' : " + e
            }
        }
    }
}

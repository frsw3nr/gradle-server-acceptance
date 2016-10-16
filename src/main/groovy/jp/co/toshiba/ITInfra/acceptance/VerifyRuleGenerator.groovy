package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import groovy.text.GStringTemplateEngine

@Slf4j
class VerifyRuleGenerator {

    final String template_dir  = './lib/template'
    final String template_file = 'VerifyRule.template'
    String template_path
    def verify_rules = []

    VerifyRuleGenerator(def verify_rules) {
        this.verify_rules  = verify_rules
        this.template_path = "${template_dir}/${template_file}"
    }

    def generate_code() {
        def f = new File(template_path)
        def engine = new groovy.text.GStringTemplateEngine()
        def binding = ['verify_rules': verify_rules]
        def template = engine.createTemplate(f).make(binding)
        return template.toString()
    }

    def generate_instance() {
        def rule_code_text = this.generate_code()
        def loader = new GroovyClassLoader()
        def spec
        // try {
            def clazz = loader.parseClass(rule_code_text)
            spec = clazz.newInstance()
        // } catch (Exception e) {
        //     def msg = "[VerifyRule] compile error " + e
        //     log.error(msg)
        //     throw new IllegalArgumentException(msg)
        // }
        return spec
    }
}

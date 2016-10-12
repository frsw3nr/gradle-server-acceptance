package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import groovy.text.GStringTemplateEngine

@Slf4j
class CodeGenerator {

    String domain
    String template_path
    def commands = []

    CodeGenerator(String template_dir, String domain) {
        this.domain       = domain
        this.template_path = template_dir + "/get_${domain}_spec.template"
    }

    def addCommand(String test_id, String line) {
        commands.add(['test_id' : test_id, 'line' : line])
    }

    def generate() {
        def f = new File(template_path)
        def engine = new groovy.text.GStringTemplateEngine()
        def binding = ['commands': commands]
        def template = engine.createTemplate(f).make(binding)
        return template.toString()
    }

}

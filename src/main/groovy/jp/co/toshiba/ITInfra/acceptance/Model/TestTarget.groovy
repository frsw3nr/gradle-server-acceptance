package jp.co.toshiba.ITInfra.acceptance.Model


import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.AutoClone
import groovy.transform.ToString
import groovy.util.logging.Slf4j

enum RunStatus {
  INIT, READY, RUN, FINISH, ERROR, COMPARED, TAGGING
}

@Slf4j
@ToString(includePackage = false, excludes="test_platforms")
@AutoClone
class TestTarget extends SpecModel {
    String name
    String domain
    String ip
    String template_id
    String account_id
    String verify_id
    String compare_server
    String tag
    Boolean comparision = false
    RunStatus target_status
    String success_rate
    LinkedHashMap<String,TestPlatform> test_platforms = [:]
    LinkedHashMap<String,TestTemplate> test_templates = [:]
    LinkedHashMap<String,PortList> port_list = [:]

    def trim_template_config_with_null(Map template_config) {
        template_config.find { platform, sub_config ->
            // Remove the element whose value is "null" in the platform setting 
            // of the second hierarchy
            if (!(sub_config instanceof Map))
                return
            while(sub_config.values().remove("null"));

            // If the platform setting is a map, search for the third hierarchy,
            // and remove elements with key or value null
            sub_config.each { key, map ->
                if (map instanceof Map) {
                    while(map.values().remove(null));
                    while(map.values().remove("null"));
                    while(map.keySet().remove("null"));
                }
            }
        }
    }

    def print_json(Map map) {
        def json = new JsonBuilder()
        json(map)
        println json.toPrettyString()
    }

    def make_template_config(Map template_config, Map target_config) {
        // Parse the JSON conversion result with the template engine
        // and return the result of JSON decode
        def json = new JsonBuilder()
        json(template_config)
        def template_config_json = json.toPrettyString()
        def engine = new groovy.text.SimpleTemplateEngine()
        def template = engine.createTemplate(template_config_json).make(target_config)
        def parsed_template_config = new JsonSlurper().parseText(template.toString())
        trim_template_config_with_null(parsed_template_config)
        // print_json(parsed_template_config)
        return parsed_template_config
    }

    public Map asMap() {
        def map = [name:name, domain:domain, ip:ip,
                   template_id:template_id, account_id:account_id,
                   verify_id:verify_id]
        map << this.custom_fields

        def template_config = test_templates[template_id]?.values
        if (template_config && template_config.size() > 0) {
            def parsed_config = this.make_template_config(template_config, map)
            map << parsed_config
        }
        return map
    }

    def clone_test_target_tag(String tag_name) {
        def test_tag_target = new TestTarget(name : "TAG:${tag_name}", 
                                             domain: this.domain,
                                             tag: tag_name,
                                             target_status: RunStatus.TAGGING)
        def test_platforms = new LinkedHashMap<String, TestPlatform>()
        this.test_platforms.each { platform, test_platform ->
            test_platforms[platform] = new TestPlatform(name: test_platform.name)
        }
        test_tag_target.test_platforms = test_platforms
        return test_tag_target
    }
}

@Slf4j
@ToString(includePackage = false)
class TestTargetSet extends SpecCompositeModel {
    String name
    def accept(visitor){
        visitor.visit_test_target_set(this)
    }

    // def children = new ConfigObject()

    def add(test_target) {
        test_target.with {
            this.children[name][domain] = it
        }
    }


}

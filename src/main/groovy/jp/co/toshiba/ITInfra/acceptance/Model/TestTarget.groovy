package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.util.logging.Slf4j
import groovy.transform.AutoClone
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

enum RunStatus {
  INIT, READY, RUN, FINISH, ERROR, COMPARED
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
    Boolean comparision = false
    RunStatus target_status
    String success_rate
    LinkedHashMap<String,TestPlatform> test_platforms = [:]
    LinkedHashMap<String,TestTemplate> test_templates = [:]
    LinkedHashMap<String,TestRule> test_rules = [:]

    def walk( map, Map target_config, root=true ) {
        map.each { key, value ->
            if( value instanceof Map ) {
                // println "$key (${root?'root':'subroot'})"
                // walk( value, false )
                // def engine = new groovy.text.SimpleTemplateEngine()
                // def binding = ["ip":"10.10.10.10"]
                // def text = engine.createTemplate($key).make(binding)
                println "MAP($root) : $key "
                walk( value, target_config, false )
            }
            else {
                println "  NODE: $key, $value"
                key = text
            }
        }
    }

    def convert_key( map, Map target_config, root=true ) {
        map.each { key, value ->
            if( value instanceof Map ) {
                // println "$key (${root?'root':'subroot'})"
                // walk( value, false )
                // def engine = new groovy.text.SimpleTemplateEngine()
                // def binding = ["ip":"10.10.10.10"]
                // def text = engine.createTemplate($key).make(binding)
                println "MAP($root) : $key "
                convert_key( value, target_config, false )
            }
            else {
                def engine = new groovy.text.SimpleTemplateEngine()
                def new_key = engine.createTemplate(key).make(target_config)
                println "  NODE: $key, $text, $value"
            }
        }
    }

    def make_template_config(Map template_config, Map target_config) {
        def target_template_config = template_config.clone()
        println "CONVERT:"
        this.convert_key(target_template_config, target_config)
        println "WALK:"
        this.walk(target_template_config, target_config)
        return target_template_config
    }

    public Map asMap() {
        def map = [name:name, domain:domain, ip:ip,
                   template_id:template_id, account_id:account_id,
                   verify_id:verify_id]
        map << this.custom_fields

        def template_config = test_templates[template_id]?.values
        if (template_config && template_config.size() > 0) {
            // this.walk(template_config)
            println "template_config:$template_config"
            def target_template_config = this.make_template_config(template_config,
                                                                   map)
            map << target_template_config
        }
        return map
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


    // def copy(source_name, target_name) {
    //     def source_domains = this.children[source_name]
    //     def target_domains = [:]
    //     source_domains.each { domain_name, test_source ->
    //         def test_target = test_source.clone()
    //         test_target.name = target_name
    //         target_domains[domain_name] = test_target
    //     }
    //     this.children[target_name] = target_domains
    // }

    // def check_filter(name, keyword) {
    //     def matched = false
    //     if (!keyword) {
    //         matched = true
    //     } else {
    //         ( name =~ /${keyword}/ ).each { m0 ->
    //             matched = true
    //         }
    //     }
    //     return matched
    // }

    // def search_all(String keyword) {
    //     def filterd = new ConfigObject()
    //     this.children.each { name, object ->
    //         if (this.check_filter(name, keyword)) {
    //             filterd[name] = object
    //         }
    //     }
    //     return filterd
    // }

    // def get_all() {
    //     return this.children
    // }

}

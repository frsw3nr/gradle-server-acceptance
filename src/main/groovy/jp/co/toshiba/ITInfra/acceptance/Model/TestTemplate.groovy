package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.util.logging.Slf4j
import groovy.transform.AutoClone
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString(includePackage = false)
@AutoClone
class TestTemplate extends SpecModel {
    String name
    ConfigObject values

    def accept(visitor) {
        visitor.visit_test_template(this)
    }
}

@Slf4j
@ToString(includePackage = false)
class TestTemplateSet extends TestTemplate {
    def children = new ConfigObject()

    def add(test_target) {
        test_target.with {
            this.children[name] = it
        }
    }

    def copy(source_name, target_name) {
        def source_domains = this.children[source_name]
        def target_domains = [:]
        source_domains.each { domain_name, test_source ->
            def test_target = test_source.clone()
            test_target.name = target_name
            target_domains[domain_name] = test_target
        }
        this.children[target_name] = target_domains
    }

    def accept(visitor){
        visitor.visit_test_template_set(this)
    }

    def check_filter(name, keyword) {
        def matched = false
        if (!keyword) {
            matched = true
        } else {
            ( name =~ /${keyword}/ ).each { m0 ->
                matched = true
            }
        }
        return matched
    }

    def search_all(String keyword) {
        def filterd = new ConfigObject()
        this.children.each { name, object ->
            if (this.check_filter(name, keyword)) {
                filterd[name] = object
            }
        }
        return filterd
    }

    def get_all() {
        return this.children
    }
}

package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.transform.AutoClone
import groovy.transform.ToString
import groovy.util.logging.Slf4j

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
class TestTemplateSet extends SpecCompositeModel {
    String name

    def accept(visitor){
        visitor.visit_test_template_set(this)
    }

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

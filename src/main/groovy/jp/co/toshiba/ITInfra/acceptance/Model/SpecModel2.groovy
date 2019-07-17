package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@Slf4j
@ToString(includePackage = false)
abstract class SpecModel2 {
    def custom_fields = [:]
    def propertyMissing(String name, value) { custom_fields[name] = value }
    def propertyMissing(String name) { custom_fields[name] }
}

@CompileStatic
@Slf4j
@ToString(includePackage = false)
class SpecModelLeaf2 extends SpecModel2 { }

@Slf4j
@ToString(includePackage = false)
class SpecModelComposite2 extends SpecModel2 {
    LinkedHashMap<String,Object> children = new LinkedHashMap<>()

    def add(SpecModel2 child) {
        this.children.put(child.name, child)
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

    // def get_all() {
    //     return this.children
    // }

    // def get_keys() {
    //     return this.children.keySet()
    // }

    // def get(String key) {
    //     return this.children?."$key"
    // }

    // def get(String key, String domain) {
    //     return this.children?."$key"?."$domain"
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

    // def search_all(String search_keywords) {
    //     def filterd = new ConfigObject()
    //     // println "KEYWORDS: ${search_keywords}, ${keywords}"
    //     def keywords = (search_keywords) ? search_keywords.split(",") : [null]
    //     keywords.each { keyword ->
    //         children.each { name, test_metric ->
    //             if (this.check_filter(name, keyword)) {
    //                 filterd[name] = test_metric
    //             }
    //         }
    //     }
    //     return filterd
    // }

    // def count() {
    //     def n = 0
    //     this.children.each { key, value ->
    //         n += value.count()
    //     }
    //     return n
    // }
}

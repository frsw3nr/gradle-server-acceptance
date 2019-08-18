package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.transform.AutoClone
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@AutoClone
@Slf4j
@ToString(includePackage = false)
class TestMetric extends SpecModel {
    String name
    String category
    String description
    String platform
    String comment
    Boolean enabled
    int snapshot_level = -1
    Boolean device_enabled

    def count() { return 1 }

    def get_definitions() {
        return [
            (enabled)? 'Y':'',
            category,
            description,
            name,
            (device_enabled)? 'Y':'',
            comment,
            platform,
        ]
    }

//    static List<TestMetric> search(TestScenario test_scenario, String domain = null) {
   static def search(TestScenario test_scenario, String domain) {
        def domain_metrics = test_scenario.test_metrics.get_all()
        return domain_metrics[domain].get_all()
   }
}

@Slf4j
@ToString(includePackage = false)
class TestMetricSet extends SpecCompositeModel {
    String name
    // def children = new ConfigObject()

    // def add(test_metric) {
    //     test_metric.with {
    //         this.children[name] = it
    //     }
    // }

    def accept(visitor) {
        visitor.visit_test_metric_set(this)
    }

    def search_all(String filter_metric, int snapshot_level = -1) {
        // def filterd = new ConfigObject()
        // children.each { name, test_metric ->
        //     println "SEARCH:$filter_metric, $name"
        //     if (test_metric.enabled && this.check_filter(name, filter_metric)) {
        //         filterd[name] = test_metric
        //     }
        // }
        // println "search_all: ${filter_metric}, ${snapshot_level}"
        // def filterd = super.search_all(filter_metric)
        // def new_filterd = filterd.findAll { it.value.enabled == true }.each { it }
        // println "search_all: ${filter_metric}, ${snapshot_level}"
        def filterd = super.search_all(filter_metric)
        def new_filterd = [:]
        filterd.findAll { metric_name, test_metric ->
            if (test_metric.enabled) {
                if (filter_metric) {
                    new_filterd[metric_name] = test_metric
                } else if (snapshot_level == -1) {
                    new_filterd[metric_name] = test_metric
                } else if (test_metric.snapshot_level <= snapshot_level) {
                    new_filterd[metric_name] = test_metric
                }
            }
        }
        return new_filterd
    }

    // def get_all() {
    //     return this.children
    // }

    // def get_keys() {
    //     return this.children.keySet()
    // }

    // def get(String key) {
    //     return this.children?."$key"
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

    // def search_all(String filter_metric) {
    //     def filterd = new ConfigObject()
    //     children.each { name, test_metric ->
    //         if (test_metric.enabled && this.check_filter(name, filter_metric)) {
    //             filterd[name] = test_metric
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

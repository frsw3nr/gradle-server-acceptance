package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString(includePackage = false)
class TestMetric extends SpecModel {
    String name
    String description
    String platform
    Boolean enabled
    Boolean device_enabled

    def count() { return 1 }
}

@Slf4j
@ToString(includePackage = false)
class TestMetricSet extends TestMetric {
    def children = new ConfigObject()

    def add(test_metric) {
        test_metric.with {
            this.children[name] = it
        }
    }

    def accept(visitor) {
        visitor.visit_test_metric_set(this)
    }

    def get_all() {
        return this.children
    }

    def get(String key) {
        return this.children?."$key"
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

    def search_all(String filter_metric) {
        def filterd = new ConfigObject()
        children.each { name, test_metric ->
            if (test_metric.enabled && this.check_filter(name, filter_metric)) {
                filterd[name] = test_metric
            }
        }
        return filterd
    }

    def count() {
        def n = 0
        this.children.each { key, value ->
            n += value.count()
        }
        return n
    }
}

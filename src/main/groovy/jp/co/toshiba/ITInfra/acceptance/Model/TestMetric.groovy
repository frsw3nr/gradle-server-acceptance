package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString

@Slf4j
@ToString
class TestMetric extends SpecModel {
    def id
    def name
    Boolean enabled
    Boolean device_enabled
    TestResult test_result
}

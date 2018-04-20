package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j

@Slf4j
class TestMetric extends SpecModel {
    def id
    Boolean enabled
    Boolean device_enabled
    TestResult TestResult
}

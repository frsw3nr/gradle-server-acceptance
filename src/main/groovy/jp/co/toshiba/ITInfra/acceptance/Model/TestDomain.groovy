package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j

@Slf4j
class TestDomain extends SpecModel {
    def test_metrics = [:]

    TestDomain(Map properties = [:]) {
        properties.each { name, value ->
            this."${name}" = value
        }
    }
}

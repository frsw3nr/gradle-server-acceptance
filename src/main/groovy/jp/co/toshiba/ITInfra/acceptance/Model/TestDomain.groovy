package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString

@Slf4j
@ToString
class TestDomain extends SpecModel {
    def name
    def test_metrics = [:]
}

package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString
class TestMetric extends SpecModel {
    String id
    String name
    Boolean enabled
    Boolean device_enabled

    def create(line) {
        def metric = null
        metric = new TestMetric(id: line['ID'], name: line['項目'], enabled: line['Test'], device_enabled: line['デバイス'])
        return metric
    }
}

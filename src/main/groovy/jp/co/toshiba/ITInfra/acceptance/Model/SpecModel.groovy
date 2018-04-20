package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString

@Slf4j
@ToString
class SpecModel {
    String name = null

    SpecModel(Map properties = null) {
        properties.each { name, value ->
            this."${name}" = value
        }
    }

}

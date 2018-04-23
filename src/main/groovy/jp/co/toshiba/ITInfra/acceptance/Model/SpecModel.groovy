package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString

@Slf4j
@ToString
class SpecModel {
   def custom_fields = [:]
   def propertyMissing(String name, value) { custom_fields[name] = value }
   def propertyMissing(String name) { custom_fields[name] }
}

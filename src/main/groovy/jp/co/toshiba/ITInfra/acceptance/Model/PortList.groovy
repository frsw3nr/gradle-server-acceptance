package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

enum PortType {
  UPPER, LOWER
}

@Slf4j
@ToString(includePackage = false)
class PortList extends SpecModel {
    String ip
    String description
    String netmask
    PortType port_type
    Boolean online

    public Map asMap() {
        def map = [ip : ip, description : description,
                   port_type : port_type, online : online]
        map << this.custom_fields

        return map
    }
}

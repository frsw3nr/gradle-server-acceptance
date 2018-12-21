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
    Boolean online
    PortType port_type

    public Map asMap() {
        def map = [ip : ip, description : description, netmask: netmask,
                   online : online, port_type : port_type]
        map << this.custom_fields

        return map
    }
}

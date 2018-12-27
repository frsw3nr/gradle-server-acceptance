package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

enum PortType {
  UPPER, LOWER, MANAGED
}

@Slf4j
@ToString(includePackage = false)
class PortList extends SpecModel {
    String ip
    String description
    String mac
    String vendor
    String switch_name
    String netmask
    String device_type
    Boolean online
    PortType port_type

    public Map asMap() {
        def map = [
                ip : ip, 
                description : description, 
                mac         : mac, 
                vendor      : vendor, 
                switch_name : switch_name, 
                netmask     : netmask, 
                device_type : device_type,
                online      : online, 
                port_type   : port_type
            ]
        map << this.custom_fields

        return map
    }
}

package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.transform.ToString
import groovy.util.logging.Slf4j

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
    String subnet
    String port_no
    String device_type
    Boolean lookup
    Boolean managed
    PortType port_type

    public Map asMap() {
        def map = [
                ip : ip, 
                description : description, 
                mac         : mac, 
                vendor      : vendor, 
                switch_name : switch_name, 
                netmask     : netmask, 
                subnet      : subnet, 
                port_no     : port_no,
                device_type : device_type,
                lookup      : lookup, 
                managed     : managed,
                port_type   : port_type
            ]
        map << this.custom_fields

        return map
    }
}

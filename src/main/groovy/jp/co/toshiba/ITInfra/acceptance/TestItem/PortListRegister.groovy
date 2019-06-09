package jp.co.toshiba.ITInfra.acceptance

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.Model.PortList
import jp.co.toshiba.ITInfra.acceptance.Model.PortType
import jp.co.toshiba.ITInfra.acceptance.TestItem

@Slf4j
@ToString(includePackage = false)
// @Singleton
class PortListRegister {

    TestItem test_item

    PortListRegister(TestItem test_item) {
        this.test_item = test_item
    }

    // PortListRegister test_item(TestItem test_item) {
    //     this.test_item = test_item
    //     return this
    // }

    def port_list(String ip, 
                  String description = null,
                  String mac         = null,
                  String vendor      = null,
                  String switch_name = null,
                  String netmask     = null,
                  String subnet      = null,
                  String port_no     = null,
                  String device_type = null,
                  Boolean lookup     = null,
                  Boolean managed    = null,
                  PortType port_type = null) {
        def _port_list = test_item.port_lists?."${ip}" ?:
                         new PortList(ip : ip, 
                                      description : description, 
                                      mac :         mac, 
                                      vendor :      vendor, 
                                      switch_name : switch_name, 
                                      netmask :     netmask,
                                      subnet :      subnet,
                                      port_no :     port_no,
                                      device_type : device_type,
                                      lookup :      lookup,
                                      managed :     managed,
                                      port_type :   port_type, 
                                     )
        if (lookup != null)
            _port_list.lookup = lookup
        if (managed != null)
            _port_list.managed = managed
        test_item.port_lists[ip] = _port_list
    }

    def lookuped_port_list(String ip, 
                         String description = null,
                         String mac         = null,
                         String vendor      = null,
                         String switch_name = null,
                         String netmask     = null,
                         String subnet      = null,
                         String port_no     = null,
                         String device_type = null) {
        this.port_list(ip, description, mac, vendor, switch_name, netmask, subnet, port_no, device_type, true)
    }

    def admin_port_list(String ip, 
                        String description = null,
                        String mac         = null,
                        String vendor      = null,
                        String switch_name = null,
                        String netmask     = null,
                        String subnet      = null,
                        String port_no     = null,
                        String device_type = null) {
        this.port_list(ip, description, mac, vendor, switch_name, netmask, subnet, port_no, device_type, true, true)
    }

}

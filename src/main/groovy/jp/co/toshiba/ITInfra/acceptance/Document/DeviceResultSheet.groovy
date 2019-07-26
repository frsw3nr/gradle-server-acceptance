package jp.co.toshiba.ITInfra.acceptance

/**
 *  Device results factory
 *  The key set is ['domain', 'test_id'] .
 *  Member headers contains header name list .
 *  Member csvs contains csv data with server_name key .
 */
import groovy.util.logging.Slf4j

@Slf4j
class DeviceResultSheet {

    // final max_csv_row = 1048576
    // def headers = [:].withDefault{[:]}
    // def csvs    = [:].withDefault{[:].withDefault{[:]}}
    // def csv_row = 0

    // /**
    //  * @param
    //  * @return
    //  */
    // def setResults(String domain, String server_name, List test_items) {
    //     test_items.each { test_item ->
    //         if (test_item.device_header && test_item.devices) {
    //             def test_id = test_item.test_id
    //             def rownum = test_item.devices.size()
    //             log.debug "Add device result ${domain}, ${server_name}, ${test_id}: ${rownum}"
    //             if (csv_row + rownum >= max_csv_row) {
    //                 def msg = "Failed to add csv data, Max Excel row limit exceed : ${max_csv_row}, Sheet '${domain}_${test_id}'"
    //                 throw new IllegalArgumentException(msg)
    //             }
    //             this.headers[domain][test_id] = test_item.device_header
    //             this.csvs[domain][test_id].put(server_name, test_item.devices)
    //             csv_row += rownum
    //         }
    //     }
    // }

    // def getHeaders(String domain, String test_id) throws NullPointerException {
    //     return headers[domain][test_id]
    // }

    // def getCSVs(String domain, String test_id) throws NullPointerException {
    //     return csvs[domain][test_id]
    // }
}

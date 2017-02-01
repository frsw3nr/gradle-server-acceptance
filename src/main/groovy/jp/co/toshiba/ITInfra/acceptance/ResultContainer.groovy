package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import groovy.transform.ToString
import groovy.util.ConfigObject
import static groovy.io.FileType.FILES
import static groovy.json.JsonOutput.*
import groovy.json.*

@Slf4j
@Singleton
class ResultContainer {
    ConfigObject test_results   = new ConfigObject()
    ConfigObject device_results = new ConfigObject()

    def loadNodeConfigJSON(String node_dir, String server_name)
        throws IOException, IllegalArgumentException {

        new File(node_dir).eachDir { platform_dir ->
            def platform = platform_dir.name
            platform_dir.eachFileMatch(FILES, ~/.*\.json/){ server_config_json ->
                if (server_config_json.name == "${server_name}.json") {
                    def results = new JsonSlurper().parseText(server_config_json.text)
                    results.each { result ->
                        result.with {
                            this.test_results[platform][server_name][test_id] = value
                        }
                    }
                }
            }
            platform_dir.eachDir { device_config_dir ->
                if (device_config_dir.name == server_name) {
                    device_config_dir.eachFileMatch(FILES, ~/.*\.json/) { device_config_json ->
                        ( device_config_json.name =~ /(.+).json/ ).each { m0, test_id ->
                            def results = new JsonSlurper().parseText(device_config_json.text)
                            this.device_results[platform][server_name][test_id] = results
                        }
                    }
                }
            }
        }
    }

    def readNodeConfig(String node_config_dir, String domain, String server_name) {
        if (!servers[domain][server_name]) {
            def node_path = "${node_config_dir}/${domain}/${server_name}"
            def server_config_json = new File("${node_path}.json")
            if (server_config_json.exists()) {
                def results = new JsonSlurper().parseText(server_config_json.text)
                def server_items = [:]
                results.each {
                    server_items[it.test_id] = it.value
                }
                servers[domain][server_name] = server_items
            }
            def device_config_dir = new File(node_path)
            if (device_config_dir.exists()) {
                device_config_dir.eachFile { device_config ->
                    ( device_config.name =~ /(.+).json/ ).each { json_file, test_id ->
                        def results = new JsonSlurper().parseText(device_config.text)
                        devices[domain][server_name][test_id] = results
                    }
                }
            }
        }
        return servers[domain][server_name]
    }

    def setHostConfig(String server_name, String domain, Map domain_results) {
        servers[server_name][domain] = domain_results['test']
    }

    def setDeviceConfig(String server_name, String domain, DeviceResultSheet device_results) {
        def test_headers = device_results['headers'][domain]
        device_results['csvs'][domain].each {test_id, server_domain_csvs ->
            if (server_domain_csvs[server_name]) {
                def header =  test_headers[test_id]
                def csvs = server_domain_csvs[server_name]
                def results = []
                csvs.each { csv ->
                    def i = 0
                    def values = [:]
                    header.each { header_name ->
                        values[header_name] = csv[i]
                        i++
                    }
                    results << values
                }
                devices[server_name][domain][test_id] = results
            }
        }
    }

    def compareMetric(String domain, String server_name, String test_id, String value) {
        def compare_value = servers?.server_name?.domain?.test_id
        return (compare_value == value) ? "Same as '$server_name'" : value
    }

}

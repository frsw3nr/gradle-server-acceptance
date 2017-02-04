package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import groovy.transform.ToString
import groovy.util.ConfigObject
import static groovy.io.FileType.FILES
import static groovy.json.JsonOutput.*
import groovy.json.*
import groovy.sql.Sql
import java.sql.*

@Slf4j
@Singleton
class ResultContainer {
    ConfigObject test_results   = new ConfigObject()
    ConfigObject device_results = new ConfigObject()
    final def pad_digit = 6

    def loadNodeConfigJSON(EvidenceManager evidence_manager, String server_name)
        throws IOException, IllegalArgumentException {
        if (!test_results[server_name]) {
            new File(evidence_manager.node_dir).eachDir { platform_dir ->
                def platform = platform_dir.name
                platform_dir.eachFileMatch(FILES, ~/.*\.json/){ server_config_json ->
                    if (server_config_json.name == "${server_name}.json") {
                        def results = new JsonSlurper().parseText(server_config_json.text)
                        results.each { result ->
                            result.with {
                                this.test_results[server_name][platform][test_id] = value
                            }
                        }
                    }
                }
                platform_dir.eachDir { device_config_dir ->
                    if (device_config_dir.name == server_name) {
                        device_config_dir.eachFileMatch(FILES, ~/.*\.json/) { device_config_json ->
                            ( device_config_json.name =~ /(.+).json/ ).each { m0, test_id ->
                                def results = new JsonSlurper().parseText(device_config_json.text)
                                def row = 1
                                results.each { result ->
                                    this.device_results[server_name][platform][test_id]["row${row}"] = result
                                    row ++
                                }
                            }
                        }
                    }
                }
            }
        }
        test_results[server_name]
    }

    def getCMDBNodeConfig(EvidenceManager evidence_manager, String server_name)
        throws SQLException, IllegalArgumentException {
        def cmdb_model = CMDBModel.instance
        cmdb_model.initialize(evidence_manager)
        def result_rows = cmdb_model.getMetricByHost(server_name)

        result_rows.each { result_row ->
            result_row.with {
                this.test_results[node_name][domain_name][metric_name] = value
            }
        }

        def device_result_rows = cmdb_model.getDeviceResultByHost(server_name)
        def device_sets = [:]
        device_result_rows.each { device_result_row ->
            device_result_row.with {
                this.device_results[node_name][domain_name][metric_name]["row${seq}"][item_name] = value
            }
        }
    }

    def setNodeConfig(String server_name, String platform, List domain_results) {
        log.debug "setNodeConfig ${server_name}, ${platform}"
        domain_results.each { domain_result->
            domain_result.with {
                this.test_results[server_name][platform] << results
                def row = 1
                devices.each { device_columns ->
                    def column = 0
                    device_columns.each { device_column ->
                        def item_name = device_header[column]
                        this.device_results[server_name][platform][test_id]["row${row}"][item_name] = device_column
                        column ++
                    }
                    row ++
                }
            }
        }
    }

    def compareMetric(String server_name, String platform, String test_id, String value) {
        def compare_value = this.test_results[server_name][platform][test_id]
        log.debug "Compare[${server_name}:${test_id}] ${value} == ${compare_value}"
        return (compare_value == value)
    }
}

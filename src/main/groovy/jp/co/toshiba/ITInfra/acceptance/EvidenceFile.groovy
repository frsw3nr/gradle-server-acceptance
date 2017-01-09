package jp.co.toshiba.ITInfra.acceptance

import groovy.io.FileType
import groovy.util.logging.Slf4j
import static groovy.json.JsonOutput.*
import groovy.json.*
import org.apache.commons.io.FileUtils
import groovy.sql.Sql
import java.sql.*

@Slf4j
class EvidenceFile {

    final static def current_build = 1
    String home
    String project_name
    String tenant_name
    String last_run_config
    String config_db_file
    def cmdb

    EvidenceFile(Map params) {
        assert params.home
        this.home = params.home
        this.project_name = new File(this.home).getName()
        this.tenant_name = '_Default'
        this.last_run_config = params.last_run_config ?: "${params.home}/build/.last_run"
        this.config_db_file  = params.config_db_file ?: 'config/config_db.groovy'
    }

    def getNodeDirSource() throws IOException {
        def last_run_json = new File(last_run_config).text
        def last_run = new JsonSlurper().parseText(last_run_json)
        def node_dir_source = last_run?.node_dir
        if (!node_dir_source) {
            def msg = "Config not found node_dir : ${last_run_config}"
            throw new IllegalArgumentException(msg)
        }
        return node_dir_source
    }

    def generate() throws IOException {
        assert home
        def last_run_json = new File("$home/build/.last_run").text
        def last_run = new JsonSlurper().parseText(last_run_json)
        def node_path = new File("./node").getAbsolutePath()
        FileUtils.copyDirectory(new File(last_run.node_dir), new File(node_path))
    }

    def registMaster(table_name, columns) {
        def table = cmdb.dataSet(table_name)
        try {
            table.add(columns)
        } catch (SQLException e) {
            log.warn "This table already have a row, Skip\n" +
                     "${table_name} : ${columns}"
        }
    }

    def getMasterHash(table_name, key_columns, id_column) throws SQLException {
        def table = cmdb.dataSet(table_name)
        def result = [:]
        table.each { row ->
            def keys = []
            key_columns.each { key_column ->
                keys << row[key_column]
            }
            result[keys.join("|")] = row[id_column]
        }
        return result
    }

    def exportCMDB() throws IOException, SQLException, IllegalArgumentException {
        assert home
        initializeCMDB()
        def node_dir_source = getNodeDirSource()
        new File(node_dir_source).eachDir {
            def domain_name = it.name
            println "Regist DOMAIN ${domain_name}"
            registMaster("DOMAIN", [DOMAIN_NAME: domain_name])
            def domain_dir = "${node_dir_source}/${domain_name}"

            // Regist Metrics
            new File(domain_dir).eachFile {
                ( it =~ /(.+).json/).each { m0, node_name->
                    def metric_text = new File("${domain_dir}/${it.name}").text
                    def metrics = new JsonSlurper().parseText(metric_text)
                    println "Regist NODE ${node_name}"
                    registMaster("NODE", [NODE_NAME: node_name,
                                 SITE_NAME: project_name, TENANT_NAME: tenant_name])
                    metrics.each { metric ->
                        println "Regist METRIC ${metric}"
                        registMaster("METRIC", [METRIC_NAME: metric?.test_id,
                                     DOMAIN_NAME: domain_name])
                    }
                }
            }

            // Regist Devices
            new File(domain_dir).eachDir {
                def node_name = it.name
                def device_dir = "${domain_dir}/${node_name}"
                new File(device_dir).eachFile {
                    ( it.name =~/(.+).json/).each { m0, metric_name->
                        def device_text = new File("${device_dir}/${it.name}").text
                        def devices = new JsonSlurper().parseText(device_text)
                        println "Regist METRIC_DEVICE ${node_name} ${metric_name}"
                        devices.each { device ->
                            println "Regist DEVICE ${device}"
                        }

                    }
                }
            }

        }
    }

    def exportCMDBAll() throws IOException, SQLException {
    }

    def initializeCMDB() throws IOException, SQLException {
        def config_db = Config.instance.read(config_db_file)
        def config_ds = config_db?.cmdb?.dataSource
        if (!config_ds) {
            def msg = "Config not found cmdb.dataSource: ${config_db_file}"
            throw new IllegalArgumentException(msg)
        }
        cmdb = Sql.newInstance(config_ds['url'], config_ds['username'],
                              config_ds['password'], config_ds['driver'])

        // Confirm existence of Version table. If not, execute db create script
        def cmdb_version = null
        try {
            List rows = cmdb.rows('SELECT BUILD FROM VERSION')
            if (rows.size()) {
                cmdb_version = rows[0]['BUILD'] ?: null
            }
        } catch (SQLException e) {
            log.warn "VERSION table not found in CMDB, Skip"
        }
        if (cmdb_version == null) {
            def create_db_script = "lib/script/cmdb/create_db.sql"
            def create_sqls = new File(create_db_script).text.split(/;\s*\n/).each {
                try {
                    cmdb.execute it
                } catch (SQLException e) {
                    log.error "Create table : " + e + "\n" + it
                }
            }
        // If the version is old, execute update scrit (TBD)
        } else if (cmdb_version < current_build) {
            log.warn "Rebuild table (TBD)"
        }
        // Regist SITE table
        registMaster("SITE", [SITE_NAME: project_name])
        // def site = cmdb.dataSet("SITE")
        // try {
        //     site.add()
        // } catch (SQLException e) {
        //     log.warn "Already exist '${project_name}' in SITE table, Skip"
        // }
    }

    def registMetricDB() {
        //
    }

    def registDeviceDB() {
        //
    }

}

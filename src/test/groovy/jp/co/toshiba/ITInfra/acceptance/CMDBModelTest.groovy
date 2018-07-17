import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import static groovy.json.JsonOutput.*
import groovy.json.*
import groovy.sql.Sql
import java.sql.*

// gradle --daemon test --tests "CMDBModelTest.設定読み込み"

class CMDBModelTest extends Specification {

    def params = [:]
    def config
    def evidence_manager
    def cmdb_config
    def cmdb_model
    ConfigTestEnvironment test_env

    def setup() {
        def config_file = 'src/test/resources/config.groovy'
        test_env = ConfigTestEnvironment.instance
        test_env.read_config(config_file)

    //     params = [
    //         getconfig_home: System.getProperty("user.dir"),
    //         project_home: 'src/test/resources',
    //         db_config: 'src/test/resources/cmdb.groovy'
    //     ]
    //     evidence_manager = new EvidenceManager(params)
    //     cmdb_model = CMDBModel.instance
    //     cmdb_model.initialize(evidence_manager)
    }

    def 設定読み込み() {
        when:
        cmdb_config = test_env.read_cmdb_config()
        def json = new groovy.json.JsonBuilder()
        json(cmdb_config)
        println json.toPrettyString()
        // println "CMDB_CONFIG: $cmdb_config"

        then:
        1 == 1
    }

    // def "DB登録"() {
    //     when:
    //     cmdb_model.export('src/test/resources/log/_node')

    //     then:
    //     1 == 1
    // }

    // def "マスター登録"() {
    //     when:
    //     def id = cmdb_model.registMaster("sites", [site_name: 'site01'])
    //     def site = cmdb_model.cmdb.rows("select * from sites")

    //     then:
    //     site[0]['id'] > 0
    //     site[0]['site_name'].size() > 0
    // }

    // def "マスター重複登録"() {
    //     when:
    //     def id1 = cmdb_model.registMaster("tenants", [tenant_name: '_Default'])
    //     def id2 = cmdb_model.registMaster("tenants", [tenant_name: '_Default'])
    //     def tenant = cmdb_model.cmdb.rows("select * from tenants")

    //     then:
    //     id1 == id2
    //     tenant.size() == 1
    // }

    // def "マスター登録複数列"() {
    //     when:
    //     def id = cmdb_model.registMaster("nodes", [node_name: 'node01', tenant_id: 1])
    //     def node = cmdb_model.cmdb.rows("select * from nodes where node_name = 'node01'")

    //     then:
    //     id > 0
    //     node[0]['node_name'] == 'node01'
    //     node[0]['tenant_id'] == 1
    // }

    // def "複数サイトノード登録"() {
    //     when:
    //     def site_id1      = cmdb_model.registMaster("sites", [site_name: 'site01'])
    //     def site_id2      = cmdb_model.registMaster("sites", [site_name: 'site02'])
    //     def node_id1      = cmdb_model.registMaster("nodes", [node_name: 'node01', tenant_id: 1])
    //     def site_node_id1 = cmdb_model.registMaster("site_nodes", [node_id: node_id1, site_id: site_id1])

    //     def node_id2      = cmdb_model.registMaster("nodes", [node_name: 'node01', tenant_id: 1])
    //     def site_node_id2 = cmdb_model.registMaster("site_nodes", [node_id: node_id2, site_id: site_id2])

    //     then:
    //     site_node_id1 != site_node_id2
    //     def sql = "select * from site_nodes where node_id = ? order by site_id"
    //     def site_node = cmdb_model.cmdb.rows(sql, node_id1)
    //     site_node[0]['site_id'] == site_id1
    //     site_node[0]['node_id'] == node_id1
    //     site_node[1]['site_id'] == site_id2
    //     site_node[1]['node_id'] == node_id1
    // }

    // def "マスター登録列名なし"() {
    //     when:
    //     def id = cmdb_model.registMaster("nodes", [HOGE: 'node01', tenant_id: 1])

    //     then:
    //     thrown(SQLException)
    // }

    // def "マスター登録キャッシュ"() {
    //     when:
    //     def id1 = cmdb_model.registMaster("sites", [site_name: 'site01'])
    //     def id2 = cmdb_model.registMaster("sites", [site_name: 'site01'])

    //     then:
    //     id1 == id2
    // }

    // def "メトリック登録"() {
    //     when:
    //     def metric = ["value": "value01", "verify": true]
    //     cmdb_model.registMetric(1, 1, metric)
    //     def metric2 = ["value": "value02", "verify": false]
    //     cmdb_model.registMetric(1, 2, metric2)
    //     def metric3 = ["value": "value03"]
    //     cmdb_model.registMetric(1, 3, metric3)

    //     def sql = "select * from test_results where node_id = ? and metric_id = ?"
    //     def rows = cmdb_model.cmdb.rows(sql, [1, 1])
    //     def rows2 = cmdb_model.cmdb.rows(sql, [1, 2])
    //     def rows3 = cmdb_model.cmdb.rows(sql, [1, 3])

    //     then:
    //     rows[0]['verify']  == 1
    //     rows2[0]['verify'] == 0
    //     rows3[0]['verify'] == null
    // }

    // def "デバイス登録"() {
    //     when:
    //     def metric_id = cmdb_model.registMaster("metrics",
    //                                 [metric_name: 'metric1',
    //                                  domain_id: 1])
    //     def devices = [
    //         ["value": "value01", "verify": true],
    //         ["value": "value02", "verify": false],
    //         ["value": "value03"],
    //     ]
    //     cmdb_model.registDevice(1, metric_id, devices)

    //     def sql = "select * from device_results where node_id = ? and metric_id = ?"
    //     def rows = cmdb_model.cmdb.rows(sql, [1, metric_id])
    //     println rows

    //     def sql2 = "select * from metrics where id = ?"
    //     def rows2 = cmdb_model.cmdb.rows(sql2, metric_id)
    //     println rows2

    //     then:
    //     rows.size() == 5
    // }

    // def "ノード定義のエクスポート"() {
    //     when:
    //     cmdb_model.export(new File('src/test/resources/node/').getAbsolutePath())
    //     def node = cmdb_model.cmdb.rows("select * from nodes")
    //     def result = cmdb_model.cmdb.rows("select * from test_results where node_id = 1")

    //     then:
    //     node[0]['node_name'].size() > 0
    //     node[1]['node_name'].size() > 0
    //     result.size() > 0
    // }

    // def "CMDBメトリック検索"() {
    //     when:
    //     cmdb_model.export(new File('src/test/resources/node/').getAbsolutePath())
    //     def result = cmdb_model.getMetricByHost('ostrich')
    //     def device_result = cmdb_model.getDeviceResultByHost('ostrich')

    //     then:
    //     result.size() > 0
    //     result[0]['domain_name'].size() > 0
    //     result[0]['node_name'].size() > 0
    //     result[0]['metric_name'].size() > 0
    //     result[0]['value'] != null

    //     device_result.size() > 0
    //     device_result[0]['domain_name'].size() > 0
    //     device_result[0]['node_name'].size() > 0
    //     device_result[0]['metric_name'].size() > 0
    //     device_result[0]['seq'] != null
    //     device_result[0]['item_name'].size() > 0
    //     device_result[0]['value'] != null
    // }

    // def "ユニコード登録"() {
    //     setup:
    //     def db = Sql.newInstance(
    //         'jdbc:mysql://localhost:3306/cmdb?useUnicode=true&characterEncoding=utf8',
    //         'root',
    //         'getperf',
    //         'com.mysql.jdbc.Driver'
    //     )

    //     when:
    //     // def metric_text = '''
    //     // |    {
    //     // |        "test_id": "os_architecture",
    //     // |        "domain": "Windows",
    //     // |        "value": "64 \u30d3\u30c3\u30c8"
    //     // |    }
    //     // '''.stripMargin()
    //     def metric_text = new File('src/test/resources/metrics1.json').getText("UTF-8")
    //     def metrics = new JsonSlurper().parseText(metric_text)
    //     println metrics

    //     // mysql> select * from test_result where value like '64%';
    //     // +---------+-----------+--------+--------+---------------------+
    //     // | node_id | metric_id | value  | verify | created             |
    //     // +---------+-----------+--------+--------+---------------------+
    //     // |       2 |        94 | 64 ??? |   NULL | 2017-02-05 08:30:15 |
    //     // +---------+-----------+--------+--------+---------------------+
    //     db.execute('DROP TABLE IF EXISTS unicode_test')
    //     db.execute('create table unicode_test(name VARCHAR(15)  CHARACTER SET utf8mb4 NOT NULL)')
    //     db.execute('insert into unicode_test values(?)', metrics['value'])
    //     def rows = db.rows("select * from unicode_test")
    //     println rows

    //     then:
    //     1 == 1
    // }
}

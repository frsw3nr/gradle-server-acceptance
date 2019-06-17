import jp.co.toshiba.ITInfra.acceptance.InventoryDB
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import jp.co.toshiba.ITInfra.acceptance.Model.ResultStatus
import spock.lang.Specification

import java.sql.SQLException

// gradle --daemon test --tests "InventoryDBTest.設定読み込み"

class InventoryDBTest extends Specification {

    def params = [:]
    def config
    def evidence_manager
    def cmdb_config
    def inventory_db
    def test_env

    def setup() {
        test_env = ConfigTestEnvironment.instance
        test_env.read_config('src/test/resources/config.groovy')
        // test_env.get_inventory_db_config('src/test/resources/config/cmdb.groovy')

        inventory_db = InventoryDB.instance
        test_env.accept(inventory_db)
        // inventory_db.initialize()
        println "CMDB_MODEL:$inventory_db"
    }

    // def 設定読み込み() {
    //     when:
    //     def config = inventory_db.db_config
    //     def json = new groovy.json.JsonBuilder()
    //     json(config)
    //     println json.toPrettyString()

    //     then:
    //     config.dataSource.url != null
    //     config.dataSource.username != null
    //     config.dataSource.password != null
    // }

    // def "マスター登録"() {
    //     when:
    //     def columns = [project_name: 'base', project_path: '/home/base']
    //     def id = inventory_db.registMaster("projects", columns)
    //     def projects = inventory_db.rows("select * from projects")
    //     def json = new groovy.json.JsonBuilder()
    //     json(projects)
    //     println json.toPrettyString()

    //     then:
    //     1 == 1
    //     // site[0]['id'] > 0
    //     // site[0]['site_name'].size() > 0
    // }

    // def "マスター重複登録"() {
    //     when:
    //     def columns = [project_name: 'base2', project_path: '/home/base2']
    //     def id1 = inventory_db.registMaster("projects", columns)
    //     def id2 = inventory_db.registMaster("projects", columns)
    //     def projects = inventory_db.rows("select * from projects")

    //     then:
    //     id1 == id2
    //     projects.size() == 1
    // }

    // def "マスター登録複数列"() {
    //     when:
    //     def columns = [project_id: 1, node_name: 'node01', platform: 'Linux']
    //     def id = inventory_db.registMaster("nodes", columns)
    //     def node = inventory_db.rows("select * from nodes where node_name = ?", 'node01')

    //     then:
    //     id > 0
    //     node[0]['node_name'] == 'node01'
    //     node[0]['project_id'] == 1
    // }

    // def "複数プロジェクトでノード登録"() {
    //     when:
    //     def project_id1 = inventory_db.registMaster("projects", [project_name: 'p01', project_path: 'p01'])
    //     def project_id2 = inventory_db.registMaster("projects", [project_name: 'p02', project_path: 'p02'])
    //     def node_id1 = inventory_db.registMaster("nodes", [node_name: 'node01',platform: 'vCenter', project_id: project_id1])
    //     def node_id2 = inventory_db.registMaster("nodes", [node_name: 'node01',platform: 'Linux', project_id: project_id2])

    //     then:
    //     def rows = inventory_db.rows("select * from nodes")
    //     def json = new groovy.json.JsonBuilder()
    //     json(rows)
    //     println json.toPrettyString()

    //     rows[0]['project_id'] == 1
    //     rows[0]['node_name']  == 'node01'
    //     rows[0]['platform']   == 'vCenter'
    //     rows[1]['project_id'] == 2
    //     rows[1]['node_name']  == 'node01'
    //     rows[1]['platform']   == 'Linux'
    // }

    // def "マスター登録列名なし"() {
    //     when:
    //     def id = inventory_db.registMaster("nodes", [HOGE: 'node01', tenant_id: 1])

    //     then:
    //     thrown(SQLException)
    // }

    // def "マスター登録キャッシュ"() {
    //     when:
    //     def id1 = inventory_db.registMaster("projects", [project_name: 'p01', project_path: 'p01a'])
    //     def id2 = inventory_db.registMaster("projects", [project_name: 'p01', project_path: 'p01b'])

    //     then:
    //     id1 == id2
    // }

    // def "メトリック登録"() {
    //     when:
    //     def metric = ["value": "value01", "verify": ResultStatus.OK]
    //     cmdb_model.registMetric(1, 1, metric)
    //     def metric2 = ["value": "value02", "verify": ResultStatus.NG]
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
    //                                  platform_id: 1])
    //     def devices = [
    //         header: ['value', 'verify'],
    //         csv: [
    //             ["value01", true],
    //             ["value02", false],
    //             ["value03"],
    //         ]
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

    // def "DB登録"() {
    //     when:
    //     cmdb_model.export('src/test/resources/log/_node')

    //     then:
    //     1 == 1
    // }

    def "ノード定義のエクスポート"() {
        when:
        inventory_db.export()
        // def node = inventory_db.rows("select * from nodes")
        // def result = inventory_db.cmdb.rows("select * from test_results where node_id = 1")
        // def metric = inventory_db.cmdb.rows("select * from metrics")

        then:
        // def json = new groovy.json.JsonBuilder()
        // json(node)
        // println json.toPrettyString()
        // node[0]['node_name'].size() > 0
        // node[1]['node_name'].size() > 0
        // result.size() > 0
        1 == 1
    }

    // def "CMDBメトリック検索"() {
    //     when:
    //     cmdb_model.export(new File('src/test/resources/node/').getAbsolutePath())
    //     def result = cmdb_model.getMetricByHost('ostrich')
    //     def device_result = cmdb_model.getDeviceResultByHost('ostrich')

    //     then:
    //     result.size() > 0
    //     result[0]['platform_name'].size() > 0
    //     result[0]['node_name'].size() > 0
    //     result[0]['metric_name'].size() > 0
    //     result[0]['value'] != null

    //     device_result.size() > 0
    //     device_result[0]['platform_name'].size() > 0
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
    //     // |        "platform": "Windows",
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

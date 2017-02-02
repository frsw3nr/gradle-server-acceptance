import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import groovy.sql.Sql
import java.sql.*

// gradle --daemon clean test --tests "CMDBModelTest"

class CMDBModelTest extends Specification {

    def params = [:]
    def config
    def evidence_manager
    def cmdb_model

    def setup() {
        params = [
            getconfig_home: System.getProperty("user.dir"),
            project_home: 'src/test/resources',
            db_config: 'src/test/resources/cmdb.groovy'
        ]
        evidence_manager = new EvidenceManager(params)
        cmdb_model = CMDBModel.instance
        cmdb_model.initialize(evidence_manager)
    }

    def "DB登録"() {
        when:
        cmdb_model.export('src/test/resources/log/_node')

        then:
        1 == 1
    }

    def "マスター登録"() {
        when:
        def id = cmdb_model.registMaster("site", [site_name: 'site01'])
        def site = cmdb_model.cmdb.rows("select * from site")

        then:
        site[0]['id'] == 1
        site[0]['site_name'].size() > 0
    }

    def "マスター重複登録"() {
        when:
        def id1 = cmdb_model.registMaster("tenant", [tenant_name: '_Default'])
        def id2 = cmdb_model.registMaster("tenant", [tenant_name: '_Default'])
        def tenant = cmdb_model.cmdb.rows("select * from tenant")

        then:
        id1 == id2
        tenant.size() == 1
    }

    def "マスター登録複数列"() {
        when:
        def id = cmdb_model.registMaster("node", [node_name: 'node01', tenant_id: 1])
        def node = cmdb_model.cmdb.rows("select * from node where node_name = 'node01'")

        then:
        id > 0
        node[0]['node_name'] == 'node01'
        node[0]['tenant_id'] == 1
    }

    def "複数サイトノード登録"() {
        when:
        def site_id1      = cmdb_model.registMaster("site", [site_name: 'site01'])
        def site_id2      = cmdb_model.registMaster("site", [site_name: 'site02'])
        def node_id1      = cmdb_model.registMaster("node", [node_name: 'node01', tenant_id: 1])
        def site_node_id1 = cmdb_model.registMaster("site_node", [node_id: node_id1, site_id: site_id1])

        def node_id2      = cmdb_model.registMaster("node", [node_name: 'node01', tenant_id: 1])
        def site_node_id2 = cmdb_model.registMaster("site_node", [node_id: node_id2, site_id: site_id2])

        then:
        site_node_id1 != site_node_id2
        def sql = "select * from site_node where node_id = ? order by site_id"
        def site_node = cmdb_model.cmdb.rows(sql, node_id1)
        site_node[0]['site_id'] == site_id1
        site_node[0]['node_id'] == node_id1
        site_node[1]['site_id'] == site_id2
        site_node[1]['node_id'] == node_id1
    }

    def "マスター登録列名なし"() {
        when:
        def id = cmdb_model.registMaster("node", [HOGE: 'node01', tenant_id: 1])

        then:
        thrown(SQLException)
    }

    def "マスター登録キャッシュ"() {
        when:
        def id1 = cmdb_model.registMaster("site", [site_name: 'site01'])
        def id2 = cmdb_model.registMaster("site", [site_name: 'site01'])

        then:
        id1 == id2
    }

    def "メトリック登録"() {
        when:
        def metric = ["value": "value01", "verify": true]
        cmdb_model.registMetric(1, 1, metric)
        def metric2 = ["value": "value02", "verify": false]
        cmdb_model.registMetric(1, 2, metric2)
        def metric3 = ["value": "value03"]
        cmdb_model.registMetric(1, 3, metric3)

        def sql = "select * from test_result where node_id = ? and metric_id = ?"
        def rows = cmdb_model.cmdb.rows(sql, [1, 1])
        def rows2 = cmdb_model.cmdb.rows(sql, [1, 2])
        def rows3 = cmdb_model.cmdb.rows(sql, [1, 3])

        then:
        rows[0]['verify']  == 1
        rows2[0]['verify'] == 0
        rows3[0]['verify'] == null
    }

    def "デバイス登録"() {
        when:
        def devices = [
            ["value": "value01", "verify": true],
            ["value": "value02", "verify": false],
            ["value": "value03"],
        ]
        cmdb_model.registDevice(1, 1, devices)

        def sql = "select * from device_result where node_id = ? and metric_id = ?"
        def rows = cmdb_model.cmdb.rows(sql, [1, 1])
        println rows

        then:
        rows.size() == 5
    }

    def "ノード定義のエクスポート"() {
        when:
        cmdb_model.export(new File('src/test/resources/node/').getAbsolutePath())
        def node = cmdb_model.cmdb.rows("select * from node")
        def result = cmdb_model.cmdb.rows("select * from test_result where node_id = 1")

        then:
        node[0]['node_name'].size() > 0
        node[1]['node_name'].size() > 0
        result.size() > 0
    }

    def "CMDBメトリック検索"() {
        when:
        cmdb_model.export(new File('src/test/resources/node/').getAbsolutePath())
        def result = cmdb_model.getMetricByHost('ostrich')
        def device_result = cmdb_model.getDeviceResultByHost('ostrich')

        then:
        result.size() > 0
        result[0]['domain_name'].size() > 0
        result[0]['node_name'].size() > 0
        result[0]['metric_name'].size() > 0
        result[0]['value'] != null

        device_result.size() > 0
        device_result[0]['domain_name'].size() > 0
        device_result[0]['node_name'].size() > 0
        device_result[0]['metric_name'].size() > 0
        device_result[0]['seq'] != null
        device_result[0]['item_name'].size() > 0
        device_result[0]['value'] != null
    }
}

import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import groovy.sql.Sql
import java.sql.*

// gradle --daemon clean test --tests "ConfigManageDBTest"

class ConfigManageDBTest extends Specification {

    def params = [:]
    def config

    def setup() {
        params = [
            base_home: System.getProperty("user.dir"),
            project_home: 'src/test/resources',
            db_config: 'src/test/resources/cmdb.groovy'
        ]
    }

    def "DB接続設定ファイルなし"() {
        setup:
        this.params['db_config'] ='src/test/resources/config_db_hoge.groovy'
        def db = new ConfigManageDB(this.params)

        when:
        db.initialize()

        then:
        thrown(FileNotFoundException)
    }

    def "DB初期化"() {
        setup:
        def db = new ConfigManageDB(this.params)

        when:
        db.initialize()

        then:
        1 == 1
    }

    def "DB登録"() {
        setup:
        def db = new ConfigManageDB(this.params)

        when:
        db.export('src/test/resources/log/_node')

        then:
        1 == 1
    }

    def "マスター登録"() {
        setup:
        def db = new ConfigManageDB(this.params)
        db.initialize()

        when:
        def id = db.registMaster("SITE", [SITE_NAME: 'site01'])

        then:
        id == 1
    }

    def "マスター重複登録"() {
        setup:
        def db = new ConfigManageDB(this.params)
        db.initialize()

        when:
        def id = db.registMaster("TENANT", [TENANT_NAME: '_Default'])

        then:
        id == 1
    }

    def "マスター登録複数列"() {
        setup:
        def db = new ConfigManageDB(this.params)
        db.initialize()

        when:
        def id = db.registMaster("NODE", [NODE_NAME: 'node01', TENANT_ID: 1])

        then:
        id == 1
    }

    def "マスター登録列名なし"() {
        setup:
        def db = new ConfigManageDB(this.params)
        db.initialize()

        when:
        def id = db.registMaster("NODE", [HOGE: 'node01', TENANT_ID: 1])

        then:
        thrown(SQLException)
    }

    def "マスター登録キャッシュ"() {
        setup:
        def db = new ConfigManageDB(this.params)
        db.initialize()

        when:
        def id  = db.registMaster("SITE", [SITE_NAME: 'site01'])
        def id2 = db.registMaster("SITE", [SITE_NAME: 'site01'])

        then:
        id2 == 1
    }

    def "メトリック登録"() {
        setup:
        def db = new ConfigManageDB(this.params)
        db.initialize()

        when:
        def metric = ["value": "value01", "verify": true]
        db.registMetric(1, 1, metric)
        def metric2 = ["value": "value02", "verify": false]
        db.registMetric(1, 2, metric2)
        def metric3 = ["value": "value03"]
        db.registMetric(1, 3, metric3)

        def sql = "select * from TEST_RESULT where NODE_ID = ? and METRIC_ID = ?"
        def rows = db.cmdb.rows(sql, [1, 1])
        println rows
        def rows2 = db.cmdb.rows(sql, [1, 2])
        println rows2
        def rows3 = db.cmdb.rows(sql, [1, 3])
        println rows3

        then:
        rows[0]['VERIFY']  == 1
        rows2[0]['VERIFY'] == 0
        rows3[0]['VERIFY'] == null
    }

    def "デバイス登録"() {
        setup:
        def db = new ConfigManageDB(this.params)
        db.initialize()

        when:
        def devices = [
            ["value": "value01", "verify": true],
            ["value": "value02", "verify": false],
            ["value": "value03"],
        ]
        db.registDevice(1, 1, devices)

        def sql = "select * from DEVICE_RESULT where NODE_ID = ? and METRIC_ID = ?"
        def rows = db.cmdb.rows(sql, [1, 1])
        println rows

        then:
        rows.size() == 5
    }
}

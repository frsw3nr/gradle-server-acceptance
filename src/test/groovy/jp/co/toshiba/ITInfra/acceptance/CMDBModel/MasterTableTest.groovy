import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.CMDBModel.*
import groovy.sql.Sql
import java.sql.*

// gradle test --tests "MasterTableTest.マスター登録"

class MasterTableTest extends Specification {

    def db
    def datasource_config = [
        username : "sa",
        password : "sa",
        url      : "jdbc:h2:mem:",
        driver   : "org.h2.Driver",
    ]

    def setup() {
        db = new MasterTable(datasource_config)
    }

    def "マスター検索"() {
        when:
        db.model.initialize_data(true)
        def master = db.getByName('nodes', 'ostrich')

        then:
        master['IP'].size() > 0
    }

    def "存在しないマスター検索1"() {
        when:
        db.model.initialize_data(true)
        def master = db.getByName('nodes', 'ostrich2')

        then:
        master == []
    }

    def "存在しないマスター検索2"() {
        when:
        db.model.initialize_data(true)
        def master = db.getByName('hoge', 'ostrich2')

        then:
        thrown(SQLException)
    }

    def "存在しないマスター検索3"() {
        when:
        db.model.initialize_data(true)
        def master = db.getByName('tag_nodes', 'test1')

        then:
        thrown(SQLException)
    }

    def "モデル初期化"() {
        when:
        db.model.initialize_data(true)

        def db2 = new MasterTable()
        def node = db2.getByName('nodes', 'ostrich')

        then:
        node['IP'].size() > 0
    }

    def "マスター登録"() {
        when:
        db.model.initialize_data()
        def node_id1 = db.registMaster('nodes', [node_name: 'linux1'], [ip: '192.168.10.1'])
        def node_id2 = db.registMaster('nodes', [node_name: 'linux2'], [ip: '192.168.10.2'])
        def node_id3 = db.registMaster('nodes', [node_name: 'linux1'], [ip: '192.168.10.3'])
        def node_id4 = db.registMaster('nodes', [node_name: 'linux3'], [ip: '192.168.10.4'])

        then:
        node_id1 == node_id3
        node_id2 == node_id1 + 1
        node_id4 == node_id1 + 2
    }

    def "存在しないマスター登録"() {
        when:
        db.model.initialize_data()
        def node_id1 = db.registMaster('hoge', [node_name: 'linux1'], [ip: '192.168.10.1'])

        then:
        thrown(SQLException)
    }
}

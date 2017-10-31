import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.CMDBModel.*
import groovy.sql.Sql
import java.sql.*

// gradle test --tests "MasterTableTest.マスター登録"

class NodeTableTest extends Specification {

    def db
    def datasource_config = [
        username : "sa",
        password : "sa",
        url      : "jdbc:h2:mem:",
        driver   : "org.h2.Driver",
    ]

    def setup() {
        db = new NodeTable(datasource_config)
    }

    def "マスター検索"() {
        when:
        db.model.initialize_data(true)
        def master = db.find([group_name: 'System%', node_name: '%'], 0)
println master

        then:
        1==1
    }

}

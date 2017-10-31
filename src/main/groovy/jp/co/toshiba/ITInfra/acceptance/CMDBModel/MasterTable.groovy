package jp.co.toshiba.ITInfra.acceptance.CMDBModel

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import groovy.io.FileType
import groovy.sql.Sql
import java.sql.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
class MasterTable {
    CMDBModel model = CMDBModel.instance

    def MasterTable(Map datasource_config) {
        model = CMDBModel.instance
        model.initialize(datasource_config)
    }

    def registMaster(String table_name, Map keys, Map attributes) throws SQLException {
        def conditions = []
        def key_values = []
        keys.each { column_name, value ->
            conditions << "${column_name} = ?"
            key_values << value
        }
        def query = "select id from ${table_name} where " +
                    conditions.join(' and ')
        log.debug "QUERY: $query [$key_values]"
        def rows = model.cmdb.rows(query, key_values)
        switch (rows?.size()) {
            case 0:
                def insert_tags = []
                def insert_sets = []
                def insert_values = []
                [keys, attributes].each { columns ->
                    columns.each { column_name, value ->
                        insert_sets << column_name
                        insert_tags << '?'
                        insert_values << value
                    }
                }
                def insert_query = """\
                    |insert into ${table_name} ( ${insert_sets.join(' , ')} )
                    | values ( ${insert_tags.join(' , ')} )
                    |""".stripMargin()

                log.debug "INSERT:\n${insert_query} ${insert_values}"
                def inserted = model.cmdb.executeInsert(insert_query, insert_values)
                log.info "INSERT[${table_name}]: ${insert_values}, RC: ${inserted[0][0]}"
                return inserted[0][0] // the value of the new row's ID column
            break;

            case 1:
                def update_id = rows[0].id
                def update_sets = []
                def set_values  = []
                def update_columns = ["id = ${update_id}"]
                attributes.each { column_name, value ->
                    update_sets << "${column_name} = ?"
                    update_columns << "${column_name} = ${value}"
                    set_values << value
                }
                def update_query = """\
                    |update ${table_name} set ${update_sets.join(' , ')}
                    | where id = ?
                    |""".stripMargin()
                set_values << update_id
                log.debug "UPDATE: $update_query [${set_values}]"
                def updated = model.cmdb.executeUpdate(update_query, set_values)
                log.debug "RESULT: ${updated}"
                log.info "UPDATE[${table_name}]: ${update_columns}, RC: ${update_id}"
                return update_id
            break;

            default:
            log.debug "UNKOWN"
            break;
        }
    }

    def getByName(String table_name, String key_name) throws SQLException {
        def alias
        (table_name =~ /^(.+?)s$/).each {m0,m1->
            alias = m1
        }
        if (!alias) {
            throw new SQLException(
                "The last character of table name must contain 's' : ${table_name}")
        }

        def query = "select * from ${table_name} where ${alias}_name = ?"
        log.info "QUERY:table=${table_name} key_name=${key_name}"
        model.cmdb.rows(query, key_name)
    }

    def getAll(String table_name, Map conditions, int page = 0) throws SQLException {

    }
}

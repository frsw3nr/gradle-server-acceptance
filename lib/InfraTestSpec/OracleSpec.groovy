package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import groovy.json.*
import groovy.sql.Sql
import java.sql.SQLException
import com.xlson.groovycsv.CsvParser
import oracle.jdbc.*
import oracle.jdbc.pool.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

@GrabConfig( systemClassLoader=true )

@Slf4j
@InheritConstructors
class OracleSpec extends InfraTestSpec {

    String oracle_ip
    String oracle_user
    String oracle_password
    String oracle_db
    String target_server
    int    oracle_port
    int    timeout = 30
    def host_ids = [:]
    def hostnames = [:]
    def db

    def init() {
        super.init()

        def test_target = test_platform?.test_target

        def os_account       = test_platform.os_account
        this.oracle_ip       = test_target.ip
        this.target_server   = test_target.name
        this.oracle_db       = test_target.remote_alias
        this.oracle_port     = os_account['port']
        this.oracle_user     = os_account['user']
        this.oracle_password = os_account['password']
        this.timeout         = test_platform.timeout
    }

    def finish() {
        super.finish()
    }

    def setup_exec(TestItem[] test_items) {

        def result
        if (!dry_run) {

            URLClassLoader loader = GroovyObject.class.classLoader
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver")
                def url = "jdbc:oracle:thin:@${oracle_ip}:${oracle_port}:${oracle_db}"
                db = Sql.newInstance(url, oracle_user, oracle_password)

            } catch (SQLException e) {
                def msg = "Oracle connection error : "
                throw new SQLException(msg + e)
            }
        }
        test_items.each {
            def method = this.metaClass.getMetaMethod(it.test_id, TestItem)
            if (method) {
                log.debug "Invoke command '${method.name}()'"
                try {
                    long start = System.currentTimeMillis();
                    method.invoke(this, it)
                    long elapsed = System.currentTimeMillis() - start
                    log.debug "Finish test method '${method.name}()' in ${this.server_name}, Elapsed : ${elapsed} ms"
                    // it.succeed = 1
                } catch (Exception e) {
                    it.verify(false)
                    log.error "[Oracle Test] Test method '${method.name}()' faild, skip.\n" + e
                }
            }
        }
    }

    def rows_to_csv(List rows, List header = null) {
        println "ROWS:$rows"
        def header_keys = [:]
        def csv = []
        rows.each { row ->
            def list = []
            row.each { column_name, value ->
                list << value
                if (!header_keys.containsKey(column_name))
                    header_keys[column_name] = true
            }
            csv << list
        }
        def headers = header_keys.keySet()
        if (header)
            headers = header
        def text = "${headers.join(',')}\n"
        csv.each { line ->
            text += "${line.join(',')}\n"
        }
        return text
    }

    def parse_csv(String lines) {
        def rownum = 1
        def header = []
        def csv = []
        lines.eachLine { line ->
            def arr = line.split(',')
            if (rownum == 1)
                header = arr
            else
                csv << arr
            rownum ++
        }
        return [header, csv]
    }

    def cdbstorage(test_item) {
        def lines = exec('cdbstorage') {
            def query = '''\
            |WITH x AS (  SELECT c1.con_id
            |                  , cf1.tablespace_name
            |                  , SUM(cf1.bytes)/1024/1024 fsm
            |               FROM cdb_free_space cf1
            |                  , v$containers   c1
            |              WHERE cf1.con_id = c1.con_id
            |           GROUP BY c1.con_id
            |                  , cf1.tablespace_name
            |          ),
            |     y AS (  SELECT c2.con_id
            |                  , cd.tablespace_name
            |                  , SUM(cd.bytes)/1024/1024 apm
            |               FROM cdb_data_files cd
            |                  , v$containers   c2
            |              WHERE cd.con_id = c2.con_id
            |           GROUP BY c2.con_id
            |                  , cd.tablespace_name
            |          )
            |   SELECT x.con_id
            |        , v.name con_name
            |        , x.tablespace_name
            |        , x.fsm
            |        , y.apm
            |     FROM x
            |        , y
            |        , v$containers v
            |    WHERE x.con_id = y.con_id
            |      AND   x.tablespace_name = y.tablespace_name
            |      AND   v.con_id = y.con_id
            | UNION
            |   SELECT vc2.con_id
            |        , vc2.name
            |        , tf.tablespace_name
            |        , null
            |        , SUM(tf.bytes)/1024/1024
            |     FROM v$containers vc2
            |        , cdb_temp_files tf
            |    WHERE vc2.con_id = tf.con_id
            | GROUP BY vc2.con_id
            |        , vc2.name
            |        , tf.tablespace_name
            | ORDER BY con_id
            |        , con_name
            '''
            def rows = db.rows(query.stripMargin())
            def header = ['ID','Cont. Name','Tablespace','Free Space MB',
                          'Alloc Space MB']
            def text = rows_to_csv(rows, header)
            new File("${local_dir}/cdbstorage").text = text
            return text
        }
        def info = [:]
        def rownum = 1
        List header
        List csv
        (header, csv) = parse_csv(lines)
        println "($header, $csv)\n";
        csv.each { arr ->
            info[arr[2]] = arr[4]
        }
        println "HEADER: $header"
        println "CSV: $csv"
        println "INFO: $info"
        test_item.devices(csv, header)
        test_item.results(info.toString())
    }

    def dbattrs(test_item) {
        def lines = exec('dbattrs') {
            def query = '''\
            |SELECT *
            |  FROM v$database
            '''
            def rows = db.rows(query.stripMargin())
            def pivot = []
            rows.each { row ->
                row.each { column_name, value ->
                    pivot << [ name: column_name, value: value]
                }
            }
            // def header = ['ID','Cont. Name','Tablespace','Free Space MB',
            //               'Alloc Space MB']
            // def text = rows_to_csv(rows, header)
            def text = rows_to_csv(pivot)
            new File("${local_dir}/dbattrs").text = text
            return text
        }
        def info = [:]
        def rownum = 1
        List header
        List csv
        (header, csv) = parse_csv(lines)
        csv.each { arr ->
            info["dbattrs.${arr[0].toLowerCase()}"] = arr[1]
        }
        println "HEADER: $header"
        println "CSV: $csv"
        println "INFO: $info"
        test_item.devices(csv, header)
        test_item.results(info)
    }

    def dbcomps(test_item) {
        def lines = exec('dbcomps') {
            def query = '''\
            |SELECT comp_name
            |     , version
            |     , status
            |  FROM dba_registry
            | ORDER BY comp_name
            '''
            def rows = db.rows(query.stripMargin())
            def header = ['Name', 'Version', 'Status']
            def text = rows_to_csv(rows, header)
            new File("${local_dir}/dbcomps").text = text
            return text
        }
        def info = [:]
        def rownum = 1
        List header
        List csv
        (header, csv) = parse_csv(lines)
        csv.each { arr ->
            info[arr[1]] = true
        }
        println "HEADER: $header"
        println "CSV: $csv"
        println "INFO: $info"
        test_item.devices(csv, header)
        test_item.results("${info.keySet()}")
    }

    def dbfeatusage(test_item) {
        def lines = exec('dbfeatusage') {
            def query = '''\
            |SELECT u1.name
            |     , u1.detected_usages
            |     , INITCAP(u1.currently_used) currently_used
            |     , u1.version
            |  FROM dba_feature_usage_statistics u1
            | WHERE version = (SELECT MAX(u2.version)
            |                    FROM dba_feature_usage_statistics u2
            |                   WHERE u2.name = u1.name
            |                 )
            |   AND u1.detected_usages > 0
            |   AND u1.dbid = (SELECT dbid FROM v$database)
            '''
            def rows = db.rows(query.stripMargin())
            def header = ['Name','Usage', 'Count Currently Used', 'Version']
            def text = rows_to_csv(rows, header)
            new File("${local_dir}/dbfeatusage").text = text
            return text
        }
        def info = [:]
        List header
        List csv
        (header, csv) = parse_csv(lines)
        csv.each { arr ->
            if (arr[2] == "True")
                return
            info[arr[0]] = arr[2]
        }
        println "HEADER: $header"
        println "CSV: $csv"
        println "INFO: $info"
        test_item.devices(csv, header)
        test_item.results(info.toString())
    }

    def dbinfo(test_item) {
        def lines = exec('dbinfo') {
            def query = '''\
            |SELECT a.NAME,a.VALUE from v$parameter a 
            '''
            def rows = db.rows(query.stripMargin())
            def header = ['Name','Value']
            def text = rows_to_csv(rows, header)
            new File("${local_dir}/dbinfo").text = text
            return text
        }
        def info = [:]
        List header
        List csv
        (header, csv) = parse_csv(lines)
        csv.each { arr ->
            info["dbinfo.${arr[0]}"] = arr[1]
        }
        println "HEADER: $header"
        println "CSV: $csv"
        println "INFO: $info"
        test_item.devices(csv, header)
        test_item.results(info)
    }

    def dbvers(test_item) {
        def lines = exec('dbvers') {
            def query = '''\
            |SELECT product
            |     , version
            |     , status
            |  FROM product_component_version
            | ORDER BY product
            '''
            def rows = db.rows(query.stripMargin())
            def text = rows_to_csv(rows)
            new File("${local_dir}/dbvers").text = text
            return text
        }
        def info = [:]
        List header
        List csv
        (header, csv) = parse_csv(lines)
        csv.each { arr ->
            (arr[0]=~/Oracle Database/).each { m0 ->
                info[arr.join(' ')] = true
            }
        }
        println "HEADER: $header"
        println "CSV: $csv"
        println "INFO: $info"
        test_item.devices(csv, header)
        test_item.results("${info.keySet()}")
    }

    def nls(test_item) {
        def lines = exec('nls') {
            def query = '''\
            |SELECT   name
            |       , '"'||value$||'"' value
            |       , comment$ as "comment"
            |    FROM sys.props$
            |   WHERE upper(name) LIKE 'NLS%'
            |ORDER BY name
            |       , value
            '''
            def rows = db.rows(query.stripMargin())
            def text = rows_to_csv(rows)
            new File("${local_dir}/nls").text = text
            return text
        }
        // def data = new CsvParser().parse(lines, separator: ',', quoteChar: '"')
        // println "DATA:$data"
        def info = [:]
        List header
        List csv
        (header, csv) = parse_csv(lines)
        csv.each { arr ->
            (arr[0]=~/NLS_CHARACTERSET/).each { m0 ->
                info[arr[0]] = arr[1]
            }
        }
        println "HEADER: $header"
        println "CSV: $csv"
        println "INFO: $info"
        test_item.devices(csv, header)
        test_item.results("${info}")
    }

    def parmdef(test_item) {
        def lines = exec('parmdef') {
            def query = '''\
            |SELECT   name
            |       , value
            |       , description
            |    FROM v$parameter
            |ORDER BY name
            |       , value
            |       , description
            '''
            def rows = db.rows(query.stripMargin())
            def text = rows_to_csv(rows)
            new File("${local_dir}/parmdef").text = text
            return text
        }
        // def data = new CsvParser().parse(lines, separator: ',', quoteChar: '"')
        // println "DATA:$data"
        def info = [:]
        List header
        List csv
        (header, csv) = parse_csv(lines)
        csv.each { arr ->
            info["parmdef.${arr[0]}"] = arr[1]
        }
        println "HEADER: $header"
        println "CSV: $csv"
        println "INFO: $info"
        test_item.devices(csv, header)
        test_item.results("${info}")
    }

    def redoinfo(test_item) {
        def lines = exec('redoinfo') {
            def query = '''\
            |SELECT   group#
            |       , thread#
            |       , bytes
            |       , status
            |       , archived
            |       , first_change#
            |       , to_char(first_time, 'yyyy-mm-dd hh24:mi:ss') first_time
            |       , next_change#
            |       , to_char(next_time, 'yyyy-mm-dd hh24:mi:ss') next_time
            |    FROM v$log
            '''
            def rows = db.rows(query.stripMargin())
            def text = rows_to_csv(rows)
            new File("${local_dir}/redoinfo").text = text
            return text
        }
        // def data = new CsvParser().parse(lines, separator: ',', quoteChar: '"')
        // println "DATA:$data"
        def info = [:]
        List header
        List csv
        (header, csv) = parse_csv(lines)
        csv.each { arr ->
            (arr[0]=~/dump_dest/).each { m0 ->
                info["redoinfo.${arr[0]}"] = arr[1]
            }
        }
        println "HEADER: $header"
        println "CSV: $csv"
        println "INFO: $info"
        test_item.devices(csv, header)
        test_item.results(info)
    }

    def sgasize(test_item) {
        def lines = exec('sgasize') {


            def content = ''
            new File("${local_dir}/sgasize").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def sysmetric(test_item) {
        def lines = exec('sysmetric') {


            def content = ''
            new File("${local_dir}/sysmetric").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def systime(test_item) {
        def lines = exec('systime') {


            def content = ''
            new File("${local_dir}/systime").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }

    def tabstorage(test_item) {
        def lines = exec('tabstorage') {


            def content = ''
            new File("${local_dir}/tabstorage").text = content
            return content
        }
        def headers = []
        def csv = []
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
    }
}

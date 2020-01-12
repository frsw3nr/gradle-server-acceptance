package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import groovy.json.*
import groovy.sql.Sql
import java.sql.SQLException
import java.sql.SQLSyntaxErrorException
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
        this.timeout         = test_platform.timeout
        this.oracle_user     = test_target.specific_user ?: os_account['user']
        this.oracle_password = test_target.specific_password ?: os_account['password']
        this.oracle_port     = os_account['port']
        if (test_target.specific_port)
            this.oracle_port = (int)(NumberUtils.toDouble(test_target.specific_port))
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
                log.info "Connect: $url"
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
        if (!dry_run) {
            try {
                db.close()

            } catch (SQLException e) {
                def msg = "Oracle connection error : "
                throw new SQLException(msg + e)
            }
        }
    }

    def rapup_test(test_item, info, csv, header) {
        def test_id = test_item.test_id
        info[test_id] = (csv.size() > 0) ? 'OK' : 'NG'
        // println "HEADER: $header"
        // println "CSV: $csv"
        // println "INFO: $info"
        test_item.devices(csv, header)
        test_item.results(info)
        test_item.verify_text_search(test_id, info[test_id])
    }

    def dbstorage(test_item) {
        def lines = exec('dbstorage') {
            def query = '''\
            |select 
            |  tablespace_name, 
            |  nvl(total_bytes / 1024 / 1024, 0) as "size(MB)",
            |  nvl((total_bytes - free_total_bytes) / 1024 / 1024, 0) as "used(MB)", 
            |  nvl(free_total_bytes / 1024 / 1024, 0) as "free(MB)", 
            |  nvl((total_bytes - free_total_bytes) / total_bytes * 100, 100) as "rate(%)" 
            |from 
            |  (
            |    select 
            |      tablespace_name, 
            |      sum(bytes) total_bytes 
            |    from 
            |      dba_data_files 
            |    group by 
            |      tablespace_name
            |  ), 
            |  (
            |    select 
            |      tablespace_name free_tablespace_name, 
            |      sum(bytes) free_total_bytes 
            |    from 
            |      dba_free_space 
            |    group by 
            |      tablespace_name
            |  ) 
            |where 
            |  tablespace_name = free_tablespace_name(+)
            '''
            def rows = db.rows(query.stripMargin())
            def header = ['Tablespace','Size MB', 'Alloc Space MB', 'Free Space MB',
                          'Rate %']
            def text = test_item.sql_rows_to_csv(rows, header)
            new File("${local_dir}/dbstorage").text = text
            return text
        }
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        csv.each { arr ->
            info[arr[0]] = arr[4]
        }
        rapup_test(test_item, info, csv, header)
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
            def text = test_item.sql_rows_to_csv(pivot)
            new File("${local_dir}/dbattrs").text = text
            return text
        }
        def info = [:]
        def rownum = 1
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        csv.each { arr ->
            info["dbattrs.${arr[0].toLowerCase()}"] = arr[1]
        }
        rapup_test(test_item, info, csv, header)
    }

    def dbinstance(test_item) {
        def lines = exec('dbattrs') {
            def query = '''\
            |SELECT *
            |  FROM v$instance
            '''
            def rows = db.rows(query.stripMargin())
            def pivot = []
            rows.each { row ->
                row.each { column_name, value ->
                    pivot << [ name: column_name, value: value]
                }
            }
            def text = test_item.sql_rows_to_csv(pivot)
            new File("${local_dir}/dbinstance").text = text
            return text
        }
        def info = [:]
        def rownum = 1
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        csv.each { arr ->
            String name, value
            (name, value) = arr
            name = name.toLowerCase()
            info["dbinstance.${name}"] = value
        }
        rapup_test(test_item, info, csv, header)
    }

    def hostconfig(test_item) {
        def lines = exec('hostconfig') {
            def query = '''\
            |SELECT LOWER(stat_name),
            |       value
            |  FROM v$osstat
            | WHERE LOWER(stat_name) in ('physical_memory_bytes','num_cpu_cores','num_cpus','num_cpu_sockets')
            '''
            def rows = db.rows(query.stripMargin())
            def header = ['Name', 'Value']
            def text = test_item.sql_rows_to_csv(rows, header)
            new File("${local_dir}/hostconfig").text = text
            return text
        }
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        def registry_versions = [:]
        csv.each { arr ->
            String name, value
            (name, value) = arr
            if (name == 'physical_memory_bytes') {
                def memory_mb = NumberUtils.toDouble(value) / (1024 * 1024)
                info["hostconfig.physical_memory_mb"] = memory_mb.round(0)
            }
            info["hostconfig.${name}"] = value
        }
        rapup_test(test_item, info, csv, header)
    }

    def dbregistry(test_item) {
        def lines = exec('dbregistry') {
            def query = '''\
            |SELECT comp_name
            |     , version
            |     , status
            |  FROM dba_registry
            | ORDER BY comp_name
            '''
            def rows = db.rows(query.stripMargin())
            def header = ['Name', 'Version', 'Status']
            def text = test_item.sql_rows_to_csv(rows, header)
            new File("${local_dir}/dbregistry").text = text
            return text
        }
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        def registry_versions = [:]
        csv.each { arr ->
            String comp_name, version, status
            (comp_name, version, status) = arr
            info["dbregistry.${comp_name}"] = status
            registry_versions[version] = true
        }
        info['dbregistry.version'] = registry_versions.keySet().join(',')
        rapup_test(test_item, info, csv, header)
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
            def text = test_item.sql_rows_to_csv(rows, header)
            new File("${local_dir}/dbcomps").text = text
            return text
        }
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        def registry_versions = [:]
        def res = [:]
        csv.each { arr ->
            String comp_name, version, status
            (comp_name, version, status) = arr
            info["dbcomps.${comp_name}"] = status
            def id = "${comp_name}.${version}"
            add_new_metric("dbcomps.${id}", "機能:${id}", status, res)
            registry_versions[version] = true
        }
        info['dbcomps.version'] = registry_versions.keySet().join(',')
        rapup_test(test_item, info, csv, header)
        test_item.results(res)
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
            def text = test_item.sql_rows_to_csv(rows, header)
            new File("${local_dir}/dbfeatusage").text = text
            return text
        }
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        csv.each { arr ->
            String name, detected_usages, currently_used, version
            (name, detected_usages, currently_used, version) = arr
            info["dbfeatusage.${name}"] = currently_used
        }
        rapup_test(test_item, info, csv, header)
    }

    def check_memory_management(Map info){
        def memory_max_target = NumberUtils.toDouble(info["dbinfo.memory_max_target"])
        def memory_target     = NumberUtils.toDouble(info["dbinfo.memory_target"])
        def sga_target        = NumberUtils.toDouble(info["dbinfo.sga_target"])
        def statistics_level  = info['dbinfo.statistics_level'].toLowerCase()

        if (statistics_level) {
            if (memory_max_target > 0 && memory_target > 0) {
                return 'AMM'
            } else if (sga_target > 0 && (statistics_level == 'typical' ||
                       statistics_level == 'all')) {
                return 'ASMM'
            } else {
                return 'None'
            }
        } else {
            return 'unkown'
        }
    }

    def dbinfo(test_item) {
        def lines = exec('dbinfo') {
            def query = '''\
            |SELECT a.NAME,a.VALUE from v$parameter a 
            '''
            def rows = db.rows(query.stripMargin())
            def header = ['Name','Value']
            def text = test_item.sql_rows_to_csv(rows, header)
            new File("${local_dir}/dbinfo").text = text
            return text
        }
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        csv.each { arr ->
            String name, value
            (name, value) = arr
            info["dbinfo.${name}"] = value
        }
        info["dbinfo.memory_management"] = check_memory_management(info)

        rapup_test(test_item, info, csv, header)
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
            def text = test_item.sql_rows_to_csv(rows)
            new File("${local_dir}/dbvers").text = text
            return text
        }
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        csv.each { arr ->
            String product, version, status
            (product, version, status) = arr
            (product=~/Oracle Database/).each {
                info['dbvers.Oracle Database'] = "$product $version $status"
            }
        }
        rapup_test(test_item, info, csv, header)
    }

    def nls(test_item) {
        def lines = exec('nls') {
            def rows
            def query = '''\
            |SELECT   LOWER(name)
            |       , value$ value
            |       , comment$ as "comment"
            |    FROM sys.props$
            |   WHERE upper(name) LIKE 'NLS%'
            |      OR upper(name) LIKE 'DEFAULT%'
            |ORDER BY name
            |       , value
            '''
            def is_succeed = false
            try {
                rows = db.rows(query.stripMargin())
                is_succeed = true
            } catch (SQLException e) {
                log.info "'sys.props\$' NLS check query failed, Retry the following query."
            }
            if (!is_succeed) {
                def query2 = '''\
                |SELECT   LOWER(PARAMETER)
                |       , VALUE
                |       , '' as "comment"
                |    FROM v$nls_parameters
                '''
                rows = db.rows(query2.stripMargin())
            }
            def text = test_item.sql_rows_to_csv(rows)
            new File("${local_dir}/nls").text = text
            return text
        }
        // def data = new CsvParser().parse(lines, separator: ',', quoteChar: '"')
        // println "DATA:$data"
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        csv.each { arr ->
            String name, value, comment
            (name, value, comment) = arr
            info["nls.${name}"] = value
        }
        rapup_test(test_item, info, csv, header)
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
            def text = test_item.sql_rows_to_csv(rows)
            new File("${local_dir}/parmdef").text = text
            return text
        }
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        csv.each { arr ->
            String name, value, description
            (name, value, description) = arr
            info["parmdef.${name}"] = value
        }
        rapup_test(test_item, info, csv, header)
    }

    def redoinfo(test_item) {
        def lines = exec('redoinfo') {
            def query = '''\
            |SELECT   group#
            |       , thread#
            |       , members
            |       , bytes
            |       , status
            |       , archived
            |       , first_change#
            |       , to_char(first_time, 'yyyy-mm-dd hh24:mi:ss') first_time
            |    FROM v$log
            '''
            def rows = db.rows(query.stripMargin())
            def text = test_item.sql_rows_to_csv(rows)
            new File("${local_dir}/redoinfo").text = text
            return text
        }
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        String redo_size
        int redo_count = 0
        def is_mirror = false
        csv.each { arr ->
            String group, thread, members, bytes, status, archived
            (group, thread, members, bytes, status, archived) = arr
            if (NumberUtils.toDouble(members) > 1)
                is_mirror = true
            redo_size = bytes
            redo_count ++
        }
        info["redoinfo.redo_size"]  = redo_size
        info["redoinfo.redo_count"] = redo_count
        info["redoinfo.redo_mirror"]  = is_mirror
        rapup_test(test_item, info, csv, header)
    }

    def sgasize(test_item) {
        def lines = exec('sgasize') {
            def query = '''\
            |SELECT   name
            |       , value
            |    FROM v$sga
            |ORDER BY name
            '''
            def rows = db.rows(query.stripMargin())
            def text = test_item.sql_rows_to_csv(rows)
            new File("${local_dir}/sgasize").text = text
            return text
        }
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        csv.each { arr ->
            String name, value
            (name, value) = arr
            info["sgasize.${name}"] = value
        }
        rapup_test(test_item, info, csv, header)
    }

    def sysmetric(test_item) {
        def lines = exec('sysmetric') {
            def query = '''\
            |SELECT   TO_CHAR(begin_time , 'YYYY-MM-DD HH24:MI:SS') begin_time
            |       , TO_CHAR(end_time   , 'YYYY-MM-DD HH24:MI:SS') end_time
            |--       , intsize_csec
            |--       , group_id
            |--       , metric_id
            |       , value
            |       , metric_name
            |--       , metric_unit
            |    FROM v$sysmetric
            |   WHERE group_id = 2
            |ORDER BY begin_time
            |       , metric_name
            '''

            def rows = db.rows(query.stripMargin())
            def text = test_item.sql_rows_to_csv(rows)
            new File("${local_dir}/sysmetric").text = text
            return text
        }
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        csv.each { arr ->
            String begin_time, end_time, value, metric_name
            (begin_time, end_time, value, metric_name) = arr
            info["sysmetric.${metric_name}"] = value
        }
        rapup_test(test_item, info, csv, header)
    }

    def systime(test_item) {
        def lines = exec('sysmetric') {
            def query = '''\
            |SELECT  LPAD(' ', 2*level-1)||stat_name stat_name
            |      , ROUND(value/1000000,2) seconds
            |      , ROUND(value/1000000/60,2) minutes
            |   FROM (select 0 id, 9 pid, null stat_name, null value from dual
            |          UNION
            |         SELECT DECODE(stat_name,'DB time',10) id
            |              , DECODE(stat_name,'DB time',0) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'DB time'
            |          UNION
            |         SELECT DECODE(stat_name,'DB CPU',20) id
            |              , DECODE(stat_name,'DB CPU',10) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'DB CPU'
            |          UNION
            |         SELECT DECODE(stat_name,'connection management call elapsed time',21) id
            |              , DECODE(stat_name,'connection management call elapsed time',10) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'connection management call elapsed time'
            |          UNION
            |         SELECT DECODE(stat_name,'sequence load elapsed time',22) id
            |              , DECODE(stat_name,'sequence load elapsed time',10) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'sequence load elapsed time'
            |          UNION
            |         SELECT DECODE(stat_name,'sql execute elapsed time',23) id
            |              , DECODE(stat_name,'sql execute elapsed time',10) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'sql execute elapsed time'
            |          UNION
            |         SELECT DECODE(stat_name,'parse time elapsed',24) id
            |              , DECODE(stat_name,'parse time elapsed',10) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'parse time elapsed'
            |          UNION
            |         SELECT DECODE(stat_name,'hard parse elapsed time',30) id
            |              , DECODE(stat_name,'hard parse elapsed time',24) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'hard parse elapsed time'
            |          UNION
            |         SELECT DECODE(stat_name,'hard parse (sharing criteria) elapsed time',40) id
            |              , DECODE(stat_name,'hard parse (sharing criteria) elapsed time',30) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'hard parse (sharing criteria) elapsed time'
            |          UNION
            |         SELECT DECODE(stat_name,'hard parse (bind mismatch) elapsed time',50) id
            |              , DECODE(stat_name,'hard parse (bind mismatch) elapsed time',40) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'hard parse (bind mismatch) elapsed time'
            |          UNION
            |         SELECT DECODE(stat_name,'failed parse elapsed time',31) id
            |              , DECODE(stat_name,'failed parse elapsed time',24) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'failed parse elapsed time'
            |          UNION
            |         SELECT DECODE(stat_name,'failed parse (out of shared memory) elapsed time',41) id
            |              , DECODE(stat_name,'failed parse (out of shared memory) elapsed time',31) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'failed parse (out of shared memory) elapsed time'
            |          UNION
            |         SELECT DECODE(stat_name,'PL/SQL execution elapsed time',25) id
            |              , DECODE(stat_name,'PL/SQL execution elapsed time',10) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'PL/SQL execution elapsed time'
            |          UNION
            |         SELECT DECODE(stat_name,'inbound PL/SQL rpc elapsed time',26) id
            |              , DECODE(stat_name,'inbound PL/SQL rpc elapsed time',10) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'inbound PL/SQL rpc elapsed time'
            |          UNION
            |         SELECT DECODE(stat_name,'PL/SQL compilation elapsed time',27) id
            |              , DECODE(stat_name,'PL/SQL compilation elapsed time',10) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'PL/SQL compilation elapsed time'
            |          UNION
            |         SELECT DECODE(stat_name,'Java execution elapsed time',28) id
            |              , DECODE(stat_name,'Java execution elapsed time',10) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'Java execution elapsed time'
            |          UNION
            |         SELECT DECODE(stat_name,'repeated bind elapsed time',29) id
            |              , DECODE(stat_name,'repeated bind elapsed time',10) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'repeated bind elapsed time'
            |          UNION
            |         SELECT DECODE(stat_name,'background elapsed time',1) id
            |              , DECODE(stat_name,'background elapsed time',0) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'background elapsed time'
            |          UNION
            |         SELECT DECODE(stat_name,'background cpu time',2) id
            |              , DECODE(stat_name,'background cpu time',1) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'background cpu time'
            |          UNION
            |         SELECT DECODE(stat_name,'RMAN cpu time (backup/restore)',3) id
            |              , DECODE(stat_name,'RMAN cpu time (backup/restore)',2) pid
            |              , stat_name
            |              , value
            |           FROM v$sys_time_model
            |          WHERE stat_name = 'RMAN cpu time (backup/restore)'
            |        )
            |CONNECT BY PRIOR id = pid START WITH id = 0
            '''

            def rows = db.rows(query.stripMargin())
            def text = test_item.sql_rows_to_csv(rows)
            new File("${local_dir}/systime").text = text
            return text
        }
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        csv.each { arr ->
            String stat_name, seconds, minutes
            (stat_name, seconds, minutes) = arr
            info["systime.${stat_name}"] = seconds
        }
        rapup_test(test_item, info, csv, header)
    }

    def tabstorage(test_item) {
        def lines = exec('tabstorage') {
            def query = '''\
            |SELECT   tab.owner owner
            |       , tab.table_name table_name
            |       , ROUND(SUM(seg.bytes/1024/1024),3) mbytes
            |    FROM (SELECT owner
            |               , segment_name
            |               , bytes
            |            FROM dba_segments
            |           WHERE 1=1
            |         ) seg
            |       , dba_tables tab
            |   WHERE seg.owner = tab.owner
            |     AND seg.segment_name = tab.table_name
            |GROUP BY tab.owner
            |       , tab.table_name
            |ORDER BY tab.owner
            |       , tab.table_name
            '''
            def rows = db.rows(query.stripMargin())
            def text = test_item.sql_rows_to_csv(rows)
            new File("${local_dir}/tabstorage").text = text
            return text
        }
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        csv.each { arr ->
            String owner, table_name, mbytes
            (owner, table_name, mbytes) = arr
            info["tabstorage.${table_name}"] = mbytes
        }
        rapup_test(test_item, info, csv, header)
    }

    def sumstorage(test_item) {
        def lines = exec('sumstorage') {
            def query = '''\
            |select 'datafiles' type, sum(bytes)/1024/1024 total from dba_data_files
            | union
            |select 'tempfiles' type, sum(bytes)/1024/1024 total from dba_temp_files
            | union
            |select 'redologs' type, sum(bytes)/1024/1024 total from v$log
            | union
            |select 'controlfiles' type, sum(block_size*file_size_blks)/1024/1024 total from v$controlfile
            '''
            def rows = db.rows(query.stripMargin())
            def text = test_item.sql_rows_to_csv(rows)
            new File("${local_dir}/sumstorage").text = text
            return text
        }
        def info = [:]
        List header, csv
        (header, csv) = test_item.parse_csv(lines)
        csv.each { arr ->
            String type, total
            (type, total) = arr
            info["sumstorage.${type}"] = total
        }
        rapup_test(test_item, info, csv, header)
    }
}

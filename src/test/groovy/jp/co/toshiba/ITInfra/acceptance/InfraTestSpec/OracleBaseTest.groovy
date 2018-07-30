import spock.lang.Specification
import static groovy.json.JsonOutput.*
import groovy.json.*
import groovy.sql.Sql
import java.sql.SQLException
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// @GrabConfig( systemClassLoader=true )

// gradle --daemon test --tests "OracleBaseTest.ダミーテスト"

class OracleBaseTest extends Specification {

    // def db

    // def setup() {
    //     // db = Sql.newInstance("jdbc:oracle:thin:@192.168.0.16:1521:orcl", 
    //     //                      "zabbix", "zabbix")
    //     db = Sql.newInstance("jdbc:oracle:thin:@hatest02:1521:orcl", 
    //                          "zabbix", "zabbix")
    // }

    def ダミーテスト() {
        when:
        println 'Test'

        then:
        1 == 1
    }

    // def ダミーテスト2() {
    //     when:
    //     println 'Test'

    //     def query = '''
    //     |select * from tab
    //     '''
    //     query = query.stripMargin()
    //     def rows = db.rows(query)
    //     println "ROWS:$rows"

    //     then:
    //     1 == 1
    // }

    // def print_json(result_info) {
    //     def json = JsonOutput.toJson(result_info)
    //     println JsonOutput.prettyPrint(json)
    // }

    // def cdbstorage() {
    //     when:
    //     def query = '''\
    //     |WITH x AS (  SELECT c1.con_id
    //     |                  , cf1.tablespace_name
    //     |                  , SUM(cf1.bytes)/1024/1024 fsm
    //     |               FROM cdb_free_space cf1
    //     |                  , v$containers   c1
    //     |              WHERE cf1.con_id = c1.con_id
    //     |           GROUP BY c1.con_id
    //     |                  , cf1.tablespace_name
    //     |          ),
    //     |     y AS (  SELECT c2.con_id
    //     |                  , cd.tablespace_name
    //     |                  , SUM(cd.bytes)/1024/1024 apm
    //     |               FROM cdb_data_files cd
    //     |                  , v$containers   c2
    //     |              WHERE cd.con_id = c2.con_id
    //     |           GROUP BY c2.con_id
    //     |                  , cd.tablespace_name
    //     |          )
    //     |   SELECT x.con_id
    //     |        , v.name con_name
    //     |        , x.tablespace_name
    //     |        , x.fsm
    //     |        , y.apm
    //     |     FROM x
    //     |        , y
    //     |        , v$containers v
    //     |    WHERE x.con_id = y.con_id
    //     |      AND   x.tablespace_name = y.tablespace_name
    //     |      AND   v.con_id = y.con_id
    //     | UNION
    //     |   SELECT vc2.con_id
    //     |        , vc2.name
    //     |        , tf.tablespace_name
    //     |        , null
    //     |        , SUM(tf.bytes)/1024/1024
    //     |     FROM v$containers vc2
    //     |        , cdb_temp_files tf
    //     |    WHERE vc2.con_id = tf.con_id
    //     | GROUP BY vc2.con_id
    //     |        , vc2.name
    //     |        , tf.tablespace_name
    //     | ORDER BY con_id
    //     |        , con_name
    //     '''
    //     query = query.stripMargin()
    //     println query
    //     def rows = db.rows(query)
    //     print_json(rows)

    //     then:
    //     1 == 1
    // }

    // TargetServer test_server
    // DomainTestRunner test

    // def setup() {
    //     test_server = new TargetServer(
    //         server_name       : 'localhost',
    //         ip                : '127.0.0.1',
    //         platform          : 'Oracle',
    //         os_account_id     : 'Test',
    //         remote_alias      : 'localhost',
    //     )
    //     test_server.setAccounts('src/test/resources/config_Oracle.groovy')
    //     test_server.dry_run = true
    // }

    // def "Oracle 認証"() {
    //     setup:

    //     when:

    //     JSONObject mainJObj = new JSONObject();
    //     JSONObject paramJObj = new JSONObject();

    //     mainJObj.put("jsonrpc", "2.0");
    //     mainJObj.put("method", "user.login");

    //     paramJObj.put("user", "Admin");
    //     paramJObj.put("password", "getperf");

    //     mainJObj.put("params", paramJObj);
    //     mainJObj.put("id", "1");

    //     Webb webb = Webb.create();

    //     System.out.println("Data to send: " + mainJObj.toString());

    //     JSONObject result = webb.post("http://localhost/Oracle/api_jsonrpc.php")
    //                                 .header("Content-Type", "application/json")
    //                                 .useCaches(false)
    //                                 .body(mainJObj)
    //                                 .ensureSuccess()
    //                                 .asJsonObject()
    //                                 .getBody();

    //     System.out.println("Authentication token: " + result.getString("result"));

    //     then:
    //     1 == 1
    // }

    // def "subnet"() {
    //     setup:
    //     when:
    //     String subnet = "192.168.0.0/16";
    //     SubnetUtils subnetUtils = new SubnetUtils(subnet);
    //     SubnetInfo subnetInfo = subnetUtils.getInfo();

    //     System.out.println("サブネット : " + subnet);
    //     System.out.println("下限 : " + subnetInfo.getLowAddress());
    //     System.out.println("上限 : " + subnetInfo.getHighAddress());

    //     then:
    //     subnetInfo.getNetmask() == '255.255.0.0'
    // }

    // def "subnet_ng"() {
    //     setup:
    //     when:
    //     String subnet = "hoge";
    //     SubnetUtils subnetUtils = new SubnetUtils(subnet);
    //     SubnetInfo subnetInfo = subnetUtils.getInfo();

    //     then:
    //     thrown(IllegalArgumentException)
    // }
}

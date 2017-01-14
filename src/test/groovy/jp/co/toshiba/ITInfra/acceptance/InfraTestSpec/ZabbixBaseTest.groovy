import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import org.json.JSONObject;
import org.json.JSONException;
import com.goebl.david.Webb;
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

// gradlew --daemon clean test --tests "ZabbixBaseTest.Zabbix 認証"

class ZabbixBaseTest extends Specification {

    TargetServer test_server
    DomainTestRunner test

    def setup() {
        test_server = new TargetServer(
            server_name       : 'localhost',
            ip                : '127.0.0.1',
            platform          : 'Zabbix',
            os_account_id     : 'Test',
            remote_alias      : 'localhost',
        )
        test_server.setAccounts('src/test/resources/config_zabbix.groovy')
        test_server.dry_run = true
    }

    // def "Zabbix 認証"() {
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

    //     JSONObject result = webb.post("http://localhost/zabbix/api_jsonrpc.php")
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

    def "subnet"() {
        setup:
        when:
        String subnet = "192.168.0.0/16";
        SubnetUtils subnetUtils = new SubnetUtils(subnet);
        SubnetInfo subnetInfo = subnetUtils.getInfo();

        System.out.println("サブネット : " + subnet);
        System.out.println("下限 : " + subnetInfo.getLowAddress());
        System.out.println("上限 : " + subnetInfo.getHighAddress());

        then:
        subnetInfo.getNetmask() == '255.255.0.0'
    }

    def "subnet_ng"() {
        setup:
        when:
        String subnet = "hoge";
        SubnetUtils subnetUtils = new SubnetUtils(subnet);
        SubnetInfo subnetInfo = subnetUtils.getInfo();

        then:
        thrown(IllegalArgumentException)
    }
}

import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo
import spock.lang.Specification
import sun.net.util.IPAddressUtil

// gradle --daemon test --tests "SubnetUtilTest.解析1"

class SubnetUtilTest extends Specification {

    def 解析1() {
        when:
        String ip = "192.168.0.1";
        String subnetMask = "255.255.0.0";
        SubnetUtils subnetUtils = new SubnetUtils(ip, subnetMask);
        SubnetInfo subnetInfo = subnetUtils.getInfo();

        println("INFO:${subnetInfo}")
        // INFO:CIDR Signature:    [192.168.0.1/16] Netmask: [255.255.0.0]
        // Network:        [192.168.0.0]
        // Broadcast:      [192.168.255.255]
        // First Address:  [192.168.0.1]
        // Last Address:   [192.168.255.254]
        // # Addresses:    [65534]
        println subnetInfo.getNetworkAddress() 

        then:
        subnetInfo.getNetworkAddress()   == '192.168.0.0'
        subnetInfo.getNetmask()          == '255.255.0.0'
        subnetInfo.getBroadcastAddress() == '192.168.255.255'
        // 1 == 1
    }

    def 解析2() {
        when:
        SubnetUtils subnetUtils = new SubnetUtils("192.168.0.0/17");
        SubnetInfo subnetInfo = subnetUtils.getInfo();

        println("INFO:${subnetInfo}")
        // INFO:CIDR Signature:    [192.168.0.1/16] Netmask: [255.255.0.0]
        // Network:        [192.168.0.0]
        // Broadcast:      [192.168.255.255]
        // First Address:  [192.168.0.1]
        // Last Address:   [192.168.255.254]
        // # Addresses:    [65534]

        then:
        subnetInfo.getNetworkAddress()   == '192.168.0.0'
        subnetInfo.getNetmask()          == '255.255.128.0'
        subnetInfo.getBroadcastAddress() == '192.168.127.255'
        // 1 == 1
    }

    def int intOfIpV4(String ip) {
        int result = 0;
        byte[] bytes = IPAddressUtil.textToNumericFormatV4(ip);
        if (bytes == null) {
            return result;
        }
        for (byte b : bytes) {
            result = result << 8 | (b & 0xFF);
        }
        return result;
    }
    def ルーティングテーブル解析1() {
        when:
        SubnetUtils subnetUtils = new SubnetUtils("192.168.0.0/17");
        SubnetInfo subnetInfo = subnetUtils.getInfo();

        int ip = subnetInfo.asInteger("192.168.0.10")
        println("INFO:${ip}")
        // INFO:CIDR Signature:    [192.168.0.1/16] Netmask: [255.255.0.0]
        // Network:        [192.168.0.0]
        // Broadcast:      [192.168.255.255]
        // First Address:  [192.168.0.1]
        // Last Address:   [192.168.255.254]
        // # Addresses:    [65534]

        then:
        1 == 1
    }
}

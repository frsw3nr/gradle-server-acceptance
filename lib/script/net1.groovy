@Grab('commons-net:commons-net')
@GrabConfig(systemClassLoader=true)

import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

// 基本パターン：IPとサブネットマスクを読み込む

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

// ルーティングテーブル結果解析

// $ ip route
// 192.168.98.0/24 dev eth0  proto kernel  scope link  src 192.168.98.130
// 169.254.0.0/16 dev eth0  scope link  metric 1002

String subnet2 = "192.168.0.0/17";
SubnetUtils subnetUtils2 = new SubnetUtils(subnet2);
SubnetInfo subnetInfo2 = subnetUtils2.getInfo();

println("INFO:${subnetInfo2}")
// INFO:CIDR Signature:    [192.168.0.1/16] Netmask: [255.255.0.0]
// Network:        [192.168.0.0]
// Broadcast:      [192.168.255.255]
// First Address:  [192.168.0.1]
// Last Address:   [192.168.255.254]
// # Addresses:    [65534]

// ルーティングテーブル結果解析3

// $ ip route
// 192.168.98.0/24 dev eth0  proto kernel  scope link  src 192.168.98.130
// 169.254.0.0/16 dev eth0  scope link  metric 1002

SubnetUtils subnetUtils3 = new SubnetUtils("192.168.10.1/12");
SubnetInfo subnetInfo3 = subnetUtils3.getInfo();

println("INFO:${subnetInfo3}")
// INFO:CIDR Signature:    [192.168.0.1/16] Netmask: [255.255.0.0]
// Network:        [192.168.0.0]
// Broadcast:      [192.168.255.255]
// First Address:  [192.168.0.1]
// Last Address:   [192.168.255.254]
// # Addresses:    [65534]


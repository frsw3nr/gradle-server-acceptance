use Zabbix::API;
use Data::Dumper;

my $zabbix = Zabbix::API->new(server => 'http://localhost/zabbix/api_jsonrpc.php',
                              verbosity => 0);

# 認証
eval {
    $zabbix->login(user => 'Admin',
                      password => 'getperf');
};
if ($@) { die 'could not authenticate' };

# ホストグループ
my $hostgroups = $zabbix->fetch('HostGroup', params => { output => "extend" });

if ( @$hostgroups ) {
    for my $hostgroup(@$hostgroups) {
        # print Dumper $hostgroup->{data};
    }
}

# $VAR1 = {
#           'flags' => '0',
#           'name' => 'Linux servers',   ※
#           'groupid' => '2',            ※
#           'internal' => '0'
#         };

# ホスト
my $params = {
    "output" => "extend",
    "selectInterfaces" => "extend",
    "selectGroups" => "extend",
    "selectParentTemplates" => "extend",
};

my $hosts = $zabbix->fetch('Host', params => $params);

if ( @$hosts ) {
    for my $host(@$hosts) {
        print Dumper $host->{data};
    }
}

# $VAR1 = {
#           'maintenances' => [],
#           'jmx_available' => '0',
#           'jmx_errors_from' => '0',
#           'snmp_error' => '',
#           'ipmi_authtype' => '-1',
#           'ipmi_username' => '',
#           'jmx_error' => '',
#           'name' => 'Zabbix server',   ※ Visible Name
#           'ipmi_error' => '',
#           'maintenanceid' => '0',
#           'errors_from' => '0',
#           'ipmi_password' => '',
#           'ipmi_privilege' => '2',
#           'maintenance_type' => '0',
#           'maintenance_status' => '0',
#           'status' => '1',             ※ 0 - (default) monitored host; 1 - unmonitored host.
#           'lastaccess' => '0',
#           'templateid' => '0',
#           'hostid' => '10084',         ※ Host Id
#           'ipmi_errors_from' => '0',
#           'ipmi_available' => '0',
#           'disable_until' => '0',
#           'proxy_hostid' => '0',
#           'interfaces' => [
#               {
#                 'dns' => '',
#                 'ip' => '127.0.0.1',   ※ IP
#                 'hostid' => '10084',   ※
#                 'port' => '10050',     ※ Port
#                 'main' => '1',
#                 'useip' => '1',
#                 'type' => '1',
#                 'interfaceid' => '1'
#               }
#             ],
          # 'groups' => [
          #     {
          #       'flags' => '0',
          #       'name' => 'Zabbix servers',  ※ Host Group
          #       'groupid' => '4',            ※ Host Group Id
          #       'internal' => '0'
          #     }
          #   ],
#           'error' => '',                ※
#           'snmp_disable_until' => '0',
#           'maintenance_from' => '0',
#           'available' => '0',           ※ 0 - (default) unknown; 1 - available; 2 - unavailable.
#           'jmx_disable_until' => '0',
#           'flags' => '0',
#           'snmp_available' => '0',
#           'host' => 'Zabbix server',   ※ Name
#           'snmp_errors_from' => '0',
#           'ipmi_disable_until' => '0'
#         };
         # 'parentTemplates' => [
         #   {
         #     'status' => '3',
         #     'templateid' => '10001',
         #     'lastaccess' => '0',
         #     'ipmi_errors_from' => '0',
         #     'ipmi_available' => '0',
         #     'disable_until' => '0',
         #     'proxy_hostid' => '0',
         #     'error' => '',
         #     'jmx_available' => '0',
         #     'jmx_errors_from' => '0',
         #     'snmp_error' => '',
         #     'maintenance_from' => '0',
         #     'snmp_disable_until' => '0',
         #     'jmx_disable_until' => '0',
         #     'ipmi_authtype' => '0',
         #     'available' => '0',
         #     'ipmi_username' => '',
         #     'jmx_error' => '',
         #     'flags' => '0',
         #     'snmp_available' => '0',
         #     'name' => 'Template OS Linux',  ※ Template Visible name
         #     'ipmi_error' => '',
         #     'maintenanceid' => '0',
         #     'host' => 'Template OS Linux',
         #     'errors_from' => '0',
         #     'ipmi_password' => '',
         #     'snmp_errors_from' => '0',
         #     'ipmi_privilege' => '2',
         #     'ipmi_disable_until' => '0',
         #     'maintenance_type' => '0',
         #     'maintenance_status' => '0'
         #   },

# ユーザー
my $params = {
    "output" => "extend",
    "selectMedias" => "extend",
    "selectUsrgrps" => "extend",
};

my $users = $zabbix->fetch('User', params => $params);

if ( @$users ) {
    for my $user(@$users) {
        print Dumper $user->{data};
    }
}

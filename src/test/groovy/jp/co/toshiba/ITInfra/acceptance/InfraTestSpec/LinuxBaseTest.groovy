import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradle --daemon test --tests "LinuxBaseTest.Linux 複数テスト仕様のロード"

class LinuxBaseTest extends Specification {

    String config_file = 'src/test/resources/config.groovy'
    TestPlatform test_platform

    def setup() {
        def test_target = new TestTarget(
            name              : 'ostrich',
            ip                : '192.168.10.1',
            domain            : 'Linux',
            template_id       : 'AP',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            remote_alias      : 'ostrich',
        )

        def test_rule = new TestRule(name : 'AP',
                                 compare_rule : 'Actual',
                                 compare_source : 'centos7')

        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
        excel_parser.make_template_link(test_target, test_scenario)
        def test_metrics = test_scenario.test_metrics.get('Linux').get('Linux').get_all()

        test_platform = new TestPlatform(
            name         : 'Linux',
            test_target  : test_target,
            test_metrics : test_metrics,
            test_rule    : test_rule,
            dry_run      : true,
        )
    }

    def "Linux テスト仕様のロード"() {
        when:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        then:
        platform_tester.test_spec != null
    }

    def "Linux 複数テスト仕様のロード"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        println("SERVER_INFO:${platform_tester.server_info}")
        platform_tester.set_test_items('hostname', 'hostname_fqdn', 'cpu')
        platform_tester.run()

        then:
        test_platform.test_results['hostname'].value.size() > 0
        test_platform.test_results['hostname_fqdn'].value.size() > 0
        test_platform.test_results['cpu'].value.size() > 0
    }

    def "Linux デバイス付テスト仕様のロード"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('packages')
        platform_tester.run()
        println test_platform.test_results.toString()

        then:
        test_platform.test_results['packages'].devices.csv.size() > 0
        test_platform.test_results['packages'].devices.header.size() > 0
    }

    def "Linux 全テスト仕様のロード"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.run()
        println "COUNT: ${test_platform.test_results.size()}"
        then:
        test_platform.test_results['hostname'].status == null
        test_platform.test_results['keyboard'].status == null
    }

    def "Linux ネットワーク"() {
        setup:
        test_platform.test_target.name = 'cent7'
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('network')
        platform_tester.run()

        then:
        test_platform.test_results['network'].devices.csv.size() > 0
        test_platform.test_results['network'].devices.header.size() > 0
    }

    def "Linux デフォルトゲートウェイ"() {
        setup:
        test_platform.test_target.name = 'cent7'
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('net_route')
        platform_tester.run()

        then:
        test_platform.test_results['net_route'].value.size() > 0
    }

    def "Linux アカウント"() {
        setup:
        test_platform.test_target.name = 'cent7'
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('user')
        platform_tester.run()

        then:
        test_platform.test_results['user'].value.size() > 0
    }

    def "Linux ファイルシステム"() {
        setup:
        test_platform.test_target.name = 'cent7'
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('filesystem')
        platform_tester.run()

        then:
        test_platform.test_results['filesystem'].value.size() > 0
    }

    def "Linux NTP"() {
        setup:
        test_platform.test_target.name = 'cent7'
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('ntp')
        platform_tester.run()

        then:
        println test_platform.test_results['ntp']
        test_platform.test_results['ntp'].value.size() > 0
    }

    // Linux   Kernel  2.6.43      uname
    def "Linux値検証 Kernel" () {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('uname')
        platform_tester.run()

        then:
        println test_platform.test_results['uname']
        1 == 1
    }

    // Linux   OS  CentOS      lsb
    def "Linux値検証 OS" () {
        when:
        println 'test'

        then:
        1 == 1
    }

    // Linux   OSRelease   6.7     lsb
    def "Linux値検証 OSRelease" () {
        when:
        println 'test'

        then:
        1 == 1
    }

    // Linux   Arch    x86_64      uname
    def "Linux値検証 Arch" () {
        when:
        println 'test'

        then:
        1 == 1
    }

    // Linux   cpu_total   1       cpu
    // Linux   cpu_real    1       cpu
    def "Linux値検証 cpu_total" () {
        when:
        println 'test'

        then:
        1 == 1
    }

    // Linux   memory2 4GB     meminfo
    def "Linux値検証 memory2" () {
        when:
        println 'test'

        then:
        1 == 1
    }

    // Linux   net_onboot  eth0    eth1    net_onboot
    def "Linux値検証 net_onboot" () {
        when:
        println 'test'

        then:
        1 == 1
    }

    // Linux   net_route   192.168.0.254       net_route
    def "Linux値検証 net_route" () {
        when:
        println 'test'

        then:
        1 == 1
    }

    // Linux   filesystem  /:26.5G [swap]:3G   filesystem
    def "Linux値検証 filesystem" () {
        when:
        println 'test'

        then:
        1 == 1
    }

    // Linux   users   zabbix      user
    def "Linux値検証 users" () {
        when:
        println 'test'

        then:
        1 == 1
    }

    // Linux   service kdump:On    iptables:Off    service
    def "Linux値検証 service" () {
        when:
        println 'test'

        then:
        1 == 1
    }

    // Linux   SELinux Off     sestatus
    def "Linux値検証 SELinux" () {
        when:
        println 'test'

        then:
        1 == 1
    }

    // Linux   packages    sysstat dmidecode   packages
    def "Linux値検証 packages" () {
        when:
        println 'test'

        then:
        1 == 1
    }

}

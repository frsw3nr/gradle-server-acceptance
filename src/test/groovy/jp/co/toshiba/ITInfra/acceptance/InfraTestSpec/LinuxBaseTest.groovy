import spock.lang.Specification
import static groovy.json.JsonOutput.*
import groovy.json.*
import org.apache.commons.io.FileUtils
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradle --daemon test --tests "LinuxBaseTest.Linux値検証 Kernel"

class LinuxBaseTest extends Specification {

    String config_file = 'src/test/resources/config.groovy'
    TestPlatform test_platform

    def setup() {
        def template_text = new File('src/test/resources/template_ap.json').text
        def template_json = new JsonSlurper().parseText(template_text)
        def template = new TestTemplate(name : 'AP', values : template_json)

        def test_target = new TestTarget(
            name              : 'ostrich',
            ip                : '192.168.10.1',
            domain            : 'Linux',
            template_id       : 'AP',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            remote_alias      : 'ostrich',
            test_templates    : ['AP': template]
        )

        test_platform = new TestPlatform(
            name         : 'Linux',
            test_target  : test_target,
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

        def metric_text = new File('src/test/resources/metrics_Linux.json').text
        def metric_json = new JsonSlurper().parseText(metric_text) as String[]

        when:
        println metric_json
        platform_tester.set_test_items(metric_json)
        platform_tester.run()
        println "COUNT: ${test_platform.test_results.size()}"

        then:
        1 == 1
        // test_platform.test_results['hostname'].status == null
        // test_platform.test_results['keyboard'].status == null
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
        def json = JsonOutput.toJson(test_platform.port_lists)
        println JsonOutput.prettyPrint(json)
        test_platform.port_lists.size() > 0
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
                                                 config_file : config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('uname')
        platform_tester.run()

        then:
        println test_platform.test_results['uname']
        println test_platform.test_results['kernel']
        println test_platform.test_results['arch']
        test_platform.test_results['uname'].status  == ResultStatus.OK
        test_platform.test_results['kernel'].verify == ResultStatus.NG
        test_platform.test_results['arch'].verify   == ResultStatus.OK
    }

    // Linux   OS  CentOS      lsb
    def "Linux値検証 OS" () {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('lsb')
        platform_tester.run()

        then:
        println test_platform.test_results['lsb']
        println test_platform.test_results['os']
        println test_platform.test_results['os_release']
        test_platform.test_results['lsb'].status  == ResultStatus.OK
        // test_platform.test_results['os'].verify == ResultStatus.OK
        // test_platform.test_results['os_release'].verify   == ResultStatus.OK
    }

    // Linux   cpu_total   1       cpu
    // Linux   cpu_real    1       cpu
    def "Linux値検証 cpu_total" () {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('cpu')
        platform_tester.run()

        then:
        println test_platform.test_results
        test_platform.test_results['cpu'].status  == ResultStatus.OK
        test_platform.test_results['cpu_total'].verify == ResultStatus.OK
        test_platform.test_results['cpu_real'].verify   == ResultStatus.OK
    }

    // Linux   memory2 4GB     meminfo
    def "Linux値検証 meminfo" () {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('meminfo')
        platform_tester.run()

        then:
        1 == 1
        println test_platform.test_results
        test_platform.test_results['meminfo'].status  == ResultStatus.OK
        test_platform.test_results['mem_total'].verify == ResultStatus.NG
    }

    // Linux   network
    def "Linux値検証 network" () {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('network')
        platform_tester.run()

        then:
        1 == 1
        println test_platform.test_results
        // test_platform.test_results['meminfo'].status  == ResultStatus.OK
        // test_platform.test_results['mem_total'].verify == ResultStatus.OK
    }

    // Linux   net_onboot  eth0    eth1    net_onboot
    def "Linux値検証 net_onboot" () {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('net_onboot')
        platform_tester.run()

        then:
        println test_platform.test_results
        test_platform.test_results['net_onboot'].status  == ResultStatus.OK
        // test_platform.test_results['net_onboot'].verify == ResultStatus.OK
    }

    // Linux   net_route   192.168.0.254       net_route
    def "Linux値検証 net_route" () {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('net_route')
        platform_tester.run()

        then:
        println test_platform.test_results
        test_platform.test_results['net_route'].status  == ResultStatus.OK
        // test_platform.test_results['net_route'].verify == ResultStatus.OK
    }

    // Linux   filesystem  /:26.5G [swap]:3G   filesystem
    def "Linux値検証 filesystem" () {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('filesystem')
        platform_tester.run()

        then:
        println test_platform.test_results
        test_platform.test_results['filesystem'].status  == ResultStatus.OK
        // test_platform.test_results['filesystem'].verify == ResultStatus.OK
    }

    // Linux   users   zabbix      user
    def "Linux値検証 user" () {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('user')
        platform_tester.run()

        then:
        test_platform.test_results['user'].status  == ResultStatus.OK
        // test_platform.test_results['user'].verify == ResultStatus.OK
        // 1 == 1
    }

    // Linux   service kdump:On    iptables:Off    service
    def "Linux値検証 service" () {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('service')
        platform_tester.run()

        then:
        test_platform.test_results['service'].status  == ResultStatus.OK
        // test_platform.test_results['service'].verify == ResultStatus.OK
    }

    // Linux   SELinux Off     sestatus
    def "Linux値検証 SELinux" () {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('sestatus')
        platform_tester.run()

        then:
        test_platform.test_results['sestatus'].status  == ResultStatus.OK
        // test_platform.test_results['sestatus'].verify == ResultStatus.OK
    }

    // Linux   packages    sysstat dmidecode   packages
    def "Linux値検証 packages" () {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('packages')
        platform_tester.run()

        then:
        println test_platform.test_results
        1 == 1
        // test_platform.test_results['packages'].status  == ResultStatus.OK
        // test_platform.test_results['packages'].verify == ResultStatus.NG
    }

}

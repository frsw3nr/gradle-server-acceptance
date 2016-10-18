import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradle --daemon clean test --tests "LinuxBaseTest.Linux テスト仕様のロード"

class LinuxBaseTest extends Specification {

    TargetServer test_server
    DomainTestRunner test

    def setup() {
        test_server = new TargetServer(
            server_name   : 'ostrich',
            ip            : 'localhost',
            platform      : 'Linux',
            os_account_id : 'Test',
            vcenter_id    : 'Test',
            vm            : 'ostrich',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
    }

    def "Linux テスト仕様のロード"() {
        setup:
        test_server.dry_run = true
        test = new DomainTestRunner(test_server, 'Linux')

        when:
        def test_item = new TestItem('hostname')
        test.run(test_item)

        then:
        println test_item.results.toString()
        test_item.results.size() > 0
    }

    def "Linux 複数テスト仕様のロード"() {
        setup:
        test_server.dry_run = true
        test = new DomainTestRunner(test_server, 'Linux')
        def test_ids = ['hostname', 'hostname_fqdn', 'cpu']
        def test_items = []
        test_ids.each {
            test_items << new TestItem(it)
        }

        when:
        test.run(test_items as TestItem[])
        test_items.each { test_item ->
            println test_item.results.toString()
       }

        then:
        test_items.each { test_item ->
            test_item.results.size() > 0
       }
    }

    def "Linux 全テスト仕様のロード"() {
        setup:
        test_server.dry_run = true
        test = new DomainTestRunner(test_server, 'Linux')
        def test_ids = [
            'uname',
            'block_device',
            'cpu',
            'crash_size',
            'filesystem',
            'filesystem_df_ip',
            'filesystem_mount',
            'fips',
            'hostname',
            'hostname_fqdn',
            'hostname_short',
            'iptables',
            'kdump',
            'lsb',
            'machineid',
            'mdadb',
            'meminfo',
            'mount_iso',
            'net_onboot',
            'network',
            'oracle',
            'packages',
            'proxy_global',
            'resolve_conf',
            'runlevel',
            'sestatus',
            'virturization',
            'vncserver',
        ]
        def test_items = []
        test_ids.each {
            test_items << new TestItem(it)
        }

        when:
        test.run(test_items as TestItem[])
        test_items.each { test_item ->
            println test_item.results.toString()
       }

        then:
        test_items.each { test_item ->
            test_item.results.size() > 0
       }
    }

}

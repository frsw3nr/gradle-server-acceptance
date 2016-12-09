import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon clean test --tests "InfraTestSpecTest"

class InfraTestSpecTest extends Specification {

    TargetServer test_server
    DomainTestRunner test
    InfraTestSpec spec

    def setup() {
        test_server = new TargetServer(
            server_name       : 'win2012',
            ip                : '192.168.0.12',
            platform          : 'Windows',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            remote_alias      : 'win2012.ostrich',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
        test_server.dry_run = true
    }

    def "UTF-16検査結果の読込"() {
        setup:
        spec = new InfraTestSpec(test_server, 'Windows')

        when:
        def lines = spec.exec('ipconfig') {
            new File("${local_dir}/ipconfig")
        }
        def tmp = [:].withDefault{''}
        def csv = []
        lines.eachLine {
            (it =~ /IPv4 アドレス.+:\s+(.+)$/).each {m0,m1->
                tmp['ipv4'] = m1
            }
        }

        then:
        tmp.size() > 0
    }

    def "SJIS検査結果の読込"() {
        setup:
        spec = new InfraTestSpec(test_server, 'Windows')

        when:
        def lines = spec.exec('ipconfig_sjis', false, "MS932") {
            new File("${local_dir}/ipconfig_sjis").getText("MS932")
        }
        def tmp = [:].withDefault{''}
        def csv = []
        lines.eachLine {
            (it =~ /IPv4 アドレス.+:\s+(.+)$/).each {m0,m1->
                tmp['ipv4'] = m1
            }
        }

        then:
        tmp.size() > 0
    }

    def "EUC-JP検査結果の読込"() {
        setup:
        spec = new InfraTestSpec(test_server, 'Windows')

        when:
        def lines = spec.exec('ipconfig_eucjp', false, "EUC_JP") {
            new File("${local_dir}/ipconfig_eucjp").getText("EUC_JP")
        }
        def tmp = [:].withDefault{''}
        def csv = []
        lines.eachLine {
            (it =~ /IPv4 アドレス.+:\s+(.+)$/).each {m0,m1->
                tmp['ipv4'] = m1
            }
        }

        then:
        tmp.size() > 0
    }

    def "UCS-2-LITTLE-ENDIAN検査結果の読込"() {
        setup:
        spec = new InfraTestSpec(test_server, 'Windows')

        when:
        def lines = spec.exec('wmic_net.txt', false, "UTF-16LE") {
            new File("${local_dir}/wmic_net.txt", "UTF-16LE")
        }
        def tmp = [:].withDefault{0}
        lines.eachLine {
            (it =~ /(イーサネット|トンネル)/).each {m0,m1->
                tmp[m1] ++
            }
        }
        println tmp
        then:
        tmp['イーサネット'] > 0
        tmp['トンネル'] > 0
    }

    def "結果の共有"() {
        setup:
        spec = new InfraTestSpec(test_server, 'Windows')

        when:
        def lines = spec.exec('date.txt', true) {
            new File("${evidence_log_share_dir}/date.txt")
        }
        println lines
        then:
        1 == 1
    }
}

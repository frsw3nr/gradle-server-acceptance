import spock.lang.Specification
import static groovy.json.JsonOutput.*
import groovy.json.*
import com.taskadapter.redmineapi.*
import com.taskadapter.redmineapi.bean.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.Ticket.*

// gradle --daemon test --tests "TestTicketManager.ポートリスト登録3"

class TestTicketManager extends Specification {

    TicketManager ticket_manager

// REGIST: 
// IAサーバ, ostrich, [OS名:CentOS release 6.7, CPU数:1, MEM容量:1.8332023620605469]
// REGIST_PORT: 
// [192.168.10.1, 192.168.10.4]

// REGIST: 
// IAサーバ, cent7, [OS名:CentOS Linux release 7.3.1611, CPU数:1, MEM容量:1.796844482421875]
// REGIST_PORT: 
// [172.17.0.1, 192.168.0.20]

// REGIST: 
// IAサーバ, win2012, [OS名:Microsoft Windows Server 2012 R2 Standard 評価版, CPU数:null, MEM容量:3.9995651245117188]
// REGIST_PORT: 
// [192.168.0.24]

    def setup() {
        def test_env = ConfigTestEnvironment.instance
        test_env.get_cmdb_config()
        test_env.config.db_config = 'src/test/resources/cmdb.groovy'
        ticket_manager = TicketManager.instance
        test_env.accept(ticket_manager)
        ticket_manager.init()
    }

    def 初期化() {
        when:
        //  ２回初期化してもエラーにならないこと
        ticket_manager.init()
        println ticket_manager

        then:
        ticket_manager != null
    }

    def "チケット登録1"() {
        when:
        ticket_manager.delete('ostrich')
        def custom_fields = [
                'OS名':'CentOS release 6.7',
                'CPU数':'1',
                'MEM容量':'1.8332023620605469',
                'インベントリ': 'ostrich',
            ]
        Issue issue = ticket_manager.regist('cmdb', 'IAサーバ', 'ostrich', custom_fields)
        println issue

        then:
        issue != null
    }

    def "不明プロジェクトエラー"() {
        when:
        ticket_manager.delete('ostrich')
        Issue issue = ticket_manager.regist('hoge', 'IAサーバ', 'ostrich')

        then:
        issue == null
    }

    def "不明トラッカーエラー"() {
        when:
        ticket_manager.delete('ostrich')
        Issue issue = ticket_manager.regist('cmdb', 'hoge', 'ostrich')

        then:
        issue == null
    }

    def "不明カスタムフィールド"() {
        when:
        ticket_manager.delete('ostrich')
        def custom_fields = [
                'hoge':'CentOS release 6.7',
            ]
        Issue issue = ticket_manager.regist('cmdb', 'IAサーバ', 'ostrich', custom_fields)

        then:
        issue == null
    }

    def "カスタムフィールド異常値"() {
        when:
        ticket_manager.delete('ostrich')
        def custom_fields = [
                // '搬入日':'2018-12-18',
                '搬入日':'hoge',
            ]
        Issue issue = ticket_manager.regist('cmdb', 'IAサーバ', 'ostrich', custom_fields)

        then:
        issue == null
    }

    def "ポートリスト登録"() {
        when:
        ticket_manager.delete('ostrich')
        Issue server1 = ticket_manager.regist('cmdb', 'IAサーバ', 'ostrich')

        // ポートリスト登録
        def port_list1 = ticket_manager.regist('cmdb', 'ポートリスト', '192.168.10.1')
        def port_list2 = ticket_manager.regist('cmdb', 'ポートリスト', '192.168.10.4')
        def result = ticket_manager.link(server1, [port_list1.id, port_list2.id])

        then:
        result == true

        when:
        def result2 = ticket_manager.link(server1, [port_list1.id, port_list2.id])

        then:
        result2 == true
    }

    def "ポートリスト登録2"() {
        when:

        // ポートリスト登録
        def custom_fields = [
            "ip": "192.168.10.100",
            "description": "LAN3",
            "mac": "00:1b:21:12:34:56",
            "vendor": "Intel Corporate",
            "switch_name": "router1",
        ]
        def port_list1 = ticket_manager.regist_port_list('cmdb', '192.168.10.100', custom_fields)

        then:
        1 == 1
    }

    def "ポートリスト登録3"() {
        when:

        // ポートリスト登録
        def custom_fields = [
            "ip": "192.168.0.100",
            "lookup": true,
        ]
        def port_list1 = ticket_manager.regist_port_list('cmdb', '192.168.0.100', custom_fields)

        then:
        1 == 1
    }

    def "ポートリスト登録4"() {
        when:

        // ポートリスト登録
        def custom_fields = [
            "ip": "192.168.0.100",
            "lookup": false,
        ]
        def port_list1 = ticket_manager.regist_port_list('cmdb', '192.168.0.100', custom_fields)

        then:
        1 == 1
    }

    def "リンク異常"() {
        when:
        ticket_manager.delete('ostrich')
        Issue server1 = ticket_manager.regist('cmdb', 'IAサーバ', 'ostrich')

        // ポートリスト登録
        def result = ticket_manager.link(server1, [-1])

        then:
        result == false
    }

    def "リンク先なし"() {
        when:
        ticket_manager.delete('ostrich')
        Issue server1 = ticket_manager.regist('cmdb', 'IAサーバ', 'ostrich')

        // ポートリスト登録
        def result = ticket_manager.link(server1, [])

        then:
        result == true
    }

}


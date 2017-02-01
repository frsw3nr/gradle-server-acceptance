import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import groovy.sql.Sql

// gradle --daemon clean test --tests "EvidenceFileTest.メイン処理"

class EvidenceFileTest extends Specification {

    def params = [
        base_home:       '.',
        project_home:    'src/test/resources',
        db_config:       'src/test/resources/cmdb.groovy',
        last_run_config: 'src/test/resources/log/.last_run',
    ]

    def "DB登録"() {
        when:
        def evidence = new EvidenceFile(params)
        evidence.exportCMDB()

        then:
        1 == 1
    }

    def "DB全体登録"() {
        when:
        def evidence = new EvidenceFile(params)
        evidence.exportCMDBAll()

        then:
        1 == 1
    }

    // def "MySQL登録"() {
    //     setup:
    //     def home = 'src/test/resources'

    //     when:
    //     def config   = 'src/test/resources/mysql.groovy'
    //     def last_run = 'src/test/resources/log/.last_run'
    //     def evidence = new EvidenceFile(home: home, db_config: config,
    //                                     last_run_config: last_run)
    //     evidence.exportCMDBAll()

    //     then:
    //     1 == 1
    // }

    def "検査シートバックアップ"() {
        setup:
        def home = System.getProperty("user.dir")
        new File("./build/check_sheet_20170116_080000.xlsx").text = 'dummy'
        new File("./build/check_sheet_20170116_080001.xlsx").text = 'dummy'
        new File("./build/check_sheet_20170116_080002.xlsx").text = 'dummy'
        new File("./build/check_sheet_20170116_090544.xlsx").text = 'dummy'
        new File("./build/log/_node").mkdirs()

        params['db_config']       = 'src/test/resources/config_zabbix.groovy'
        params['last_run_config'] = 'src/test/resources/log2/.last_run'

        when:
        def evidence = new EvidenceFile(params)
        evidence.generate()

        then:
        1 == 1
    }

}

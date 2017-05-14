import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import org.apache.commons.io.FileUtils
import groovy.io.FileType
import static groovy.json.JsonOutput.*

class EvidenceSheetTest extends Specification{

    def "設定ファイルの日付パラメータ変換"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config.groovy')

        then:
        evidence.evidence_target != './build/check_sheet.xlsx'
    }

    def "設定ファイルなし"() {
        when:
        def evidence = new EvidenceSheet('config/hoge.groovy')

        then:
        thrown(FileNotFoundException)
    }

    def "既定のExcelファイル読み込み"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config.groovy')
        evidence.readSheet()
        println evidence.compare_servers
        evidence.test_servers.each {
            println "Sever: ${it.server_name}, Compare: ${it.compare_server}"
        }

        then:
        evidence.evidence_source == './src/test/resources/check_sheet.xlsx'
    }

    def "計算式を埋め込んだExcelファイル読み込み"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config.groovy')
        evidence.evidence_source = './src/test/resources/check_sheet_formula.xlsx'
        evidence.readSheet()

        then:
        thrown(IllegalArgumentException)
    }

    def "日本語Excelファイル読み込み"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config_jp.groovy')
        evidence.readSheet()

        then:
        evidence.evidence_source == './src/main/resources/root/jp/サーバーチェックシート.xlsx'
    }

    def "Excelシートなし"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config2.groovy')
        evidence.readSheet()

        then:
        thrown(IllegalArgumentException)
    }

    def "既定のExcelファイル書き込み"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config1.groovy')
        evidence.readSheet()
        evidence.prepareTestStage()

        def data = ['Linux': ['ostrich': ['vCenter': [
            'test': ['NumCpu':2, 'PowerState':'PoweredOn', 'MemoryGB':2],
            'verify':['NumCpu':true, 'MemoryGB':false]
        ]]]]
        evidence.updateTestResult('Linux', 'ostrich', 0, data['Linux']['ostrich'])

        then:
        evidence.evidence_source == './src/test/resources/check_sheet.xlsx'
    }

    def "テンプレート書き込み"() {
        setup:
        def evidence_manager = new EvidenceManager(
            getconfig_home: '.',
            project_home: 'src/test/resources',
            db_config: 'src/test/resources/cmdb.groovy'
        )
        ResultContainer.instance.test_results   = new ConfigObject()
        ResultContainer.instance.device_results = new ConfigObject()
        ResultContainer.instance.loadNodeConfigJSON(evidence_manager, 'ostrich')
        ResultContainer.instance.loadNodeConfigJSON(evidence_manager, 'win2012')

        def evidence = new EvidenceSheet('src/test/resources/config.groovy')
        evidence.readSheet()

        when:
        evidence.prepareTestStage()
        println evidence.compare_servers
        evidence.updateTemplateResult('Linux',   'ostrich', 0)
        evidence.updateTemplateResult('Windows', 'win2012', 0)

        then:
        evidence.evidence_source == './src/test/resources/check_sheet.xlsx'
    }

    def "デバイスシートの書き込み"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config.groovy')
        evidence.readSheet()
        evidence.prepareTestStage()

        def headers = ['name', 'epoch', 'version', 'release', 'installtime', 'arch']
        def csvs = [
            'ostrich': [
                ['name1', 'epoch1', 'version1', 'release1', 'installtime1', 'arch1'],
                ['name2', 'epoch2', 'version2', 'release2', 'installtime2', 'arch2'],
                ['name3', 'epoch3', 'version3', 'release3', 'installtime3', 'arch3'],
            ],
            'testtestdb': [
                ['name1', 'epoch1', 'version1', 'release1', 'installtime1', 'arch1'],
                ['name2', 'epoch2', 'version2', 'release2', 'installtime2', 'arch2'],
            ]
        ]
        evidence.insertDeviceSheet('Linux', 'packages', headers, csvs)

        then:
        evidence.device_test_ids.size() > 0
        evidence.evidence_source == './src/test/resources/check_sheet.xlsx'
    }

    def "検査サーバスクリプト読み込み"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config.groovy')
        evidence.readServerConfigScript('src/test/resources/test_servers.groovy')

        then:
        evidence.test_servers.size() > 0
    }

    def "検査サーバCSV読み込み"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config_jp.groovy')
        evidence.readServerConfigCSV('src/test/resources/issues.csv')

        then:
        println evidence.test_servers[0].infos
        evidence.test_servers.size() > 0
    }

    def "Excelファイルと検査サーバスクリプト読み込み"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config.groovy')
        evidence.readSheet('src/test/resources/test_servers.groovy')

        then:
        evidence.test_servers.size() > 0
        evidence.evidence_source == './src/test/resources/check_sheet.xlsx'
    }

    def "比較対象サーバの抽出"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config.groovy')
        evidence.readSheet()
        println evidence.compare_servers
        // println prettyPrint(toJson(Config.instance.servers))
        // println prettyPrint(toJson(Config.instance.devices))

        then:
        evidence.compare_servers.size() > 0
    }

    def "ノード定義読み込み"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config.groovy')
        evidence.readSheet()
        FileUtils.copyDirectory(new File("src/test/resources/node/"),
                                new File("node/"))
        // println evidence.compare_servers

        then:
        evidence.compare_servers.size() > 0
    }

    def "Excel検査結果の読込"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config.groovy')
        evidence.evidence_source = './src/test/resources/check_sheet_20170512_143424.xlsx'
        def csv = evidence.readAllTestResult()
        def row = csv.size()
        def colsize_is_4 = true
        csv.each {
            if ( it.size() != 4)
                colsize_is_4 = false
        }

        then:
        row > 0
        colsize_is_4 == true
    }

}

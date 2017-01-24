import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

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

        then:
        evidence.evidence_source == './src/test/resources/check_sheet.xlsx'
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



}

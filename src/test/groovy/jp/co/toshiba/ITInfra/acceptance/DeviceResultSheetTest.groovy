import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon clean test --tests "DeviceSheetTest"

class DeviceResultSheetTest extends Specification {

    def "デバイス検査結果の登録"() {
        setup:
        def device_result = new DeviceResultSheet()
        def test_item = new TestItem('packages')
        test_item.device_header = ['name', 'epoch', 'version', 'release', 'installtime', 'arch']
        test_item.devices = [
            ['name1', 'epoch1', 'version1', 'release1', 'installtime1', 'arch1'],
            ['name2', 'epoch2', 'version2', 'release2', 'installtime2', 'arch2'],
            ['name3', 'epoch3', 'version3', 'release3', 'installtime3', 'arch3'],
        ]

        when:
        device_result.setResults('Linux', 'testtestdb', [test_item])
        def headers = device_result.getHeaders('Linux', 'packages')
        def csvs    = device_result.getCSVs('Linux', 'packages')

        then:
        headers.size() > 0
        csvs.size() > 0
    }

    def "複数のデバイス検査結果の登録"() {
        setup:
        def device_result = new DeviceResultSheet()
        def test_item1 = new TestItem('packages')
        test_item1.device_header = ['name', 'epoch', 'version', 'release', 'installtime', 'arch']
        test_item1.devices = [
            ['name1', 'epoch1', 'version1', 'release1', 'installtime1', 'arch1'],
            ['name2', 'epoch2', 'version2', 'release2', 'installtime2', 'arch2'],
            ['name3', 'epoch3', 'version3', 'release3', 'installtime3', 'arch3'],
        ]

        def test_item2 = new TestItem('networks')
        test_item2.device_header = ['name', 'version']
        test_item2.devices = [
            ['name1', 'version1'],
            ['name2', 'version2'],
        ]

        when:
        device_result.setResults('Linux', 'testtestdb', [test_item1, test_item2])
        def headers = device_result.getHeaders('Linux', 'networks')
        def csvs    = device_result.getCSVs('Linux', 'networks')

        then:
        headers.size() > 0
        csvs.size() > 0
    }

    def "複数サーバのデバイス検査結果の登録"() {
        setup:
        def device_result = new DeviceResultSheet()
        def test_item = new TestItem('networks')
        test_item.device_header = ['name', 'version']
        test_item.devices = [
            ['name1', 'version1'],
            ['name2', 'version2'],
        ]

        when:
        device_result.setResults('Linux', 'testtestdb', [test_item])
        device_result.setResults('Linux', 'testtestdb2', [test_item])
        def headers = device_result.getHeaders('Linux', 'networks')
        def csvs    = device_result.getCSVs('Linux', 'networks')

        println headers
        println csvs

        then:
        headers.size() > 0
        csvs.size() > 0
    }
}

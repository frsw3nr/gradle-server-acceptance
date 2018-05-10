import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon test --tests "TestScriptGeneratorTest"

class TestScriptGeneratorTest extends Specification {

    def "vCenterテストスクリプトの初期化"() {
        setup:
        def code = new TestScriptGenerator('./lib/template', 'vCenter')

        when:
        code.addCommand('vm', 'get-vm $vm | select NumCpu, PowerState, MemoryGB, VMHost')
        println code.generate()

        then:
        code.generate().size() > 0
    }

}

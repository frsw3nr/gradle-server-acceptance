import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon clean test --tests "CodeGeneratorTest"

class CodeGeneratorTest extends Specification {

    def "vCenterテストスクリプトの初期化"() {
        setup:
        def code = new CodeGenerator('./lib/template', 'vCenter')

        when:
        code.addCommand('vm', 'get-vm $vm | select NumCpu, PowerState, MemoryGB, VMHost')
        println code.generate()

        then:
        1 == 1
    }

}

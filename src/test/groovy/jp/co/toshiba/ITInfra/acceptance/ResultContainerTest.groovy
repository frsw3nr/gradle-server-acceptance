import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.Charset
import org.apache.commons.io.FileUtils

// gradle --daemon clean test --tests "ResultContainerTest"

class ResultContainerTest extends Specification {

    def node_dir = 'src/test/resources/node'

    def "ResultContainer execption"() {
        when:
        new ResultContainer()

        then:
        thrown RuntimeException
    }

    def "JSON読み込み"() {
        when:
        ResultContainer.instance.loadNodeConfigJSON(node_dir, 'ostrich')

        then:
        ResultContainer.instance.test_results.size() > 0
        ResultContainer.instance.device_results.size() > 0
    }

    // def "MySQL読み込み"() {
    //     when:

    //     then:
    //     1 == 1
    // }

    // def "検査結果の比較"() {
    //     when:

    //     then:
    //     1 == 1
    // }

    // def "検査結果デバイスの比較"() {
    //     when:

    //     then:
    //     1 == 1
    // }
}

package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import org.hidetake.groovy.ssh.Ssh
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

@Slf4j
@InheritConstructors
class vCenterSpec extends vCenterSpecBase {

    @Override
    def init() {
        println "test:vCenterSpec 2.14"
        super.init()
    }

    def finish() {
    }

}

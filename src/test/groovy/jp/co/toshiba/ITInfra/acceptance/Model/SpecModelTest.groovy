// import com.gh.mygreen.xlsmapper.*
// import com.gh.mygreen.xlsmapper.annotation.*
// import com.getconfig.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.Model.SpecModel2
import jp.co.toshiba.ITInfra.acceptance.Model.SpecModelLeaf2
import spock.lang.Specification

// gradle --daemon test --tests "SpecModelTest.*"

class SpecModelTest extends Specification {

    def param1 = ['name': 'Linux',   "enabled": false]
    def param2 = ['name': 'Windows', "enabled": false]

    def "初期化"() {
        when:
        // SpecModel linux = new SpecModel(name:'ostrich')
        SpecModelLeaf2 linux = new SpecModelLeaf2(param1)
        println("$linux")

        then:
        linux.enabled == false
        1 == 1
    }

    // def "コンポジット初期化"() {
    //     when:
    //     SpecModelLeaf2 linux1 = new SpecModelLeaf2(param1)
    //     SpecModelLeaf2 linux2 = new SpecModelLeaf2(name: 'Linux', second: true)
    //     SpecModelComposite2 spec1 = new SpecModelComposite2(name: 'root')
    //     spec1.add(linux1)
    //     spec1.add(linux2)
    //     println("$spec1")

    //     then:
    //     spec1.get('Linux').second == true
    //     spec1.get('Linux').enabled == null
    //     // linux.enabled == false
    //     1 == 1
    // }

    // def "ツリーコンポジット初期化"() {
    //     when:
    //     SpecModelLeaf2 linux1 = new SpecModelLeaf2(param1)
    //     SpecModelComposite2 spec1 = new SpecModelComposite2(name: 'root')
    //     spec1.add_leaf(['OS','ostrich', 'Linux'], linux1)
    //     println("$spec1")

    //     then:
    //     // linux.enabled == false
    //     1 == 1
    // }
}

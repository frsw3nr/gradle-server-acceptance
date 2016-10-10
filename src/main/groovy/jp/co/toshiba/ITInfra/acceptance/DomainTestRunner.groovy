package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString

// ToDo

// 定数の定義、user_lib, user_package
// DomainTestRunner に変更
// run(test_id) 実装
// run(TestSpec[]) 実装

@Slf4j
class DomainTestRunner {

    static final user_lib = './lib'
    static final user_package = 'InfraTestSpec'

    TargetServer test_server
    String     domain
    TestItem[] results

    private spec

    DomainTestRunner(TargetServer test_server, String domain) {
        this.test_server = test_server
        this.domain      = domain

        def loader = new GroovyClassLoader()
        loader.addClasspath(user_lib)
        loader.clearCache()

        def user_script = "${user_lib}/${user_package}/${domain}Spec.groovy"
        def clazz = loader.parseClass(new File(user_script))
        spec = clazz.newInstance(test_server, domain)
    }

    def run(TestItem[] test_items) {
        spec.init()
        spec.setup_exec(test_items)
        // test_items.each {
        //     // println it.test_id
        //     spec.setup_exec(it)
        // }
    }

    def run(TestItem test_item) {
        spec.init()
        spec.setup_exec(test_item)

        // switch(spec.mode) {
        //     case EnumTestMode.loop_exec_parse:
        //         spec.setup_exec()
        //         spec.metaClass.methods.each { method ->
        //             if (method.name == test_id) {
        //                 println "Method : " + method.name
        //                 method.invoke(spec)
        //             }
        //         }
        //         spec.cleanup_exec()
        //     break

        //     case EnumTestMode.loop_exec_loop_parse:
        //         spec.setup_exec()
        //         spec.metaClass.methods.each { method ->
        //             if (method.name == test_id) {
        //                 println "Method : " + method.name
        //                 method.invoke(this, method.name)
        //             }
        //         }
        //         spec.cleanup_exec()
        //     break
        // }
        println spec.mode
// setup_exec
// cleanup_exec
// setup_parse
// cleanup_parse

        // println 'hello from GreetingTask'
        // spec.metaClass.methods.each { method ->
        //     if (method.name == test_id) {
        //         println "Method : " + method.name
        //         // method.invoke(this, method.name)
        //     }
        //     // (method.name == test_id).each {m0,m1->
        //     //     println "Method : " + method.name
        //     //     // method.invoke(this, m1)
        //     // }
        // }

    }
}

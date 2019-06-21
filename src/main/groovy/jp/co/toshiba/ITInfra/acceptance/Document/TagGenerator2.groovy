package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import jp.co.toshiba.ITInfra.acceptance.Model.TestTargetSet

@Slf4j
@ToString(includePackage = false)
class DisplayPriority {
    String tag
    int priority_group
    int priority_row

    DisplayPriority(String tag, int priority_group, int priority_row) {
        this.tag            = tag
        this.priority_group = priority_group
        this.priority_row   = priority_row
    }

    int priority() {
        return this.priority_group * 1000 + this.priority_row
    }
}

// @Slf4j
// @ToString(includePackage = false)
// class DomainDisplayOrders {
//     Map display_orders = new LinkedHashMap<String,DisplayPriority>()
// }

@Slf4j
@ToString(includePackage = false)
class TagGenerator2 {

    // Map domain_display_orders = new LinkedHashMap<String, DomainDisplayOrders>() 
    Map domain_display_orders = [:].withDefault {
        new LinkedHashMap<String,DisplayPriority>()
    }
        
    def set_environment(ConfigTestEnvironment env) {
    }

    // def add_compared_results(String domain, List target_names, TestTargetSet source, TestTargetSet dest) {
    //     // Add first column to comparison server
    //     def compare_server = target_names[0]
    //     def compare_target = source.get(compare_server, domain)
    //     compare_target.tag = compare_server
    //     dest.add(compare_target)

    //     // Add the terget server from the second column
    //     def target_size = target_names.size()
    //     (1..(target_size - 1)).each { index ->
    //         def target_name = target_names[index]
    //         def test_target = source.get(target_name, domain)
    //         test_target.compare_server = compare_server
    //         test_target.tag = compare_server
    //         dest.add(test_target)
    //     }
    // }

    // def add_single_results(String domain, List target_names, TestTargetSet source, TestTargetSet dest) {
    //     def test_target = source.get(target_names[0], domain)
    //     test_target.compare_server = null
    //     dest.add(test_target)
    // }

    // TODO: ターゲットにタグの追加、Excelフォームのカラムグループセット
    // TODO: テストシナリオのターゲットリストをクラスターID順にソート
    // def make_target_tag(TestScenario test_scenario) {
    //     def new_test_targets = new TestTargetSet(name: 'cluster')
    //     def test_targets = test_scenario.test_targets
    //     domain_clusters.each { domain, domain_cluster ->
    //         def clustering_targets = domain_cluster.sorted_clustering_targets

    //         clustering_targets.each { cluster_index, target_names ->
    //             def target_size = target_names.size()
    //             if (target_size >= 2) {
    //                 add_compared_results(domain, target_names, test_targets, new_test_targets)

    //             } else if (target_size == 1) {
    //                 add_single_results(domain, target_names, test_targets, new_test_targets)
    //             }
    //         }
    //     }
    //     test_scenario.test_targets = new_test_targets
    //     log.info "New sorted clustering targets : ${new_test_targets.get_keys()}"
    // }

    // * 比較対象を指定したシナリオで、 Excel 検査結果出力レイアウトを変更
    //     * タグ機能の追加 [ ]
    //     * グループ化して、グループの順に配置 [ ]
    //     * グループ内の1列目に比較対象を配置 [ ]
    // TODO:
    // * 暫定で TagGenerator2 クラスを作る。比較指定モードの場合の操作
    //     targets 順に検索
    //     初回のtarget で compare_server があればその前に compare_server を追加
    //     同一 compare_server が続けば、同じタグでタグ付け
    //     compare_server 自身の列は削除する(既に追加されているはず)

    def make_domain_display_order(TestScenario test_scenario) {
        def targets = test_scenario.test_targets.get_all()
        def tags = [:].withDefault{0}
        def row = 0
        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                if (test_target.comparision)
                    return
                def tag = test_target.compare_server
                if (tag) {
                    if (tags[tag] == 0) {
                        def priority = new DisplayPriority(tag, tags.size(), 0)
                        this.domain_display_orders[domain][tag] = priority
                    }
                    tags[tag] ++
                    def priority = new DisplayPriority(tag, tags.size(), tags[tag])
                    this.domain_display_orders[domain][target_name] = priority
                } else {
                    def priority = new DisplayPriority(null, 99, row)
                    this.domain_display_orders[domain][target_name] = priority
                }
                row ++
            }
        }
    }

    def make_target_tag(TestScenario test_scenario) {
        TestTargetSet new_test_targets = new TestTargetSet(name: 'cluster')
        def test_targets = test_scenario.test_targets
        this.domain_display_orders.each { domain, display_orders ->
            Map sorted_targets = display_orders.sort { a, b -> 
                a.value.priority() <=> b.value.priority() 
            }
            sorted_targets.each {target_name, display_priority ->
                def test_target = test_targets.get(target_name, domain)
                test_target.tag = display_priority.tag
                println test_target
                new_test_targets.add(test_target)
                println "${domain},${target_name}: ${display_priority}"
            }
        }
        println new_test_targets.children
        test_scenario.test_targets = new_test_targets
    }

    def visit_test_scenario(TestScenario test_scenario) {
        this.make_domain_display_order(test_scenario)
        this.make_target_tag(test_scenario)
        // def new_test_targets = new TestTargetSet(name: 'cluster')
        // def test_targets = test_scenario.test_targets
        // this.domain_display_orders.each { domain, display_orders ->
        //     Map sorted_targets = display_orders.sort { a, b -> 
        //         a.value.priority() <=> b.value.priority() 
        //     }
        //     sorted_targets.each {target_name, display_priority ->
        //         println "${target_name}: ${display_priority}"
        //     }
        // }
    }
}

package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import jp.co.toshiba.ITInfra.acceptance.Model.TestTargetSet
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
import jp.co.toshiba.ITInfra.acceptance.Model.RunStatus

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

@Slf4j
@ToString(includePackage = false)
class TagGeneratorManual {
    Map domain_display_orders = [:].withDefault {
        new LinkedHashMap<String,DisplayPriority>()
    }
        
    def set_environment(ConfigTestEnvironment env) {
    }

    def make_domain_display_order(TestScenario test_scenario) {
        // def targets = test_scenario.test_targets.get_all()
        def tags = [:].withDefault{0}
        def row = 0
        // targets.each { target_name, domain_targets ->
        //     domain_targets.each { domain, test_target ->
        List<TestTarget> test_targets = TestTarget.search(test_scenario)
        test_targets.each { test_target ->
            def domain = test_target.domain
            def target_name = test_target.name

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

    def make_target_tag(TestScenario test_scenario) {
        TestTargetSet new_test_targets = new TestTargetSet(name: 'cluster')
        def test_targets = test_scenario.test_targets
        this.domain_display_orders.each { domain, display_orders ->
            Map sorted_targets = display_orders.sort { a, b -> 
                a.value.priority() <=> b.value.priority() 
            }
            DisplayPriority display_priority_last
            TestTarget test_target
            sorted_targets.each {target_name, display_priority ->
                if (display_priority.priority_group != display_priority_last?.priority_group) {
                    if (display_priority_last) {
                        def tag = display_priority_last.tag
                        def compared = test_targets.get(tag, domain)
                        def test_target_tag = compared.clone_test_target_tag(tag)
                        log.info "Create Tag : ${test_target_tag.name}"
                        new_test_targets.add(test_target_tag)
                    }
                    display_priority_last = display_priority
                }
                test_target = test_targets.get(target_name, domain)
                test_target.tag = display_priority.tag
                new_test_targets.add(test_target)
            }
            // Add a tag if the last line is set to compare
            if (test_target.compare_server) {
                def tag = test_target.compare_server
                def compared = test_targets.get(tag, domain)
                def test_target_tag = compared.clone_test_target_tag(tag)
                log.info "Create Tag : ${test_target_tag.name}"
                new_test_targets.add(test_target_tag)
            }
        }
        test_scenario.test_targets = new_test_targets
    }

    def visit_test_scenario(TestScenario test_scenario) {
        this.make_domain_display_order(test_scenario)
        this.make_target_tag(test_scenario)
    }
}

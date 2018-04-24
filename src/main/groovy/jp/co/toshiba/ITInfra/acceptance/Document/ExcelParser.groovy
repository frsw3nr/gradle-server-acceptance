package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

class ExcelParser {
    def bounds

    def visit_test_scenario(test_scenario) {
        println "visit_test_scenario"
        // Excel 本体からシートリストを読み込み、シート名からドメイン識別
        // ドメインテンプレートを生成して登録
        ['Linux', 'Windows'].each { domain_name ->
            def test_domain = new TestDomainTemplate(name: domain_name)
            test_domain.accept(this)
            test_scenario.test_domain_templates[domain_name] = test_domain
        }
    }

    def visit_check_sheet(check_sheet) {
        println "visit_check_sheet ${check_sheet}"
        // Excle シート名を読み込み
        // 順に検索してシート登録

    }

    def visit_test_target(test_target) {
        println "visit_test_target"
    }

    def visit_test_domain(test_domain) {
        println "visit_test_domain"
    }

    def visit_test_rule(test_rule) {
        println "visit_test_rule"
    }

}

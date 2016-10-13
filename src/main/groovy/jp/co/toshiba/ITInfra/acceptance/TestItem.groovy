package jp.co.toshiba.ITInfra.acceptance

class TestItem {

    String  test_id
    String  description
    Boolean enabled

    int succeed = 0
    def results

    TestItem(String test_id) {
        this.test_id = test_id
        this.enabled = true
        this.results = [:]
    }

    def results(String result) {
        this.results[this.test_id] = result
    }

    def results(Map results) {
        this.results = results
    }
}

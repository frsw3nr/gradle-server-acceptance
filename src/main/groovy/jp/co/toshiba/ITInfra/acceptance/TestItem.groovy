package jp.co.toshiba.ITInfra.acceptance

public enum VerifyStatus {
    ok,
    ng,
    unkown,
}

class TestItem {

    String  test_id
    String  description
    Boolean enabled

    int succeed = 0
    def results
    def verify_statuses

    TestItem(String test_id) {
        this.test_id = test_id
        this.enabled = true
        this.results = [:]
        this.verify_statuses = [:]
    }

    def results(String result) {
        this.results[this.test_id] = result
    }

    def results(Map results) {
        this.results = results
    }
}

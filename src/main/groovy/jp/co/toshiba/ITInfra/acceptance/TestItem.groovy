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
    def verify_status
    def device_header = []
    def devices = []

    TestItem(String test_id) {
        this.test_id = test_id
        this.enabled = true
        this.results = [:]
        this.verify_status = [:]
    }

    def results(String result) {
        this.results[this.test_id] = result
    }

    def results(Map results) {
        this.results << results
    }

    def verify_status(Boolean result) {
        this.verify_status[this.test_id] = result
    }

    def verify_status(Map results) {
        this.verify_status << results
    }

    def devices(List csv, List header) {
        this.devices = csv
        this.device_header = header
    }
}

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
    def additional_test_items

    TestItem(String test_id) {
        this.test_id = test_id
        this.enabled = true
        this.results = [:]
        this.verify_status = [:]
        this.additional_test_items = [:]
    }

    def preset_null_status = { Closure closure ->
        if (!results.containsKey(this.test_id)) {
            if (closure.call() == true) {
                this.verify_status[this.test_id] = false
            }
        }
    }

    def results(String result) {
        preset_null_status() {
            (result == '[:]' || result.size() == 0)
        }
        this.results[this.test_id] = result
    }

    def results(Map results_in) {
        preset_null_status() {
            (results_in.size() == 0)
        }
        this.results << results_in
    }

    def verify_status(Boolean result) {
        this.verify_status[this.test_id] = result
    }

    def verify_status(Map results) {
        this.verify_status << results
    }

    def devices(HashMap settings = [:], List csv, List header) {
        this.devices = csv
        this.device_header = header
    }
}

package jp.co.toshiba.ITInfra.acceptance

class TestItem {

    String  test_id
    String  description
    Boolean enabled

    int    rc
    String result
    String results = [:]

    TestItem(String test_id) {
        this.test_id = test_id
    }

    def results(String result) {

    }

    def put_results(Map results) {

    }
}

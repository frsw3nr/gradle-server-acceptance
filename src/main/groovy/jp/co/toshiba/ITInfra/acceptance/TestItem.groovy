package jp.co.toshiba.ITInfra.acceptance

class TestItem {

    String  test_id
    String  description
    Boolean enabled

    int    rc
    String result

    TestItem(String test_id) {
        this.test_id = test_id
    }
}

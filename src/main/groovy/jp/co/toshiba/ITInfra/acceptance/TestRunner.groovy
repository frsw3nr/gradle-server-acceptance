package jp.co.toshiba.ITInfra.acceptance

class TestRunner {

    final created
    def owner
    EvidenceSheet evidence

    TestRunner() {
        created = new Date().format("yyyyMMdd-HHmmss")
    }

    Boolean readEvidence() {
        //
    }

    Boolean writeEvidence() {
        //
    }

    Boolean runTest() {
        //
    }

    static void main(String[] args) {
        def test = new TestRunner()
        println test.created
    }
}

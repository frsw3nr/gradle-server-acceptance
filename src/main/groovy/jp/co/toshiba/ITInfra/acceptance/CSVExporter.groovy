package jp.co.toshiba.ITInfra.acceptance

import groovy.json.*
import static groovy.json.JsonOutput.*
import groovy.util.logging.Slf4j

@Slf4j
class CSVExporter {

    TestRunner test_runner
    ConfigObject test_results = new ConfigObject()
    def sheet_files
    def report_file

    CSVExporter(TestRunner test_runner) {
        this.test_runner = test_runner
    }

    def readTestResultSheet(String sheet_files) {
        sheet_files.each { sheet_file ->
            evidence_sheet = new EvidenceSheet(test_runner.config_file)
            evidence_sheet.evidence_source = sheet_file
            evidence_sheet.readAllTestResults(test_results)
        }
    }

    def exportTestResult() {

    }

    def run() {

    }
}

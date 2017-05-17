package jp.co.toshiba.ITInfra.acceptance

import groovy.json.*
import static groovy.json.JsonOutput.*
import groovy.util.logging.Slf4j
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVFormat

@Slf4j
class CSVExporter {

    TestRunner test_runner
    def csv
    def csv_export

    CSVExporter(TestRunner test_runner) {
        this.test_runner = test_runner
        def config = Config.instance.read(test_runner.config_file)
        if (!config?.evidence?.csv_export)
            log.warn "Parameter 'config.evidence.csv_export' not found in config.groovy. Use default : 'build/compare.csv'."
        csv_export = config?.evidence?.csv_export ?: 'build/compare.csv'
    }

    def readTestResultSheet(List sheet_files) {
        csv = []
        sheet_files.each { sheet_file ->
            log.info "Read '${sheet_file}'"
            def evidence = new EvidenceSheet(test_runner.config_file)
            evidence.evidence_source = sheet_file
            def csv_evidence = evidence.readAllTestResult()
            if (csv_evidence)
                csv += csv_evidence
        }
        return csv
    }

    def exportTestResult() throws IOException {
        if (csv.size() == 0) {
            throw new IllegalArgumentException("No test result data")
        }
        log.info "Export ${csv_export}"
        def target_dir = new File(csv_export).getParentFile()
        target_dir.mkdirs()
        def writer = new PrintWriter(csv_export)
        CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL)
        csv.each {
            printer.printRecord(it)
        }
        printer.close()
    }

    def run() {
        if (!test_runner.export_files) {
            log.error "Malformed input --export"
            return false
        }
        List sheet_files = test_runner.export_files.split(/,/)
        csv = this.readTestResultSheet(sheet_files)
        this.exportTestResult()
    }
}

package jp.co.toshiba.ITInfra.acceptance

class EvidenceSheet {

    def evidence_source   = 'check_sheet.xlsx'
    def evidence_target   = 'build/check_sheet.xlsx'
    def sheet_name_server = 'Target'
    Map sheet_name_checks = [
        'Linux':   'Check(Linux)',
        'Windows': 'Check(Windows)',
    ]

    def config_file  = 'config/config.groovy'
    def platforms    = [:]
    def domains      = [:]

    TestServer test_servers = [:]
    TestItem   test_specs   = [:].withDefault([:]).withDefault([:])

    def readServerSheet() {
    }

    def readSpecSheet() {
    }


}

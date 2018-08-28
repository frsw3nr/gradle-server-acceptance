package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
import org.apache.commons.compress.compressors.gzip.*
import org.apache.commons.compress.archivers.tar.*
import org.apache.commons.compress.utils.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*
import com.xlson.groovycsv.CsvParser

@Slf4j
@InheritConstructors
class HitachiVSPSpec extends WindowsSpecBase {

    String ip
    String report_dir
    final tar_buffer_size = 64 * 1024

    def init() {
        super.init()

        this.ip           = test_platform.test_target.ip ?: 'unkown'
        def os_account    = test_platform.os_account
        this.report_dir  = os_account['report_dir']
    }

    def finish() {
        super.finish()
    }

    def extract_config_report_tgz(source, target) throws IOException {
        log.info "Extract config report tgz : ${source}, target : ${target}"
        def gzip_io = new GzipCompressorInputStream(new FileInputStream(source))
        new BufferedInputStream(gzip_io).with { gzip ->
            new TarArchiveInputStream(gzip, tar_buffer_size).with { tar ->
                def tarEntry
                while ((tarEntry = tar.nextTarEntry) != null) {
                    def entryFile = new File("${target}/${tarEntry.name}")
                    if (tarEntry.isDirectory()) {
                        entryFile.mkdirs()
                    }
                    else {
                        if (!entryFile.parentFile.exists()) {
                            entryFile.parentFile.mkdirs()
                        }
                        entryFile.withOutputStream { outputStream ->
                            IOUtils.copy(tar, outputStream, tar_buffer_size)
                        }
                        entryFile.with {
                            lastModified = tarEntry.modTime.time
                        }
                    }
                }
            }
        }
    }

    // \build\log\HitachiVSP\<サーバ>\HitachiVSP の下に
    // レポート構成ファイル(tgz)をダウンロード。
    // ファイル名は、 hitachi_vsp_raidinf_report.tgz とする。
    // 本ファイルを指定ディレクトリに解凍する。
    // this.report_dir の指定を解凍先に変える。
    // あとはテスト項目リスト順にテストを行う

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()

        def script_path = './lib/template/get_HitachiVSP_spec.ps1'
        def cmd = """\
            |powershell -NonInteractive ${script_path}
            |-log_dir '${local_dir}'
            |-ip '${ip}' -server '${server_name}'
            |-user '${os_user}' -password '${os_password}'
        """.stripMargin()
        log.info "Execute command:\n${cmd}"
        execPowerShell(script_path, cmd)
        def report_tar = 'hitachi_vsp_raidinf_report.tgz'
        def report_dir = (dry_run) ?
            "${dry_run_staging_dir}/${server_name}/${platform}" :
            local_dir
        extract_config_report_tgz("${report_dir}/${report_tar}", local_dir)
        test_items.each {
            def method = this.metaClass.getMetaMethod(it.test_id, TestItem)
            if (method) {
                log.debug "Invoke command '${method.name}()'"
                try {
                    long start = System.currentTimeMillis();
                    method.invoke(this, it)
                    long elapsed = System.currentTimeMillis() - start
                    log.debug "Finish test method '${method.name}()' in ${this.server_name}, Elapsed : ${elapsed} ms"
                    // it.succeed = 1
                } catch (Exception e) {
                    it.status(false)
                    log.error "[Test] Test method '${method.name}()' faild, skip.\n" + e
                }
            }
        }
    }

    def parse_csv(TestItem test_item, String test_id, List headers) throws IOException {
        def csv_file = new File("${local_dir}/CSV/${test_id}.csv")
        String[] lines = csv_file.readLines()
        def csv = []
        def row = 0
        lines.each {
            if (row > 1) {
                String[] columns = it.split(/,/)
                csv << columns
            }
            row ++
        }
        test_item.devices(csv, headers)
        test_item.results(csv.size().toString())
        return csv
    }

    def CacheInfo(TestItem test_item) {
        def headers = ['Location', 'CMG#0 Size(GB)', 'Cache Size(GB)', 'SM Size(MB)', 'CFM#0 Type']
        def csv = parse_csv(test_item, 'CacheInfo', headers)
        def infos = []
        csv.each { row ->
            infos << ['Location' : row[0], 'CMG#0' : row[1], 'Cache' : row[2], 'SM' : row[3]]
        }
        test_item.results(infos.toString())
    }

    def DkcInfo(TestItem test_item) {
        def headers = ['Storage System Type', 'Serial Number#', 'IP Address', 'Subnet Mask', 'Number of CUs', 'Number of DKBs', 'Configuration Type', 'Model']
        def csv = parse_csv(test_item, 'DkcInfo', headers)
        def infos = [:].withDefault{[]}
        def networks = [:]
        def net_subnet = [:]
        def rownum = 0
        csv.each { row ->
            def colnum = 0
            ['Type', 'Serial', 'IP', 'Subnet'].each { metric ->
                infos[metric] << row[colnum]
                if (metric == 'IP')
                    networks[rownum] = row[colnum]
                if (metric == 'Subnet')
                    net_subnet[rownum] = row[colnum]
                colnum ++
            }
            rownum ++
        }
        test_item.results(['DkcInfo':infos.toString(), 
                          'Type':infos['Type'], 'Serial':infos['Serial'],
                          'networks':networks.toString(), 'net_subnet':net_subnet.toString()])
    }

    def LdevInfo(TestItem test_item) {
        def headers = ['ECC Group','LDEV#','LDEV Name','LDEV Emulation','LDEV Type','LDEV Attribute','Volume Size(Cyl)','Volume Size(MB)','Volume Size(Blocks)','CVS','Pool ID','RAID Concatenation#0','RAID Concatenation#1','RAID Concatenation#2','ORACLE CHECK SUM','Current MPU','Setting MPU','Allocated','Pool Name','CmdDevSecurity','CmdDevUserAuth','CmdDevDevGrpDef','Resource Group ID (LDEV)','Resource Group Name (LDEV)','Encryption','T10 PI','ALUA Mode','Accelerated Compression']
        def csv_file = new File("${local_dir}/CSV/LdevInfo.csv")
        String[] lines = csv_file.readLines()
        def csv = parse_csv(test_item, 'LdevInfo', headers)
        def infos = [:].withDefault{0}
        csv.each { row ->
            def volume_size = 'Volume ' + row[7] + ' MB'
            infos[volume_size] += 1
        }
        test_item.results(infos.toString())
    }

    def LPartition(TestItem test_item) {
        def headers = ['CLPR#','CLPR Name','Cache Size(MB)','ECC Group','LDEV#(V-VOL)']
        def csv = parse_csv(test_item, 'LPartition', headers)
        def infos = [:].withDefault{0}
        csv.each { row ->
            def volume_size = 'Cache ' + row[2] + ' MB'
            infos[volume_size] += 1
        }
        test_item.results(infos.toString())
    }

    def LunInfo(TestItem test_item) {
        def headers = ['Port','Host Group','Host Mode','Host Mode Option','LUN#','LDEV#','Command Device','Command Security','CVS','CHB Location','Package Type','Resource Group ID (Host Group)','Resource Group Name (Host Group)','T10 PI Mode','T10 PI','Asymmetric Access State']
        parse_csv(test_item, 'LunInfo', headers)
    }

    def MicroVersion(TestItem test_item) {
        def headers = ['DKCMAIN','ROM BOOT','RAM BOOT','Config','HDD','Expander','CFM','DKB','Printout Tool','CHB(FC16G)','CHB(iSCSI)','GUM','Unified Hypervisor','NASFWINST','NASFW']
        def csv = parse_csv(test_item, 'MicroVersion', headers)
        def infos = [:]
        csv.each { row ->
            if (row[0].size() > 0)
                infos['DKCMAIN'] = row[0]
        }
        test_item.results(infos.toString())
    }

    def PdevInfo(TestItem test_item) {
        def headers = ['ECC Group', 'Emulation Type', 'CR#', 'PDEV Location', 'Device Type', 'RPM', 'Device Type-Code', 'Device Size', 'Device Capacity', 'Drive Version', 'DKB1', 'DKB2', 'Serial Number#', 'RAID Level', 'RAID Concatenation#0', 'RAID Concatenation#1', 'RAID Concatenation#2', 'Resource Group ID (ECC Group)', 'Resource Group Name (ECC Group)', 'Encryption', 'Accelerated Compression']
        def csv = parse_csv(test_item, 'PdevInfo', headers)
        def infos = [:].withDefault{[:].withDefault{[:].withDefault{0}}}
        csv.each { row ->
            infos['ECC' + row[0]][row[13]][row[8]] += 1
        }
        test_item.results(infos.toString())
    }

    def SsdDriveInfo(TestItem test_item) {
        def headers = ['ECC Group','CR#','PDEV Location','Device Type-Code','Device Capacity','SSD Device Type','Used Endurance Indicator (%)','Used Endurance Indicator Threshold (%)','Used Endurance Indicator Warning SIM (%)','FMD Battery Life Indicator Warning SIM (%)','FMD Battery Life Indicator (%)']
        parse_csv(test_item, 'SsdDriveInfo', headers)
    }

    // def DkcInfo(TestItem test_item) {
    //     def csv_file = new File("${this.report_dir}/${this.server_name}/CSV/DkcInfo.csv")
    //     String[] lines = csv_file.readLines()

    //     def csv = []
    //     def row = 0
    //     lines.each {
    //         if (row > 1) {
    //             String[] columns = it.split(/,/)
    //             csv << columns
    //         }
    //         row ++
    //     }
    //     def headers = ['Storage System Type', 'Serial Number#', 'IP Address', 'Subnet Mask', 'Number of CUs', 'Number of DKBs', 'Configuration Type', 'Model']
    //     test_item.devices(csv, headers)
    //     test_item.results(csv.size().toString())
    // }

}

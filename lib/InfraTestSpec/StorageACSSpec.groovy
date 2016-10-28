package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.hidetake.groovy.ssh.Ssh
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
@InheritConstructors
class StorageACSSpec extends InfraTestSpec {

    String server_name
    String ip
    String remote_ip
    String remote_user
    String remote_password
    String remote_alias
    String work_dir
    String script_path
    Boolean is_remote
    private String csv_source_line
    private int position = 0

    def init() {
        super.init()
        server_name = test_server.server_name
        if (test_server.remote_account) {
            def remote_account = test_server.remote_account
            remote_ip       = remote_account['server']
            remote_user     = remote_account['user']
            remote_password = remote_account['password']
            remote_alias    = test_server.remote_alias
            work_dir        = '/tmp/gradle-test'
            is_remote       = true
        } else {
            is_remote       = false
        }
        script_path = local_dir + '/get_StorageACS_spec.ps1'
    }

    def finish() {
        super.finish()
    }

    def remote_exec(TestItem[] test_items) {
        def ssh = Ssh.newService()
        ssh.remotes {
            ssh_host {
                host = this.remote_ip
                port = 22
                user = this.remote_user
                password = this.remote_password
                knownHosts = allowAnyHosts
            }
        }
        ssh.settings {
            dryRun = this.dry_run
        }
        ssh.run {
            session(ssh.remotes.ssh_host) {
                try {
                    execute "mkdir -vp ${work_dir}"
                } catch (Exception e) {
                    log.error "[SSH Test] Failed to create '$work_dir' in ${this.server_name}, skip.\n" + e
                    return
                }

                test_items.each {
                    def method = this.metaClass.getMetaMethod(it.test_id, Object, TestItem)
                    if (method) {
                        log.debug "Invoke command '${method.name}()'"
                        try {
                            long start = System.currentTimeMillis();
                            method.invoke(this, delegate, it)
                            long elapsed = System.currentTimeMillis() - start
                            log.info "Finish test method '${method.name}()' in ${this.server_name}, Elapsed : ${elapsed} ms"
                            it.succeed = 1
                        } catch (Exception e) {
                            log.error "[SSH Test] Test method '${method.name}()' faild, skip.\n" + e
                        }
                    }
                }
                try {
                    remove work_dir
                } catch (Exception e) {
                    log.error "[SSH Test] Failed to remove '$work_dir' in ${this.server_name} faild, skip.\n" + e
                    return
                }
            }
        }
    }

    def local_exec(TestItem[] test_items) {
        test_items.each {
            def method = this.metaClass.getMetaMethod(it.test_id, Object, TestItem)
            if (method) {
                log.debug "Invoke command '${method.name}()'"
                try {
                    long start = System.currentTimeMillis();
                    method.invoke(this, null, it)
                    long elapsed = System.currentTimeMillis() - start
                    log.info "Finish test method '${method.name}()' in ${this.server_name}, Elapsed : ${elapsed} ms"
                    it.succeed = 1
                } catch (Exception e) {
                    log.error "[SSH Test] Test method '${method.name}()' faild, skip.\n" + e
                }
            }
        }
    }

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()
        if (is_remote) {
            remote_exec(test_items)
        } else {
            local_exec(test_items)
        }
    }

    def tsuacs(session, test_item) {
        def lines = exec('tsuacs') {
            // run_ssh_command(session, '/bin/lsblk -i', 'tsuacs')
        }

        def is_target = false
        def storage_infos = [:]
        def csv = []
        def is_csv = false
        lines.eachLine {
            // === model: SC3000 system ID: 0xffffffff3 system code: KC0003 ===
            (it =~ /^=== model: (.+?) system ID: (.+?) system code: (.+?) ===/).each {m0, m1, m2, m3->
                if (is_target) {
                    return
                }
                if (m3 == server_name) {
                    is_target = true
                    storage_infos['model']       = m1
                    storage_infos['system_id']   = m2
                    storage_infos['system_code'] = m3
                }
            }
            // host interface  : FC 8Gbps Optical 2-Port
            (it =~ /host interface\s+: (.+?)$/).each {m0, m1->
                if (is_target) {
                    storage_infos['host_interface'] = m1
                }
            }
            // cache size      : 5379MB
            (it =~ /cache size\s+: (.+?)$/).each {m0, m1->
                if (is_target) {
                    storage_infos['cache_size'] = m1
                }
            }
            // FW revision     : 1135
            (it =~ /FW revision\s+: (.+?)$/).each {m0, m1->
                if (is_target) {
                    storage_infos['fw_version'] = m1
                }
            }
            // device                LDISK   WBC     RAIDgr. level   HDD     stripe
            (it =~ /^device\s+/).each {m0 ->
                is_csv = true
            }
            if (is_csv && is_target) {
                if (is_csv && !(it =~/^device/) && it.size() > 0) {
                    FixedLengthStringParser parser = new FixedLengthStringParser(it)
                    csv << [
                        parser.next(22),
                        parser.next(8),
                        parser.next(8),
                        parser.next(8),
                        parser.next(8),
                        parser.next(8),
                        parser.next(64),
                    ]
                }
                if (it.size() == 0) {
                    is_csv = false
                }
            }
        }
        def headers = ['device', 'ldisk', 'wbc', 'raid_gr', 'level', 'hddd', 'stripe']
        test_item.devices(csv, headers)
        test_item.results(storage_infos)
    }

    def afacs(session, test_item) {
        def lines = exec('afacs') {
            // run_ssh_command(session, '/bin/lsblk -i', 'tsuacs')
        }

        def is_target = false
        def storage_infos = [:]
        def csv = []
        def is_csv = false
        lines.eachLine {
            // === model: AF7500 system ID: 0x26c61ce5 system code: KB0001 ===
            (it =~ /^=== model: (.+?) system ID: (.+?) system code: (.+?) ===/).each {m0, m1, m2, m3->
                if (m3 == server_name) {
                    is_target = true
                    storage_infos['model']       = m1
                    storage_infos['system_id']   = m2
                    storage_infos['system_code'] = m3
                } else {
                    is_target = false
                }
            }
            // host interface  : FC 8Gbps Optical 2-Port
            (it =~ /host interface\s+: (.+?)$/).each {m0, m1->
                if (is_target) {
                    storage_infos['host_interface'] = m1
                }
            }
            // cache size      : 5379MB
            (it =~ /cache size\s+: (.+?)$/).each {m0, m1->
                if (is_target) {
                    storage_infos['cache_size'] = m1
                }
            }
            // FW revision     : 1135
            (it =~ /FW revision\s+: (.+?)$/).each {m0, m1->
                if (is_target) {
                    storage_infos['fw_version'] = m1
                }
            }

            // device                LDISK   WBC     RAIDgr. level   HDD     stripe
            (it =~ /^device\s+/).each {m0 ->
                is_csv = true
            }
            if (is_csv) {
                if (is_target && !(it =~/^device/) && it.size() > 0) {
                    FixedLengthStringParser parser = new FixedLengthStringParser(it)
                    csv << [
                        parser.next(53),
                        parser.next(8),
                        parser.next(12),
                        parser.next(12),
                        parser.next(8),
                        parser.next(8),
                        parser.next(64),
                    ]
                }
                if (it.size() == 0) {
                    is_csv = false
                }
            }
        }
        def headers = ['device', 'ldisk', 'wbc', 'raid_gr', 'level', 'hddd', 'stripe']
        test_item.devices(csv, headers)
        test_item.results(storage_infos)
    }
}

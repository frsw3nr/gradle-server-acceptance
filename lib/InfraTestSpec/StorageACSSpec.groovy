package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.hidetake.groovy.ssh.Ssh
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*

@Slf4j
@InheritConstructors
class StorageACSSpec extends InfraTestSpec {

    def init() {
        super.init()
    }

    def finish() {
        super.finish()
    }

    def setup_exec(TestItem[] test_items) {
        super.setup_exec()

        def ssh = Ssh.newService()
        ssh.remotes {
            ssh_host {
                host = this.ip
                port = 22
                user = this.os_user
                password = this.os_password
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

    def tsuacs(session, test_item) {
        def lines = exec('tsuacs') {
            run_ssh_command(session, '/bin/lsblk -i', 'tsuacs')
        }

        def csv = []
        lines.eachLine {
            (it =~  /^(.+?)\s+(\d+:\d+\s.+)$/).each { m0,m1,m2->
                def device = m1
                def arr = [device]
                arr.addAll(m2.split(/\s+/))
                csv << arr
            }
            // link/ether 00:0c:29:c2:69:4b brd ff:ff:ff:ff:ff:ff promiscuity 0
            (it =~ /link\/ether\s+(.*?)\s/).each {m0, m1->
                hw_address.add(m1)
            }
        }
        def headers = ['name', 'maj:min', 'rm', 'size', 'ro', 'type', 'mountpoint']
        test_item.devices(csv, headers)

        test_item.results(lines)
    }

    def afacs(session, test_item) {
        def lines = exec('tsuacs') {
            run_ssh_command(session, '/bin/lsblk -i', 'tsuacs')
        }

        def csv = []
        lines.eachLine {
            (it =~  /^(.+?)\s+(\d+:\d+\s.+)$/).each { m0,m1,m2->
                def device = m1
                def arr = [device]
                arr.addAll(m2.split(/\s+/))
                csv << arr
            }
            // link/ether 00:0c:29:c2:69:4b brd ff:ff:ff:ff:ff:ff promiscuity 0
            (it =~ /link\/ether\s+(.*?)\s/).each {m0, m1->
                hw_address.add(m1)
            }
        }
        def headers = ['name', 'maj:min', 'rm', 'size', 'ro', 'type', 'mountpoint']
        test_item.devices(csv, headers)

        test_item.results(lines)
    }
}

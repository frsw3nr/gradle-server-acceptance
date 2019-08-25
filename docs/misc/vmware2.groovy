@GrabConfig( systemClassLoader=true )
@Grapes( [
    @Grab(group='com.toastcoders', module='yavijava', version='6.0.05'),
])

import com.vmware.vim25.*
import com.vmware.vim25.mo.*

def ip          = System.getenv("TEST_IP") ?: '192.168.0.13'
def os_user     = System.getenv("TEST_OS_USER") ?: 'someuser'
def os_password = System.getenv("TEST_OS_PASSWORD") ?: 'P@ssw0rd'

// VMWare ESXiに接続
host = "https://${ip}/sdk"
si = new ServiceInstance(new URL(host), os_user, os_password, true)
rf = si.getRootFolder()

// 仮想マシンのVMWare Toolsのバージョンを一覧表示
sc = [["VirtualMachine", "name"]] as String[][]
vms = new InventoryNavigator(rf).searchManagedEntities(sc, true)
for( vm in vms ){
  println "${vm.name}:${vm.config?.tools?.toolsVersion}"
}
si.getServerConnection().logout()

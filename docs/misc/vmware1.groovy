@GrabConfig( systemClassLoader=true )
@Grapes( [
    // @Grab(group='com.vmware', module='vijava', version='5.1'),
// https://mvnrepository.com/artifact/com.toastcoders/yavijava
    @Grab(group='com.toastcoders', module='yavijava', version='6.0.01'),
])

import java.net.URL;
import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;
def ip          = System.getenv("TEST_IP") ?: '192.168.0.13'
def os_user     = System.getenv("TEST_OS_USER") ?: 'someuser'
def os_password = System.getenv("TEST_OS_PASSWORD") ?: 'P@ssw0rd'

println "Connect: ${ip}"

long start = System.currentTimeMillis();
ServiceInstance si = new ServiceInstance(new URL("https://${ip}/sdk"), os_user, os_password, true);
long end = System.currentTimeMillis();
System.out.println("time taken:" + (end-start));
// Folder rootFolder = si.getRootFolder();
// String name = rootFolder.getName();
// System.out.println("root:" + name);
// ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
// if(mes==null || mes.length ==0)
// {
//     return;
// }

// VirtualMachine vm = (VirtualMachine) mes[0]; 

// VirtualMachineConfigInfo vminfo = vm.getConfig();
// VirtualMachineCapability vmc = vm.getCapability();

// vm.getResourcePool();
// System.out.println("Hello " + vm.getName());
// System.out.println("GuestOS: " + vminfo.getGuestFullName());
// System.out.println("Multiple snapshot supported: " + vmc.isMultipleSnapshotsSupported());

// si.getServerConnection().logout();

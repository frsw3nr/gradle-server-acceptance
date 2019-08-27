@GrabConfig( systemClassLoader=true )
@Grapes( [
    @Grab(group='com.toastcoders', module='yavijava', version='6.0.05'),
])

import com.vmware.vim25.ws.CustomSSLTrustContextCreator;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.X509TrustManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import com.vmware.vim25.*
import com.vmware.vim25.mo.*

// https://github.com/yavijava/yavijava/issues/115

// I also observed this in 2 VCs, one 6.0 and other 6.5. When I use IP address in 6.0, 
// I see this error. I rerun the tests with DNS name, the same test passed. However, 
// in 6.5 VC instance, it doesn't work with both ip and DNS name. Moreover, 
// in this 6.5 instance, self signed certs were little skewed. Let me regenerate the 
// certificate and try.
// Having said that, HostnameVerifier should handle this. I remember it used to work 
// fine with older versions of jdk. I don't know if some part of HostnameVerifier api 
// has deprecated or the way we are using with later versions of java is not backward 
// compatible.
// Meanwhile, I tried in one more 6.5 instance, it works with DNS, but not with IP.

//  @carterg
 
// carterg commented on 10 Jan 2018
// I'm seeing this issue as well in 6.5 when using the IP address

// TrustAllManager trustAllManager = new TrustAllManager();
// ServiceInstance si = new ServiceInstance(new URL(LoadVcenterProps.url),
//         LoadVcenterProps.userName, LoadVcenterProps.password, trustAllManager);

class TrustAllManager implements X509TrustManager {
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType) {
    }

    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType) {
    }
}

def trustManager = new TrustAllManager()
println trustManager.getClass()

def ip          = System.getenv("TEST_IP") ?: '192.168.0.13'
def os_user     = System.getenv("TEST_OS_USER") ?: 'someuser'
def os_password = System.getenv("TEST_OS_PASSWORD") ?: 'P@ssw0rd'

// VMWare ESXiに接続
host = "https://${ip}/sdk"
// si = new ServiceInstance(new URL(host), os_user, os_password, true)
si = new ServiceInstance(new URL(host), os_user, os_password, trustManager)
rf = si.getRootFolder()

// 仮想マシンのVMWare Toolsのバージョンを一覧表示
sc = [["VirtualMachine", "name"]] as String[][]
vms = new InventoryNavigator(rf).searchManagedEntities(sc, true)
for( vm in vms ){
  println "${vm.name}:${vm.config?.tools?.toolsVersion}"
}
si.getServerConnection().logout()

@GrabConfig( systemClassLoader=true )
@Grapes( 
@Grab(group='com.goebl', module='david-webb', version='1.3.0'))


import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.*;
import com.goebl.david.Webb

// wget --no-proxy https://localhost:57443/
// wget --no-proxy https://10.46.15.203:57443/  --no-check-certificate 
// wget --no-proxy https://10.46.15.203:57443/ --ca-certificate=ca.crt
// wget --no-proxy https://10.152.32.104:57443/ --ca-certificate=ca.crt

// wget https://www.googleapis.com/oauth2/v1/certs 

Webb webb = Webb.create()
webb.setBaseUri(null);

class TrustingHostnameVerifier implements HostnameVerifier {
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}

// class AlwaysTrustManager implements X509TrustManager {
//     public void checkClientTrusted(X509Certificate[] arg0, String arg1) { }
//     public void checkServerTrusted(X509Certificate[] arg0, String arg1) { }
//     public X509Certificate[] getAcceptedIssuers() { return null; }
// }

// // TrustManager[] trustAllCerts = new TrustManager[] { new AlwaysTrustManager() };
// TrustManager trustAllCerts = new TrustManager() { new AlwaysTrustManager() };
// TrustManager trustAllCerts = new AlwaysTrustManager();
def trustAllCerts = [
    new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return null
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }
    }
] as TrustManager[] 
SSLContext sslContext = SSLContext.getInstance("TLS");
sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

webb.setSSLSocketFactory(sslContext.getSocketFactory());
webb.setHostnameVerifier(new TrustingHostnameVerifier());

// def response = webb.get("https://10.152.32.104:57443/").asVoid();
// testHttpsValidCertificate
// def response = webb.get("https://www.googleapis.com/oauth2/v1/certs").asVoid();
// testHttpsInvalidCertificate
// def response = webb.get("https://tv.eurosport.com/").asVoid();
// testHttpsInvalidCertificateAndHostnameIgnore
// def response = webb.get("https://10.152.32.104:57443/").asVoid();
def response = webb.get("https://localhost/sysmgr/").asVoid();

println response.isSuccess()
println response.getStatusCode()


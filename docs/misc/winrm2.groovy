@GrabConfig( systemClassLoader=true )
@Grapes( [
@Grab(group='io.cloudsoft.windows', module='winrm4j', version='0.7.0'),
])

import org.apache.http.client.config.*;
import io.cloudsoft.winrm4j.client.*;
import io.cloudsoft.winrm4j.winrm.*;

def ip          = System.getenv("TEST_IP") ?: '192.168.0.13'
def os_user     = System.getenv("TEST_OS_USER") ?: 'someuser'
def os_password = System.getenv("TEST_OS_PASSWORD") ?: 'P@ssw0rd'

WinRmClientContext context = WinRmClientContext.newInstance();

WinRmTool tool = WinRmTool.Builder.builder(ip, os_user, os_password)
    .authenticationScheme(AuthSchemes.NTLM)
    .port(5985)
    .useHttps(false)
    .context(context)
    .build();

tool.executePs("echo hi");

context.shutdown();

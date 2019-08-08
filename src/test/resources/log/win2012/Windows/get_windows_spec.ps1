Param(
    [string]$log_dir
  , [string]$ip
  , [string]$server
  , [string]$user
  , [string]$password
)
$log_dir = Convert-Path $log_dir
$secure   = ConvertTo-SecureString $password -asplaintext -force
$cred     = New-Object System.Management.Automation.PsCredential $user, $secure

$ErrorActionPreference = "Stop"
$session = $null
try {
    $script:session  = New-PSSession $ip -Credential $cred
} catch [Exception] {
    Write-Error "$error"
    exit 1
}
$ErrorActionPreference = "Continue"

$log_path = Join-Path $log_dir "system"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject -Class Win32_ComputerSystem `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "os_conf"
Invoke-Command -Session $session -ScriptBlock { `
    Get-ItemProperty "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion" | `
    Format-List `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "os"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_OperatingSystem | `
    Format-List Caption,CSDVersion,ProductType,OSArchitecture `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "driver"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_PnPSignedDriver `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "fips"
Invoke-Command -Session $session -ScriptBlock { `
    Get-Item "HKLM:System\CurrentControlSet\Control\Lsa\FIPSAlgorithmPolicy" `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "virturalization"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject -Class Win32_ComputerSystem | Select Model | FL `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "storage_timeout"
Invoke-Command -Session $session -ScriptBlock { `
    Get-ItemProperty "HKLM:SYSTEM\CurrentControlSet\Services\disk" `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "monitor"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_DesktopMonitor | FL `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "ie_version"
Invoke-Command -Session $session -ScriptBlock { `
    Get-ItemProperty "HKLM:SOFTWARE\Microsoft\Internet Explorer" `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "system_log"
Invoke-Command -Session $session -ScriptBlock { `
    Get-EventLog system | Where-Object { $_.EntryType -eq "Error" } | FL `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "apps_log"
Invoke-Command -Session $session -ScriptBlock { `
    Get-EventLog application | Where-Object { $_.EntryType -eq "Error" } | FL `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "ntp"
Invoke-Command -Session $session -ScriptBlock { `
    (Get-Item "HKLM:System\CurrentControlSet\Services\W32Time\Parameters").GetValue("NtpServer") `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "user_account_control"
Invoke-Command -Session $session -ScriptBlock { `
    Get-ItemProperty "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Policies\System" `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "remote_desktop"
Invoke-Command -Session $session -ScriptBlock { `
    (Get-Item "HKLM:System\CurrentControlSet\Control\Terminal Server").GetValue("fDenyTSConnections") `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "cpu"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject -Class Win32_Processor | Format-List DeviceID, Name, MaxClockSpeed, SocketDesignation, NumberOfCores, NumberOfLogicalProcessors `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "memory"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_OperatingSystem | `
    select TotalVirtualMemorySize,TotalVisibleMemorySize, `
        FreePhysicalMemory,FreeVirtualMemory,FreeSpaceInPagingFiles `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "dns"
Invoke-Command -Session $session -ScriptBlock { `
    Get-DnsClientServerAddress|FL `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "nic_teaming_config"
Invoke-Command -Session $session -ScriptBlock { `
    Get-NetLbfoTeamNic `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "nic_teaming"
Invoke-Command -Session $session -ScriptBlock { `
    Get-NetLbfoTeamNic `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "tcp"
Invoke-Command -Session $session -ScriptBlock { `
    Get-ItemProperty "HKLM:SYSTEM\CurrentControlSet\Services\Tcpip\Parameters" | `
    Format-List `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "network"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_NetworkAdapterConfiguration | `
 Where{$_.IpEnabled -Match "True"} | `
 Select ServiceName, MacAddress, IPAddress, DefaultIPGateway, Description, IPSubnet | `
 Format-List ` `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "network_profile"
Invoke-Command -Session $session -ScriptBlock { `
    Get-NetConnectionProfile `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "net_bind"
Invoke-Command -Session $session -ScriptBlock { `
    Get-NetAdapterBinding | FL `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "net_ip"
Invoke-Command -Session $session -ScriptBlock { `
    Get-NetIPInterface | FL `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "firewall"
Invoke-Command -Session $session -ScriptBlock { `
    Get-NetFirewallRule -Direction Inbound -Enabled True `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "filesystem"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_LogicalDisk | Format-List * `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "user"
Invoke-Command -Session $session -ScriptBlock { `
    
$result = @()
$accountObjList =  Get-CimInstance -ClassName Win32_Account
$userObjList = Get-CimInstance -ClassName Win32_UserAccount
foreach($userObj in $userObjList)
{
    $IsLocalAccount = ($userObjList | ?{$_.SID -eq $userObj.SID}).LocalAccount
    if($IsLocalAccount)
    {
        $query = "WinNT://{0}/{1},user" -F $env:COMPUTERNAME,$userObj.Name
        $dirObj = New-Object -TypeName System.DirectoryServices.DirectoryEntry -ArgumentList $query
        $UserFlags = $dirObj.InvokeGet("UserFlags")
        $DontExpirePasswd = [boolean]($UserFlags -band 0x10000)
        $AccountDisable   = [boolean]($UserFlags -band 0x2)
        $obj = New-Object -TypeName PsObject
        Add-Member -InputObject $obj -MemberType NoteProperty -Name "UserName" -Value $userObj.Name
        Add-Member -InputObject $obj -MemberType NoteProperty -Name "DontExpirePasswd" -Value $DontExpirePasswd
        Add-Member -InputObject $obj -MemberType NoteProperty -Name "AccountDisable" -Value $AccountDisable
        Add-Member -InputObject $obj -MemberType NoteProperty -Name "SID" -Value $userObj.SID
        $result += $obj
    }
}
$result | Format-List `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "etc_hosts"
Invoke-Command -Session $session -ScriptBlock { `
    Get-Content "$($env:windir)\system32\Drivers\etc\hosts" `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "patch_lists"
Invoke-Command -Session $session -ScriptBlock { `
    wmic qfe `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "service"
Invoke-Command -Session $session -ScriptBlock { `
    Get-Service | FL `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "packages"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_Product | `
    Select-Object Name, Vendor, Version | `
    Format-List
Get-ChildItem -Path( `
  'HKLM:SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall', `
  'HKCU:SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall') | `
  % { Get-ItemProperty $_.PsPath | Select-Object DisplayName, Publisher, DisplayVersion } | `
  Format-List `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "feature"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WindowsFeature | ?{$_.InstallState -eq [Microsoft.Windows.ServerManager.Commands.InstallState]::Installed} | FL `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "task_scheduler"
Invoke-Command -Session $session -ScriptBlock { `
    Get-ScheduledTask | `
 ? {$_.State -eq "Ready"} | `
 Get-ScheduledTaskInfo | `
 ? {$_.NextRunTime -ne $null}| `
 Format-List `
} | Out-File $log_path -Encoding UTF8

Remove-PSSession $session

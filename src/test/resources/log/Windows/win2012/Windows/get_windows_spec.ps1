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

$log_path = Join-Path $log_dir "cpu"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_Processor `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "memory"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_OperatingSystem | `
    select TotalVirtualMemorySize,TotalVisibleMemorySize, `
        FreePhysicalMemory,FreeVirtualMemory,FreeSpaceInPagingFiles `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "system"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject -Class Win32_ComputerSystem `
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
$log_path = Join-Path $log_dir "filesystem"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_LogicalDisk `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "user"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_UserAccount | FL `
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
$log_path = Join-Path $log_dir "fips"
Invoke-Command -Session $session -ScriptBlock { `
    Get-Item "HKLM:System\CurrentControlSet\Control\Lsa\FIPSAlgorithmPolicy" `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "network"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_NetworkAdapterConfiguration | `
 Where{$_.IpEnabled -Match "True"} | `
 Select ServiceName, MacAddress, IPAddress, DefaultIPGateway, Description, IPSubnet | `
 Format-List ` `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "nic_teaming"
Invoke-Command -Session $session -ScriptBlock { `
    Get-NetLbfoTeamNic `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "remote_desktop"
Invoke-Command -Session $session -ScriptBlock { `
    (Get-Item "HKLM:System\CurrentControlSet\Control\Terminal Server").GetValue("fDenyTSConnections") `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "firewall"
Invoke-Command -Session $session -ScriptBlock { `
    Get-NetFirewallRule -Direction Inbound -Enabled True `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "dns"
Invoke-Command -Session $session -ScriptBlock { `
    Get-DnsClientServerAddress|FL `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "etc_hosts"
Invoke-Command -Session $session -ScriptBlock { `
    Get-Content "$($env:windir)\system32\Drivers\etc\hosts" `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "net_accounts"
Invoke-Command -Session $session -ScriptBlock { `
    net accounts `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "storage_timeout"
Invoke-Command -Session $session -ScriptBlock { `
    Get-ItemProperty "HKLM:SYSTEM\CurrentControlSet\Services\disk" `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "task_scheduler"
Invoke-Command -Session $session -ScriptBlock { `
    Get-ScheduledTask | `
 ? {$_.State -eq "Ready"} | `
 Get-ScheduledTaskInfo | `
 ? {$_.NextRunTime -ne $null}| `
 Format-List `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "patch_lists"
Invoke-Command -Session $session -ScriptBlock { `
    wmic qfe `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "ntp"
Invoke-Command -Session $session -ScriptBlock { `
    (Get-Item "HKLM:System\CurrentControlSet\Services\W32Time\Parameters").GetValue("NtpServer") `
} | Out-File $log_path -Encoding UTF8

Remove-PSSession $session

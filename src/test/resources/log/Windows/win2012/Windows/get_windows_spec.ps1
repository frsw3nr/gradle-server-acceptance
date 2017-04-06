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
$log_path = Join-Path $log_dir "driver"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_PnPSignedDriver `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "filesystem"
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_LogicalDisk `
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
$log_path = Join-Path $log_dir "firewall"
Invoke-Command -Session $session -ScriptBlock { `
    Get-NetFirewallRule -Direction Inbound -Enabled True `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "dns"
Invoke-Command -Session $session -ScriptBlock { `
    Get-DnsClientServerAddress|FL `
} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "storage_timeout"
Invoke-Command -Session $session -ScriptBlock { `
    Get-ItemProperty "HKLM:SYSTEM\CurrentControlSet\Services\disk" `
} | Out-File $log_path -Encoding UTF8

Remove-PSSession $session

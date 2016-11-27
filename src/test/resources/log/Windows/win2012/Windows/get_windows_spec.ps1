Param(
    [string]$log_dir
  , [string]$ip
  , [string]$server
  , [string]$user
  , [string]$password
)
$secure   = ConvertTo-SecureString $password -asplaintext -force
$cred     = New-Object System.Management.Automation.PsCredential $user, $secure

$ErrorActionPreference = "Stop"
$session = $null
try {
    $script:session  = New-PSSession $server -Credential $cred
} catch [Exception] {
    Write-Error "$error"
    exit 1
}
$ErrorActionPreference = "Continue"

Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_Processor `
} | Out-File "$log_dir/cpu" -Encoding UTF8
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_OperatingSystem | `
    select TotalVirtualMemorySize,TotalVisibleMemorySize, `
        FreePhysicalMemory,FreeVirtualMemory,FreeSpaceInPagingFiles `
} | Out-File "$log_dir/memory" -Encoding UTF8
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject -Class Win32_ComputerSystem `
} | Out-File "$log_dir/system" -Encoding UTF8
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_OperatingSystem | `
    Format-List Caption,CSDVersion,ProductType,OSArchitecture `
} | Out-File "$log_dir/os" -Encoding UTF8
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_PnPSignedDriver `
} | Out-File "$log_dir/driver" -Encoding UTF8
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_LogicalDisk `
} | Out-File "$log_dir/filesystem" -Encoding UTF8
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_UserAccount | FL `
} | Out-File "$log_dir/user" -Encoding UTF8
Invoke-Command -Session $session -ScriptBlock { `
    Get-Service | FL `
} | Out-File "$log_dir/service" -Encoding UTF8
Invoke-Command -Session $session -ScriptBlock { `
    Get-Item "HKLM:System\CurrentControlSet\Control\Lsa\FIPSAlgorithmPolicy" `
} | Out-File "$log_dir/fips" -Encoding UTF8
Invoke-Command -Session $session -ScriptBlock { `
    Get-WmiObject Win32_NetworkAdapterConfiguration | `
 Where{$_.IpEnabled -Match "True"} | `
 Select MacAddress, IPAddress, DefaultIPGateway, Description, IPSubnet | `
 Format-List ` `
} | Out-File "$log_dir/network" -Encoding UTF8
Invoke-Command -Session $session -ScriptBlock { `
    Get-NetFirewallRule -Direction Inbound -Enabled True `
} | Out-File "$log_dir/firewall" -Encoding UTF8
Invoke-Command -Session $session -ScriptBlock { `
    Get-DnsClientServerAddress|FL `
} | Out-File "$log_dir/dns" -Encoding UTF8
Invoke-Command -Session $session -ScriptBlock { `
    Get-ItemProperty "HKLM:SYSTEM\CurrentControlSet\Services\disk" `
} | Out-File "$log_dir/storage_timeout" -Encoding UTF8
Invoke-Command -Session $session -ScriptBlock { `
    w32tm /query /status `
} | Out-File "$log_dir/ntp" -Encoding UTF8

Remove-PSSession $session

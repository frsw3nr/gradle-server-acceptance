Param(
    [string]$log_dir
  , [string]$ip
  , [string]$server
  , [string]$user
  , [string]$password
)

$secure   = ConvertTo-SecureString $password -asplaintext -force
$cred     = New-Object System.Management.Automation.PsCredential $user, $secure


            Get-WmiObject -Credential $cred -ComputerName $ip Win32_Processor | Out-File "$log_dir/cpu" -Encoding UTF8
Get-WmiObject -Credential $cred -ComputerName $ip Win32_OperatingSystem | `
    select TotalVirtualMemorySize,TotalVisibleMemorySize, `
        FreePhysicalMemory,FreeVirtualMemory,FreeSpaceInPagingFiles | Out-File "$log_dir/memory" -Encoding UTF8
Get-WmiObject -Credential $cred -ComputerName $ip Win32_PnPSignedDriver | Out-File "$log_dir/driver" -Encoding UTF8
Get-WmiObject -Credential $cred -ComputerName $ip Win32_LogicalDisk | Out-File "$log_dir/filesystem" -Encoding UTF8
$reg = Get-WmiObject -List -Namespace root\default -Credential $cred -ComputerName $ip | `
Where-Object {$_.Name -eq "StdRegProv"}
$HKLM = 2147483650
$reg.GetStringValue($HKLM,"System\CurrentControlSet\Control\Lsa\FIPSAlgorithmPolicy","Enabled").sValue | Out-File "$log_dir/fips" -Encoding UTF8
Get-WmiObject -Credential $cred -ComputerName $ip Win32_NetworkAdapterConfiguration | Out-File "$log_dir/network" -Encoding UTF8
$hklm  = 2147483650
$key   = "SYSTEM\CurrentControlSet\Services\disk"
$value = "TimeoutValue"
$reg = get-wmiobject -list "StdRegProv" -namespace root\default -computername $ip -credential $cred | where-object { $_.Name -eq "StdRegProv" }
$reg.GetStringValue($hklm, $key, $value) | Select-Object uValue | Out-File "$log_dir/storage_timeout" -Encoding UTF8

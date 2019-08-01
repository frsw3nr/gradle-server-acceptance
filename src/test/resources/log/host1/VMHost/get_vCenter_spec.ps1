Param(
    [string]$log_dir
  , [string]$vm
  , [string]$server
  , [string]$vcenter
  , [string]$user
  , [string]$password
)
$ErrorActionPreference = "Stop"
try {
    Add-PSSnapin VMware.VimAutomation.Core
    Connect-VIServer -User $user -Password $password -Server $vcenter
} catch [Exception] {
    Write-Error "$error"
    exit 1
}
$ErrorActionPreference = "Continue"
Get-VMHost $vm | Format-List | Out-File "$log_dir/VMHost" -Encoding UTF8
Get-VMHostAccount | Format-Table -Auto | Out-File "$log_dir/Account" -Encoding UTF8
Get-VMHostNetworkAdapter -VMHost $vm | Format-Table -Auto | Out-File "$log_dir/NetworkAdapter" -Encoding UTF8
Get-VMHostDisk -VMHost $vm | Format-List | Out-File "$log_dir/Disk" -Encoding UTF8
Get-Datastore -VMHost $vm | Format-Table -Auto | Out-File "$log_dir/Datastore" -Encoding UTF8

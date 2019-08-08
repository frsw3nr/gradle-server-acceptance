Param(
    [string]$log_dir
  , [string]$vm
  , [string]$server
  , [string]$vcenter
  , [string]$user
  , [string]$password
)
$log_dir = Convert-Path $log_dir
$ErrorActionPreference = "Stop"
echo "TESTTESTTEST"
try {
  if ( !(Get-PSSnapin -Name VMware.VimAutomation.Core -ErrorAction SilentlyContinue ) ) {
    if ( !(Get-Module -Name VMware.VimAutomation.Core -ErrorAction SilentlyContinue) ) {
      if (Test-Path "C:\Program Files (x86)\VMware\Infrastructure\PowerCLI\Scripts\Initialize-PowerCLIEnvironment.ps1") {
        . "C:\Program Files (x86)\VMware\Infrastructure\PowerCLI\Scripts\Initialize-PowerCLIEnvironment.ps1"
      } elseif (Test-Path "C:\Program Files (x86)\VMware\Infrastructure\vSphere PowerCLI\Scripts\Initialize-PowerCLIEnvironment.ps1") {
        . "C:\Program Files (x86)\VMware\Infrastructure\vSphere PowerCLI\Scripts\Initialize-PowerCLIEnvironment.ps1"
      } else {
        Write-Host Initialize-PowerCLIEnvironment.ps1 script is not found!
      }
    }
  } else {
    Add-PSSnapin VMware.VimAutomation.Core
  }
  Connect-VIServer -User $user -Password $password -Server $vcenter -Force
} catch [Exception] {
  Write-Error "$error"
  exit 1
}
$ErrorActionPreference = "Continue"

$log_path = Join-Path $log_dir "vm"
Get-VM $vm | `
 select NumCpu, PowerState, MemoryGB, VMHost, @{N="Cluster";E={Get-Cluster -VM $_}} | `
 Format-List | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "vm_conf"
Get-VMResourceConfiguration -VM $vm | `
FL | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "vmext"
(Get-VM $vm | select ExtensionData).ExtensionData.config | `
 Select Name,CpuHotAddEnabled,MemoryReservationLockedToMax,MemoryHotAddEnabled | `
 Format-List | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "datastore"
Get-Datastore -VM cent7 | FL | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "vm_storage"
 Get-Harddisk -VM $vm | `
 select Parent, Filename,CapacityGB, StorageFormat, DiskType | `
 Format-List | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "vmnet"
Get-NetworkAdapter -VM cent7 | FL | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "vmwaretool"
Get-VM $vm | `
 Get-AdvancedSetting vmware.tools.internalversion,vmware.tools.requiredversion | `
 Select Name, Value | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "vm_timesync"
Get-VM $vm |
Select @{N='TimeSync';E={$_.ExtensionData.Config.Tools.syncTimeWithHost}} |
Format-List | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "vm_iops_limit"
Get-VMResourceConfiguration -VM $vm | `
format-custom -property DiskResourceConfiguration | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "vm_nic_limit"
Get-VM $vm | Get-NetworkAdapter |
sort Parent,{$_.ExtensionData.ResourceAllocation.Limit} |
select Parent,Name,{$_.ExtensionData.ResourceAllocation.Limit} | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "vm_floppy"
Get-Vm $vm | Get-FloppyDrive | `
 Select Parent, Name, ConnectionState | `
 Format-List | Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "vm_video_ram"
Get-VM $vm | `
 Select @{N="VideoRamSizeInKB";E={($_.ExtensionData.Config.Hardware.Device | `
 where {$_.key -eq 500}).VideoRamSizeInKB}} | Sort-Object Name | `
 Format-List | Out-File $log_path -Encoding UTF8

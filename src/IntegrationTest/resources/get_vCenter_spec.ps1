Param(
    [string]$log_dir
  , [string]$vm
  , [string]$server
  , [string]$vcenter
  , [string]$user
  , [string]$password
)
Add-PSSnapin VMware.VimAutomation.Core
Connect-VIServer -User $user -Password $password -Server $vcenter

get-vm $vm | `
 select NumCpu, PowerState, MemoryGB, VMHost, @{N="Cluster";E={Get-Cluster -VM $_}} | `
 Format-List | Out-File "$log_dir/vm" -Encoding UTF8
            Get-VM $vm `
 Get-AdvancedSetting vmware.tools.internalversion,vmware.tools.requiredversion `
 Select Name, Value | Out-File "$log_dir/vmwaretool" -Encoding UTF8
Get-VM $vm | Select @{N='TimeSync';E={$_.ExtensionData.Config.Tools.syncTimeWithHost}} | Out-File "$log_dir/vm_timesync" -Encoding UTF8

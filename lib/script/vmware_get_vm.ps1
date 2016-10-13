Param(
    [string]$vm
  , [string]$server
  , [string]$vcenter
  , [string]$user
  , [string]$password
)
Add-PSSnapin VMware.VimAutomation.Core
$log_file = ".\build\log\vcenter\" + $server + "\vm"
Connect-VIServer -User $user -Password $password -Server $vcenter
get-vm $vm | select NumCpu, PowerState, MemoryGB, VMHost | Out-File $log_file -Encoding UTF8



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

$log_path = Join-Path $log_dir "vmnet"
Get-NetworkAdapter -VM ostrich | FL | Out-File $log_path -Encoding UTF8

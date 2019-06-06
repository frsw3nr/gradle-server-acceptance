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

Remove-PSSession $session

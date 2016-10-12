Param(
    [string]$ip
  , [string]$server
  , [string]$user
  , [string]$password
)

$secure   = ConvertTo-SecureString $password -asplaintext -force
$cred     = New-Object System.Management.Automation.PsCredential $user, $secure
$log_dir  = ".\build\log\windows\" + $server

$log_file    = $log_dir + "\network"
Get-WmiObject -Credential $cred -ComputerName $ip Win32_NetworkAdapterConfiguration | Out-File $log_file -Encoding UTF8
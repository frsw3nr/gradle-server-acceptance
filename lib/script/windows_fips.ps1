Param(
    [string]$ip
  , [string]$server
  , [string]$user
  , [string]$password
)

$secure   = ConvertTo-SecureString $password -asplaintext -force
$cred     = New-Object System.Management.Automation.PsCredential $user, $secure
$log_dir  = ".\build\log\windows\" + $server

$reg = Get-WmiObject -List -Namespace root\default -ComputerName $ip -Credential $cred | Where-Object {$_.Name -eq "StdRegProv"}

$log_file    = $log_dir + "\fips"
$HKLM = 2147483650
$reg.GetStringValue($HKLM,"System\CurrentControlSet\Control\Lsa\FIPSAlgorithmPolicy","Enabled").sValue  | Out-File $log_file -Encoding UTF8



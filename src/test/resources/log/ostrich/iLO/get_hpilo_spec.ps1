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

$log_path = Join-Path $log_dir "FindHPiLO"
    
Find-HPiLO "$ip" -Full `
 | FL `
| Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "License"
    
Get-HPiLOLicense -Server "$ip" -Credential $cred -DisableCert `
 | FL `
| Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "FwVersion"
    
$xml = @"
<RIBCL VERSION="2.0">
   <LOGIN USER_LOGIN="adminname" PASSWORD="password">
      <RIB_INFO MODE="read">
         <GET_FW_VERSION/>
      </RIB_INFO>
   </LOGIN>
</RIBCL>
"@
Invoke-HPiLORIBCLCommand -Server "$ip" -Credential $cred `
    -RIBCLCommand $xml `
    -DisableCertificateAuthentication -OutputType "ribcl" `
| Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "FwInfo"
    
Get-HPiLOFirmwareInfo -Server "$ip" -Credential $cred -DisableCertificateAuthentication `
 | Select -ExpandProperty "FirmwareInfo" `
 | Select "FIRMWARE_NAME", "FIRMWARE_VERSION" `
 | FL `
| Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "BootMode"
    
Get-HPiLOCurrentBootMode -Server "$ip" -Credential $cred -DisableCertificateAuthentication `
 | FL `
| Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "Processor"
    
Get-HPiLOProcessor -Server "$ip" -Credential $cred -DisableCert `
 | Select -ExpandProperty "PROCESSOR" `
 | Select "LABEL","NAME","SPEED","STATUS","EXECUTION_TECHNOLOGY" | FL `
| Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "Nic"
    
$xml = @"
<RIBCL VERSION="2.0">
   <LOGIN USER_LOGIN="adminname" PASSWORD="password">
      <SERVER_INFO MODE="read">
         <GET_EMBEDDED_HEALTH />
      </SERVER_INFO>
   </LOGIN>
</RIBCL>
"@
Invoke-HPiLORIBCLCommand -Server "$ip" -Credential $cred `
    -RIBCLCommand $xml `
    -DisableCertificateAuthentication -OutputType "ribcl" `
| Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "Storage"
    
$xml = @"
<RIBCL VERSION="2.0">
   <LOGIN USER_LOGIN="adminname" PASSWORD="password">
      <SERVER_INFO MODE="read">
         <GET_EMBEDDED_HEALTH />
      </SERVER_INFO>
   </LOGIN>
</RIBCL>
"@
Invoke-HPiLORIBCLCommand -Server "$ip" -Credential $cred `
    -RIBCLCommand $xml `
    -DisableCertificateAuthentication -OutputType "ribcl" `
| Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "SNMP"
    
Get-HPiLOSNMPIMSetting -Server "$ip" -Credential $cred -DisableCert `
 | FL `
| Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "HostPowerSaver"
    
Get-HPiLOHostPowerSaver -Server "$ip" -Credential $cred -DisableCert `
 | FL `
| Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "PowerReading"
    
Get-HPiLOPowerReading -Server "$ip" -Credential $cred -DisableCert `
 | FL `
| Out-File $log_path -Encoding UTF8
$log_path = Join-Path $log_dir "PowerSupply"
    
Get-HPiLOPowerSupply -Server "$ip" -Credential $cred -DisableCert `
 | Select -ExpandProperty "POWER_SUPPLY_SUMMARY" `
 | Select "HIGH_EFFICIENCY_MODE","POWER_SYSTEM_REDUNDANCY","PRESENT_POWER_READING" `
 | FL `
| Out-File $log_path -Encoding UTF8


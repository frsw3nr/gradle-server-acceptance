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

# テスト用
# $log_path = Join-Path $log_dir "hitachi_vsp_raidinf_report.tgz"
# Invoke-Command -Session $session -ScriptBlock { `
#     [Byte[]]$bFile = Get-Content "c:\Users\verification\appdata\Local\Temp\vsp_raidinf_repos_csv.tgz" -Encoding Byte
#     $bFile `
# } | Set-Content $log_path -Encoding Byte

# テスト用2
# $log_path = Join-Path $log_dir "hitachi_vsp_raidinf_report.tgz"
# Invoke-Command -Session $session -ScriptBlock { `
#     [Byte[]]$bFile = Get-Content "c:\work\vsp_raidinf_repos.tgz" -Encoding Byte
#     $bFile `
# } | Set-Content $log_path -Encoding Byte

# メインモジュール
$log_path = Join-Path $log_dir "hitachi_vsp_raidinf_report.tgz"
Invoke-Command -Session $session -ScriptBlock { `

$script_path = Join-Path $env:temp "report_raidinf.bat"
$script_content = @'
set SVP_HOST=%1
set SVP_USER=%2
set SVP_PASS=%3
set SVP_WAIT=30
set REPORT_DATE=%date:~2,2%%date:~5,2%%date:~8,2%
set REPORT_NAME=%REPORT_DATE%-CreateConfigurationReport

cd "C:\Program Files (x86)\raidinf"

.\raidinf.exe -login %SVP_USER% %SVP_PASS% -servername %SVP_HOST%
.\raidinf.exe add report -servername %SVP_HOST%
ping 127.0.0.1 -n %SVP_WAIT% > nul
.\raidinf.exe download report -servername %SVP_HOST% -report %REPORT_NAME% -targetfolder %TEMP%
ping 127.0.0.1 -n %SVP_WAIT% > nul
.\raidinf.exe delete report -servername %SVP_HOST% -report %REPORT_NAME%
.\raidinf.exe -logout -servername %SVP_HOST%

cd %TEMP%
del vsp_raidinf_repos.tgz
rename Report_%REPORT_NAME%.tgz vsp_raidinf_repos.tgz
'@

$script_content | Set-Content $script_path

$script_log = Join-Path $env:temp "report_raidinf.txt"
&$script_path localhost hccuser password | Out-File -Force $script_log

$result = Join-Path $env:temp 'vsp_raidinf_repos_csv.tgz'
[Byte[]]$bFile = Get-Content $result -Encoding Byte
$bFile `
} | Set-Content $log_path -Encoding Byte

Remove-PSSession $session

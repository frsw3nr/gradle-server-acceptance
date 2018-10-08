param (
        [parameter(mandatory=$true)][string]$vsp_host
    )

echo "vsp_host : $vsp_host"

$scriptBase = Split-Path -Parent $MyInvocation.MyCommand.Path
$reportDir = Join-Path $scriptBase "..\..\src\test\resources\log\HitachiVSP\$vsp_host\HitachiVSP"
New-Item $reportDir -ItemType Directory -Force | Out-Null
$reportDir = Resolve-Path $reportDir

echo @"
$vsp_host 用VSP構成レポート検査用ディレクトリを作成しました。
SVPコンソールから以下のフォルダに構成レポートをダウンロードしてください。
ダウンロード手順は、プロジェクト直下にある「HitachiVSP構成レポート作成手順.xlsx」を参照してください

ダウンロード先ディレクトリ：$reportDir
ダウンロードファイル名： hitachi_vsp_raidinf_report.tgz
"@


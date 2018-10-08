param (
        [parameter(mandatory=$true)][string]$vsp_host
    )

echo "vsp_host : $vsp_host"

$scriptBase = Split-Path -Parent $MyInvocation.MyCommand.Path
$reportDir = Join-Path $scriptBase "..\..\src\test\resources\log\HitachiVSP\$vsp_host\HitachiVSP"
New-Item $reportDir -ItemType Directory -Force | Out-Null
$reportDir = Resolve-Path $reportDir

echo @"
$vsp_host �pVSP�\�����|�[�g�����p�f�B���N�g�����쐬���܂����B
SVP�R���\�[������ȉ��̃t�H���_�ɍ\�����|�[�g���_�E�����[�h���Ă��������B
�_�E�����[�h�菇�́A�v���W�F�N�g�����ɂ���uHitachiVSP�\�����|�[�g�쐬�菇.xlsx�v���Q�Ƃ��Ă�������

�_�E�����[�h��f�B���N�g���F$reportDir
�_�E�����[�h�t�@�C�����F hitachi_vsp_raidinf_report.tgz
"@


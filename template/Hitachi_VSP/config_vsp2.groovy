// �����d�l�V�[�g��`

evidence.source = './template/Hitachi_VSP/HitachiVSP2�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�

//evidence.target='./build/HitachiVSP2�`�F�b�N�V�[�g_<date>.xlsx'
evidence.target='./build/vsp2_check_sheet.xlsx'

// �������ʃ��O�f�B���N�g��

evidence.staging_dir='./build/log'

// �����A���،��ʃf�B���N�g��

evidence.json_dir='./src/test/resources/json'

// ���񉻂��Ȃ��^�X�N
// ����x���w������Ă��A�w�肵���h���C���^�X�N�̓V���A���Ɏ��s����

test.serialization.tasks = []

// DryRun���[�h���O�ۑ���

test.dry_run_staging_dir = './src/test/resources/log/'

// �R�}���h�̎�̃^�C���A�E�g
// Windows,vCenter�̏ꍇ�A�S�R�}���h���܂Ƃ߂��o�b�`�X�N���v�g�̃^�C���A�E�g�l

test.HitachiVSP2.timeout = 300

// �R�}���h�̎�̃f�o�b�O���[�h

 test.HitachiVSP2.debug  = false

// DryRun �\�s���K���[�h

test.HitachiVSP2.dry_run   = false

// HitachiVSP2 �ڑ����

account.HitachiVSP2.Test.report_dir = './src/hitachi_vsp_report'
account.HitachiVSP2.Test.user     = 'administrator'
account.HitachiVSP2.Test.password = 'P@ssw0rd'

// �����d�l�V�[�g��`

evidence.source = './template/FJ_Eternus/ETERNUS�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�

//evidence.target='./build/ETERNUS�`�F�b�N�V�[�g_<date>.xlsx'
 evidence.target='./build/eternus_check_sheet.xlsx'

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

test.Eternus.timeout = 300

// �R�}���h�̎�̃f�o�b�O���[�h

 test.Eternus.debug  = false

// DryRun �\�s���K���[�h

test.Eternus.dry_run   = false

// Eternus �ڑ����

account.Eternus.Test.user      = 'root'
account.Eternus.Test.password  = 'root'
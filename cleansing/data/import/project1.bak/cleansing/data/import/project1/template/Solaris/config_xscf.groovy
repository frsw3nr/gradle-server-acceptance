// �����d�l�V�[�g��`

evidence.source = './template/Solaris/XSCF�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�

evidence.target='./build/XSCF�`�F�b�N�V�[�g_<date>.xlsx'
// evidence.target='./build/xscf_check_sheet.xlsx'

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

test.XSCF.timeout = 30

// �R�}���h�̎�̃f�o�b�O���[�h

test.XSCF.debug  = false

// DryRun �\�s���K���[�h

test.XSCF.dry_run   = false

// XSCF �ڑ����

account.XSCF.Test.user      = 'console'
account.XSCF.Test.password  = 'console0'
account.XSCF.Test.work_dir  = '/tmp/gradle_test'

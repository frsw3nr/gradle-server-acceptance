// �����d�l�V�[�g��`

evidence.source = './template/Router/RTX�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�

// evidence.target='./build/RTX�`�F�b�N�V�[�g_<date>.xlsx'
evidence.target='./build/rtx_check_sheet.xlsx'

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

test.RouterRTX.timeout = 30

// �R�}���h�̎�̃f�o�b�O���[�h

test.RouterRTX.debug  = false

// DryRun �\�s���K���[�h

test.RouterRTX.dry_run   = false

// RouterRTX �ڑ����

account.RouterRTX.Test.password  = 'console0'
account.RouterRTX.Test.work_dir  = '/tmp/gradle_test'


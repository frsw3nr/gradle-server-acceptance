// �����d�l�V�[�g��`

evidence.source = './template/AIX/AIX�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�

// evidence.target='./build/AIX�`�F�b�N�V�[�g_<date>.xlsx'
 evidence.target='./build/aix_check_sheet.xlsx'

// �������ʃ��O�f�B���N�g��
evidence.staging_dir='./build/log'

// �����A���،��ʃf�B���N�g��
evidence.json_dir='./src/test/resources/json'

// ���񉻂��Ȃ��^�X�N
// ����x���w������Ă��A�w�肵���h���C���^�X�N�̓V���A���Ɏ��s����

test.serialization.tasks = ['']

// DryRun���[�h���O�ۑ���
test.dry_run_staging_dir = './src/test/resources/log/'

// �R�}���h�̎�̃^�C���A�E�g
// Windows,vCenter�̏ꍇ�A�S�R�}���h���܂Ƃ߂��o�b�`�X�N���v�g�̃^�C���A�E�g�l

test.AIX.timeout = 300

// �R�}���h�̎�̃f�o�b�O���[�h

 test.AIX.debug  = false

// DryRun �\�s���K���[�h

test.AIX.dry_run   = false

// AIX �ڑ����

account.AIX.Test.user      = 'guest'
account.AIX.Test.password  = 'guest0001'
account.AIX.Test.work_dir  = '/tmp/gradle_test'

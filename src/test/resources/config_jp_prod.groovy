// �����d�l�V�[�g��`

evidence.source = './�T�[�o�[�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�
//evidence.target='./build/check_sheet_<date>.xlsx'
evidence.target='./build/check_sheet.xlsx'

// �������ʃ��O�f�B���N�g��
evidence.staging_dir='./build/log'

// �����A���،��ʃf�B���N�g��
evidence.json_dir='./build/json'

// ���񉻂��Ȃ��^�X�N
test.serialization.tasks = ['vCenter']

// DryRun���[�h���O�ۑ���
test.dry_run_staging_dir = './src/test/resources/log/'

// �R�}���h�̎�̃^�C���A�E�g
// Windows,vCenter�̏ꍇ�A�S�R�}���h���܂Ƃ߂��o�b�`�X�N���v�g�̃^�C���A�E�g�l
test.Linux.timeout   = 30
test.Windows.timeout = 300
test.vCenter.timeout = 300

// �R�}���h�̎�̃f�o�b�O���[�h
test.Linux.debug   = false
test.Windows.debug = false
test.vCenter.debug = false

// vCenter�ڑ����
account.vCenter.Test.server   = '192.168.10.100'
account.vCenter.Test.user     = 'test_user'
account.vCenter.Test.password = 'P@ssword'

// Linux �ڑ����
account.Linux.Test.user      = 'someuser'
account.Linux.Test.password  = 'P@ssword'
account.Linux.Test.work_dir  = '/tmp/gradle_test'

// Windows �ڑ����
account.Windows.Test.user     = 'administrator'
account.Windows.Test.password = 'P@ssword'

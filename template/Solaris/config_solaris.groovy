// �����d�l�V�[�g��`

evidence.source = './template/Solaris/Solaris�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�

evidence.target='./build/Solaris�`�F�b�N�V�[�g_<date>.xlsx'
// evidence.target='./build/solaris_check_sheet.xlsx'

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

test.Solaris.timeout = 60

// �R�}���h�̎�̃f�o�b�O���[�h

 test.Solaris.debug  = false

// DryRun �\�s���K���[�h

test.Solaris.dry_run   = false

// Solaris �ڑ����

account.Solaris.Test.user      = 'admin'
account.Solaris.Test.password  = 'Y5root'
account.Solaris.Test.work_dir  = '/tmp/gradle_test'

account.Solaris.Test_telnet.use_telnet = false
account.Solaris.Test_telnet.user       = 'someuser'
account.Solaris.Test_telnet.password   = 'P@ssw0rd'
account.Solaris.Test_telnet.work_dir   = '/tmp/gradle_test'

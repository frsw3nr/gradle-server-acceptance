// �����d�l�V�[�g��`

evidence.source = './�T�[�o�[�`�F�b�N�V�[�g.xlsx'
evidence.sheet_name_server = '�`�F�b�N�Ώ�VM'
evidence.sheet_name_rule = '�������[��'
evidence.sheet_name_spec = [
    'Linux':   '�Q�X�gOS���o���`�F�b�N�V�[�g(Linux)',
    'Windows': '�Q�X�gOS���o���`�F�b�N�V�[�g(Windows)',
]

// �������ʃt�@�C���o�͐�

evidence.target='./build/�`�F�b�N�V�[�g_<date>.xlsx'

// �������ʃ��O�f�B���N�g��

evidence.staging_dir='./build/log.<date>'

// �������[�h

test.dry_run_staging_dir = './src/test/resources/log/'

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

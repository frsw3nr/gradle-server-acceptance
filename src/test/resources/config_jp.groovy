// �����d�l�V�[�g��`

evidence.source = './src/main/resources/root/jp/�T�[�o�[�`�F�b�N�V�[�g.xlsx'
evidence.sheet_name_server = '�`�F�b�N�Ώ�'
evidence.sheet_name_rule = '�������[��'
evidence.sheet_name_spec = [
    'Linux':   '�Q�X�gOS�`�F�b�N�V�[�g(Linux)',
    'Windows': '�Q�X�gOS�`�F�b�N�V�[�g(Windows)',
    'VMHost':  '�Q�X�gOS�`�F�b�N�V�[�g(VMHost)',
]

// �������ʃt�@�C���o�͐�

evidence.target='./build/�`�F�b�N�V�[�g_<date>.xlsx'

// �������ʃ��O�f�B���N�g��

evidence.staging_dir='./build/log'

// CSV�ϊ��}�b�v

evidence.csv_item_map = [
    '�T�[�o��' :            'server_name',
    'IP�A�h���X' :          'ip',
    'Platform' :            'platform',
    'OS�A�J�E���gID' :      'os_account_id',
    'vCenter�A�J�E���gID' : 'remote_account_id',
    'VM�G�C���A�X��' :      'remote_alias',
    '����ID' :              'verify_id',
    'CPU��' :               'NumCpu',
    '��������' :            'MemoryGB',
    'ESXi��' :              'ESXiHost',
    'HDD' :                 'HDDtype',
]

// ���񉻂��Ȃ��^�X�N
// ����x���w������Ă��A�w�肵���h���C���^�X�N�̓V���A���Ɏ��s����

test.serialization.tasks = ['vCenter', 'VMHost']

// DryRun���[�h���O�ۑ���

test.dry_run_staging_dir = './src/test/resources/log/'

// �R�}���h�̎�̃^�C���A�E�g
// Windows,vCenter�̏ꍇ�A�S�R�}���h���܂Ƃ߂��o�b�`�X�N���v�g�̃^�C���A�E�g�l

test.Linux.timeout   = 30
test.Windows.timeout = 300
test.vCenter.timeout = 300
test.VMHost.timeout  = 300

// vCenter�ڑ����

account.Remote.Test.server   = '192.168.10.100'
account.Remote.Test.user     = 'test_user'
account.Remote.Test.password = 'P@ssword'

// Linux �ڑ����

account.Linux.Test.user      = 'someuser'
account.Linux.Test.password  = 'P@ssword'
account.Linux.Test.work_dir  = '/tmp/gradle_test'

// Windows �ڑ����

account.Windows.Test.user     = 'administrator'
account.Windows.Test.password = 'P@ssword'

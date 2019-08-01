// �����d�l�V�[�g��`

evidence.source = './�T�[�o�[�`�F�b�N�V�[�g.xlsx'
// evidence.sheet_name_server = 'Target'
// evidence.sheet_name_rule = 'Rule'
// evidence.sheet_name_spec = [
//     'Linux':   'CheckSheet(Linux)',
//     'Windows': 'CheckSheet(Windows)',
// ]

// �������ʃt�@�C���o�͐�

evidence.target='./build/check_sheet_<date>.xlsx'
//evidence.target='./build/check_sheet.xlsx'

// �������ʃ��O�f�B���N�g��
evidence.staging_dir='./build/log'

// �����A���،��ʃf�B���N�g��
evidence.json_dir='./src/test/resources/json'

// CSV�ϊ��}�b�v

// evidence.csv_item_map = [
//     '�T�[�o��' :            'server_name',
//     'IP�A�h���X' :          'ip',
//     'Platform' :            'platform',
//     'OS�A�J�E���gID' :      'os_account_id',
//     'vCenter�A�J�E���gID' : 'remote_account_id',
//     'VM�G�C���A�X��' :      'remote_alias',
//     '����ID' :              'verify_id',
//     'CPU��' :               'NumCpu',
//     '��������' :            'MemoryGB',
//     'ESXi��' :              'ESXiHost',
//     'HDD' :                 'HDDtype',
// ]

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

// account.Remote.Test.server   = '192.168.10.100'
// account.Remote.Test.user     = 'test_user'
// account.Remote.Test.password = 'P@ssword'

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

// VMHost �ڑ����

// account.VMHost.Test.user     = 'root'
// account.VMHost.Test.password = 'P@ssword'

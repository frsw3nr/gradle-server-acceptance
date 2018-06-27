// �����d�l�V�[�g��`

//evidence.source = './�T�[�o�`�F�b�N�V�[�g.xlsx'
evidence.source = './build/�T�[�o�`�F�b�N�V�[�g.xls'

// �������ʃt�@�C���o�͐�

//evidence.target='./build/�T�[�o�`�F�b�N�V�[�g_<date>.xlsx'
evidence.target='./build/check_sheet.xlsx'

// �������ʃ��O�f�B���N�g��
evidence.staging_dir='./build/log'

// �����A���،��ʃf�B���N�g��
evidence.json_dir='./src/test/resources/json'

// ���|�[�g�ϊ��}�b�v
report.item_map.target = [
    'server'        : 'name',
    'domain'        : 'domain',
    'ip'            : 'ip',
    'successrate'   : 'success_rate',
    'verifycomment' : 'verify_comment',
]

report.item_map.platform.Linux = [
    'os'   : 'os',
    'os2'  : 'arch',
    'cpu'  : 'cpu_total',
    'mem'  : 'mem_total',
    'disk' : 'filesystem',
    'net'  : 'net_ip',
]

report.item_map.platform.Windows = [
    'os'   : 'os_caption',
    'os2'  : 'os_architecture',
    'cpu'  : 'cpu_total',
    'mem'  : 'visible_memory',
    'disk' : 'filesystem',
    'net'  : 'network',
]

report.item_map.platform.iLO = [
    'net_mng' : 'Nic',
]

report.item_map.platform.PRIMERGY = [
    'net_mng' : 'nic',
]

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

test.Linux.dry_run   = false
test.Windows.dry_run = false
test.vCenter.dry_run = false

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


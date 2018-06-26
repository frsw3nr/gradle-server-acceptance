// �����d�l�V�[�g��`

evidence.source = './Solaris�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�

// evidence.target='./build/Solaris�`�F�b�N�V�[�g.xlsx'
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
// ����x���w������Ă��A�w�肵���h���C���^�X�N�̓V���A���Ɏ��s����

// test.serialization.tasks = ['vCenter']

// DryRun���[�h���O�ۑ���

test.dry_run_staging_dir = './src/test/resources/log/'

// �R�}���h�̎�̃^�C���A�E�g
// Windows,vCenter�̏ꍇ�A�S�R�}���h���܂Ƃ߂��o�b�`�X�N���v�g�̃^�C���A�E�g�l

test.Solaris.timeout = 300

// �R�}���h�̎�̃f�o�b�O���[�h

// test.Solaris.debug  = false

// Solaris �ڑ����

// account.Solaris.Test.user      = 'root'
// account.Solaris.Test.password  = 'root0000'
account.Solaris.Test.user      = 'guest'
account.Solaris.Test.password  = 'guest000'
account.Solaris.Test.work_dir  = '/tmp/gradle_test'
//account.Solaris.Test.logon_test = [['user':'someuser', 'password':'P@ssword'],
//                                   ['user':'root'  , 'password':'P@ssw0rd']]

 

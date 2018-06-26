// �����d�l�V�[�g��`

evidence.source = './PRIMERGY�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�

// evidence.target='./build/PRIMERGY�`�F�b�N�V�[�g_<date>.xlsx'
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

// ���񉻂��Ȃ��^�X�N
// ����x���w������Ă��A�w�肵���h���C���^�X�N�̓V���A���Ɏ��s����

test.serialization.tasks = []

// DryRun���[�h���O�ۑ���

test.dry_run_staging_dir = './src/test/resources/log/'

// �R�}���h�̎�̃^�C���A�E�g
test.Primergy.timeout = 300

// Zabbix�ڑ����

account.Primergy.Test.user     = 'admin'
account.Primergy.Test.password = 'admin'


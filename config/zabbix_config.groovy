// �����d�l�V�[�g��`

evidence.source = './Zabbix�Ď��ݒ�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�

//evidence.target='./build/Zabbix�Ď��ݒ�`�F�b�N�V�[�g_<date>.xlsx'
evidence.target='./build/zabbix_check_sheet.xlsx'

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

// ���񉻂��Ȃ��^�X�N

test.serialization.tasks = []

// DryRun���[�h���O�ۑ���

test.dry_run_staging_dir = './src/test/resources/log/'

// �R�}���h�̎�̃^�C���A�E�g
test.Zabbix.timeout = 300

// Zabbix�ڑ����

account.Zabbix.Test.server   = '192.168.0.20'
account.Zabbix.Test.user     = 'Admin'
account.Zabbix.Test.password = 'zabbix'

account.Remote.Test2.server   = 'yps4za'
account.Remote.Test2.user     = 'Admin'
account.Remote.Test2.password = 'zabbix'

// Redmine �t�B���^�[�ݒ�

redmine.default_values = [
	'remote_account_id' : 'Test',
	'verify_id' : 'RuleAP',
]
redmine.fixed_items    = [
    'platform' : 'Zabbix'
]
redmine.required_items = ['server_name', 'platform', 'remote_account_id']

redmine.default_filter_options = [
    'project': '�\���Ǘ����؃T�C�g',
    'status': '%',
    'version': '%',
    'tracker': 'IA�T�[�o�[',
]



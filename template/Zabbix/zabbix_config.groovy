// �����d�l�V�[�g��`

evidence.source = './template/Zabbix/Zabbix�Ď��ݒ�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�

//evidence.target='./build/Zabbix�Ď��ݒ�`�F�b�N�V�[�g_<date>.xlsx'
evidence.target='./build/zabbix_check_sheet.xlsx'

// �������ʃ��O�f�B���N�g��
evidence.staging_dir='./build/log'

// �����A���،��ʃf�B���N�g��
evidence.json_dir='./src/test/resources/json'

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


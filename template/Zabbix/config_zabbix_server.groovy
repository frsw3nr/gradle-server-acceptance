// �����d�l�V�[�g��`

evidence.source = './template/Zabbix/ZabbixServer�Ď��ݒ�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�

evidence.target='./build/ZabbixServer�Ď��ݒ�`�F�b�N�V�[�g_<date>.xlsx'
// evidence.target='./build/zabbix_server_check_sheet.xlsx'

// �������ʃ��O�f�B���N�g��

evidence.staging_dir='./build/log'

// �����A���،��ʃf�B���N�g��

evidence.json_dir='./src/test/resources/json'

// ���񉻂��Ȃ��^�X�N
// ����x���w������Ă��A�w�肵���h���C���^�X�N�̓V���A���Ɏ��s����

test.serialization.tasks = []

// DryRun���[�h���O�ۑ���

test.dry_run_staging_dir = './src/test/resources/log/'

// �R�}���h�̎�̃^�C���A�E�g
// Windows,vCenter�̏ꍇ�A�S�R�}���h���܂Ƃ߂��o�b�`�X�N���v�g�̃^�C���A�E�g�l

test.ZabbixServer.timeout = 300

// �R�}���h�̎�̃f�o�b�O���[�h

test.ZabbixServer.debug  = false

// DryRun �\�s���K���[�h

test.ZabbixServer.dry_run   = false

// ZabbixServer�ڑ����

account.ZabbixServer.Test.user     = 'Admin'
account.ZabbixServer.Test.password = 'zabbix'

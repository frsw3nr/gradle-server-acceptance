// �����d�l�V�[�g��`

evidence.source = './����VSP�X�g���[�W�`�F�b�N�V�[�g.xlsx'
evidence.sheet_name_server = '�`�F�b�N�Ώ�'
evidence.sheet_name_rule = '�������[��'
evidence.sheet_name_spec = [
    'HitachiVSP':   '����VSP�`�F�b�N�V�[�g',
]

// �������ʃt�@�C���o�͐�

evidence.target='./build/����VSP�`�F�b�N�V�[�g_<date>.xlsx'

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
    '��r�ΏۃT�[�o��' :    'compare_server',
    'CPU��' :               'NumCpu',
    '��������' :            'MemoryGB',
    'ESXi��' :              'ESXiHost',
    'HDD' :                 'HDDtype',
]

// ���񉻂��Ȃ��^�X�N
// ����x���w������Ă��A�w�肵���h���C���^�X�N�̓V���A���Ɏ��s����

test.serialization.tasks = ['HitachiVSP']

// DryRun���[�h���O�ۑ���

test.dry_run_staging_dir = './src/test/resources/log/'

// �R�}���h�̎�̃^�C���A�E�g

test.HitachiVSP.timeout   = 300

// �R�}���h�̎�̃f�o�b�O���[�h

//test.HitachiVSP.debug = false

// ����VSP �ڑ����

account.HitachiVSP.Test.report_dir = './src/hitachi_vsp_report'

account.HitachiVSP.Test.user     = 'administrator'
account.HitachiVSP.Test.password = 'P@ssw0rd'
account.HitachiVSP.Test2.user     = 'verification'
account.HitachiVSP.Test2.password = 'Hcc12676'

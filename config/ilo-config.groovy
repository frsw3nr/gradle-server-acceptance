// �����d�l�V�[�g��`

evidence.source = './iLO�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�

//evidence.target='./build/iLO�`�F�b�N�V�[�g_<date>.xlsx'
evidence.target='./build/ilo_check_sheet.xlsx'

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

report.item_map.platform.iLO = [
]

// ���񉻂��Ȃ��^�X�N
// ����x���w������Ă��A�w�肵���h���C���^�X�N�̓V���A���Ɏ��s����

test.serialization.tasks = ['iLO']

// DryRun���[�h���O�ۑ���

test.dry_run_staging_dir = './src/test/resources/log/'

// �R�}���h�̎�̃^�C���A�E�g
// Windows,vCenter�̏ꍇ�A�S�R�}���h���܂Ƃ߂��o�b�`�X�N���v�g�̃^�C���A�E�g�l

test.Linux.timeout   = 300
test.Windows.timeout = 300
test.VMHost.timeout  = 300

// �R�}���h�̎�̃f�o�b�O���[�h

// test.Linux.debug   = false
// test.Windows.debug = false
// test.VMHost.debug  = false
test.iLO.debug  = true

// iLO �ڑ����

account.iLO.Test.user      = 'toshiba'
account.iLO.Test.password  = 'Toshiba1048'

// �����d�l�V�[�g��`

evidence.source = './template/FJ_Primergy/PRIMERGY�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�

evidence.target='./build/PRIMERGY�`�F�b�N�V�[�g_<date>.xlsx'
// evidence.target='./build/primergy_check_sheet.xlsx'

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
test.Primergy.timeout = 300

// �R�}���h�̎�̃f�o�b�O���[�h

test.Primergy.debug  = false

// DryRun �\�s���K���[�h

test.Primergy.dry_run   = false

// Primergy�ڑ����

account.Primergy.Test.irmc     = 5
account.Primergy.Test.user     = 'admin'
account.Primergy.Test.password = 'admin'

���p�菇
========

���O����
--------

**�ݒ���m�F**

Linux �ڑ����

	Hostname: ostrich, IP: 192.168.10.1, User: someuser, Password: XXX

vCenter �ڑ����

	IP : 192.168.10.100, User: root, Password: XXXX

VM��

	ostrich

**�����t�@�C���ݒ�**

1.�`�F�b�N�V�[�g.xlsx 

�V�[�g�u�`�F�b�N�Ώ�VM�v�Ɍ����ΏۃT�[�o�̐ڑ����L���B

2. config/config.groovy 

�ڑ����ҏW�B

	// vCenter�ڑ����
	
	account.vCenter.Test.server   = '192.168.10.100'
	account.vCenter.Test.user     = 'root'
	account.vCenter.Test.password = 'XXXX'
	
	// Linux �ڑ����
	
	account.Linux.Test.user      = 'someuser'
	account.Linux.Test.password  = 'XXXX'
	account.Linux.Test.work_dir  = '/tmp/gradle_test'

�������s
--------

**getconfig���s**

server-acceptance�f�B���N�g���Ɉړ����ăe�X�g���s

	cd (�𓀃f�B���N�g��)\server-acceptance
	getconfig

�����ΏۃT�[�o���i�荞�݂����ꍇ

	getconfig -s ostrich

����Ɍ���ID���i�荞�݂����ꍇ

	getconfig -s ostrich -t hostname,lsb

**�������ʊm�F**

build�̉��Ɍ������ʂ��o�͂����B


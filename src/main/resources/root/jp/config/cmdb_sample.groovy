// Configuration Management Database connection setting

// cmdb.dataSource.username = "sa"
// cmdb.dataSource.password = "sa"
// cmdb.dataSource.url = "jdbc:h2:mem:"
// cmdb.dataSource.driver = "org.h2.Driver"

cmdb.dataSource.username = "redmine"
cmdb.dataSource.password = "getperf"
cmdb.dataSource.url = "jdbc:mysql://redmine:3306/redmine?useUnicode=true&characterEncoding=utf8"
cmdb.dataSource.driver = "com.mysql.jdbc.Driver"

// // Redmine custome fields map

// cmdb.redmine.custom_fields = [
//     '�z�X�g��' :         'server_name',
//     'IP�A�h���X' :       'ip',
//     '�v���b�g�t�H�[��' : 'platform',
//     'OS�A�J�E���g' :     'os_account_id',
//     '�ŗL�p�X���[�h' :   'os_specific_password',
//     'vCenter�A�J�E���g': 'remote_account_id',
//     'Zabbix�A�J�E���g':  'remote_account_id',
//     'VM�G�C���A�X��' :   'remote_alias',
//     '����ID' :           'verify_id',
//     '��r�Ώ�' :         'compare_server',
//     'CPU���蓖��' :      'NumCpu',
//     '���������蓖��' :   'MemoryGB',
//     'ESXi�z�X�g' :       'ESXiHost',
//     '�X�g���[�W�\��' :   'HDDtype',
// ]

// �C���x���g���p�`�P�b�g�J�X�^���t�B�[���h��

ticket.custom_field.inventory = '�C���x���g��'

// �|�[�g���X�g�g���b�J�[��

port_list.tracker = '�|�[�g���X�g'

// �|�[�g���X�g�^�p�X�e�[�^�XID
// Redmine �`�P�b�g�X�e�[�^�X���猟��

port_list.in_operation_status_id = 10

// �|�[�g���X�g�`�P�b�g�J�X�^���t�B�[���h���X�g

port_list.custom_fields = [
    '�|�[�g�ԍ�' : 'port_no',
    '�|�[�g�f�o�C�X' : 'description',
    'MAC�A�h���X' : 'mac',
    'MAC�A�h���X�x���_�[' : 'vendor',
    '�X�C�b�`��' : 'switch_name',
    '�l�b�g�}�X�N' : 'netmask',
    '�@����' : 'device_type',
    '�䒠�����킹' : 'lookup',
    '�Ǘ��p' : 'managed',
]

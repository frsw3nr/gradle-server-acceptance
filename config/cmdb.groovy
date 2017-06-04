// Configuration Management Database connection setting

cmdb.dataSource.username = "root"
cmdb.dataSource.password = "getperf"
cmdb.dataSource.url = "jdbc:mysql://ostrich:3306/redmine?useUnicode=true&characterEncoding=utf8"
cmdb.dataSource.driver = "com.mysql.jdbc.Driver"

// Redmine custome fields map

cmdb.redmine.custom_fields = [
    '�z�X�g��' :         'server_name',
    'IP�A�h���X' :       'ip',
    '�v���b�g�t�H�[��' : 'platform',
    'OS�A�J�E���g' :     'os_account_id',
    '�ŗL�p�X���[�h' :   'os_specific_password',
    'vCenter�A�J�E���g': 'remote_account_id',
    'VM�G�C���A�X��' :   'remote_alias',
    '����ID' :           'verify_id',
    '��r�Ώ�' :         'compare_server',
    'CPU���蓖��' :      'NumCpu',
    '���������蓖��' :   'MemoryGB',
    'ESXi�z�X�g' :       'ESXiHost',
    '�X�g���[�W�\��' :   'HDDtype',
]

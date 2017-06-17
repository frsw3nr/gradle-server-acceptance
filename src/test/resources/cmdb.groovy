// Configuration Management Database connection setting

cmdb.dataSource.username = "sa"
cmdb.dataSource.password = "sa"
cmdb.dataSource.url = "jdbc:h2:mem:"
cmdb.dataSource.driver = "org.h2.Driver"

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

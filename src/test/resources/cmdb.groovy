// Configuration Management Database connection setting

cmdb.dataSource.username = "sa"
cmdb.dataSource.password = "sa"
cmdb.dataSource.url = "jdbc:h2:mem:"
cmdb.dataSource.driver = "org.h2.Driver"

// cmdb.dataSource.username = "redmine"
// cmdb.dataSource.password = "getperf"
// cmdb.dataSource.url = "jdbc:mysql://redmine:3306/redmine?useUnicode=true&characterEncoding=utf8"
// cmdb.dataSource.driver = "com.mysql.jdbc.Driver"

// �C���x���g���p�`�P�b�g�J�X�^���t�B�[���h��

ticket.custom_field.inventory = '�C���x���g��'

// �|�[�g���X�g�g���b�J�[��

port_list.tracker = '�|�[�g���X�g'

// �|�[�g���X�g�`�P�b�g�J�X�^���t�B�[���h���X�g

port_list.custom_fields = [
    '�|�[�g�ԍ�' : 'description',
    'MAC�A�h���X' : 'mac',
    'MAC�A�h���X�x���_�[' : 'vendor',
    '�X�C�b�`��' : 'switch_name',
    '�l�b�g�}�X�N' : 'netmask',
    '�@����' : 'device_type',
]

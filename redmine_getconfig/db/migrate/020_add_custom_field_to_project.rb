class AddCustomFieldToProject < ActiveRecord::Migration
  include Redmine::I18n
  def up
    # Custom Field
    owner        = IssueCustomField.find_or_create_by!(name: 'オーナー情報',       field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    platform     = IssueCustomField.find_or_create_by!(name: 'プラットフォーム',   field_format: 'list',   :possible_values => ['オンプレ', '仮想マシン'], is_required: false, editable: true, visible: true, is_for_all: true)
    os           = IssueCustomField.find_or_create_by!(name: 'OS名',               field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    sys_name     = IssueCustomField.find_or_create_by!(name: 'システム',           field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    model_name   = IssueCustomField.find_or_create_by!(name: '機種',               field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    model_type   = IssueCustomField.find_or_create_by!(name: '型番',               field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    cpu          = IssueCustomField.find_or_create_by!(name: 'CPU数',              field_format: 'float',  is_required: false, editable: true, visible: true, is_for_all: true)
    mem          = IssueCustomField.find_or_create_by!(name: 'MEM容量',            field_format: 'float',  is_required: false, editable: true, visible: true, is_for_all: true)
    disk         = IssueCustomField.find_or_create_by!(name: 'ディスク構成',       field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    # inventory    = IssueCustomField.find_or_create_by!(name: 'インベントリ',       field_format: 'link',   url_pattern: '/redmine/inventory?node=%value%&id=%project_identifier%' , is_required: false, editable: true, visible: true, is_for_all: true)
    inventory    = IssueCustomField.find_or_create_by!(name: 'インベントリ',       field_format: 'link',   is_required: false, editable: true, visible: true, is_for_all: true)
    hostgroup    = IssueCustomField.find_or_create_by!(name: 'ホストグループ',     field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    templete     = IssueCustomField.find_or_create_by!(name: '監視テンプレート',   field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    ship_date    = IssueCustomField.find_or_create_by!(name: '搬入日',             field_format: 'date',   is_required: false, editable: true, visible: true, is_for_all: true)
    due_date     = IssueCustomField.find_or_create_by!(name: '引渡し日',           field_format: 'date',   is_required: false, editable: true, visible: true, is_for_all: true)
    support_type = IssueCustomField.find_or_create_by!(name: '保守種別',           field_format: 'list',   :possible_values => ['ワランティー', 'HW保守', 'PF保守', '保守対象外', 'その他'], is_required: false, editable: true, visible: true, is_for_all: true)
    support_info = IssueCustomField.find_or_create_by!(name: '保守契約情報',       field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    serial       = IssueCustomField.find_or_create_by!(name: 'S/N',                field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    sys_code     = IssueCustomField.find_or_create_by!(name: 'システムコード',     field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    rack_no      = IssueCustomField.find_or_create_by!(name: 'ラック番号',         field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    amount       = IssueCustomField.find_or_create_by!(name: '個数',               field_format: 'float',  is_required: false, editable: true, visible: true, is_for_all: true)
    job_id       = IssueCustomField.find_or_create_by!(name: '発番',               field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    product      = IssueCustomField.find_or_create_by!(name: '品名',               field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    ip           = IssueCustomField.find_or_create_by!(name: '接続IP',             field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    switch_name  = IssueCustomField.find_or_create_by!(name: 'スイッチ名',         field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    lan_category = IssueCustomField.find_or_create_by!(name: '上位/下位',          field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    device_type  = IssueCustomField.find_or_create_by!(name: '機器種別',           field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    port_no      = IssueCustomField.find_or_create_by!(name: 'ポート番号',         field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    port_device  = IssueCustomField.find_or_create_by!(name: 'ポートデバイス',     field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    net_category = IssueCustomField.find_or_create_by!(name: 'ネットワーク分類',   field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    mac_vendor   = IssueCustomField.find_or_create_by!(name: 'MACアドレスベンダー',field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    netmask      = IssueCustomField.find_or_create_by!(name: 'ネットマスク',       field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    subnet       = IssueCustomField.find_or_create_by!(name: 'サブネット',         field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    mac_address  = IssueCustomField.find_or_create_by!(name: 'MACアドレス',        field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    host_found   = IssueCustomField.find_or_create_by!(name: '台帳つき合わせ',     field_format: 'bool',   is_required: false, editable: true, visible: true, is_for_all: true)
    location     = IssueCustomField.find_or_create_by!(name: '設置場所',           field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    admin_ip     = IssueCustomField.find_or_create_by!(name: '管理用',             field_format: 'bool',   is_required: false, editable: true, visible: true, is_for_all: true)
    admin_pass   = IssueCustomField.find_or_create_by!(name: '管理者パスワード',   field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)

    # トラッカーとカスタムフィールドの関連付け
    ia_server    = Tracker.find_by_name('IAサーバ')
    sparc_server = Tracker.find_by_name('SPARCサーバ')
    power_server = Tracker.find_by_name('POWERサーバ')
    storage      = Tracker.find_by_name('ストレージ')
    network      = Tracker.find_by_name('ネットワーク')
    portlist     = Tracker.find_by_name('ポートリスト')
    software     = Tracker.find_by_name('ソフトウェア')

    # サーバ用トラッカーのカスタムフィールド設定
    [owner, platform, os, sys_name, model_name, model_type, cpu, mem, inventory, hostgroup, templete, ship_date, due_date, support_type, support_info, serial, sys_code, rack_no, location].each { |custom_field|
        [ia_server, sparc_server, power_server].each { |tracker|
            tracker.custom_fields << custom_field
        }
    }

    # ストレージのカスタムフィールド設定
    [owner, sys_name, model_name, model_type, cpu, inventory, hostgroup, templete, ship_date, due_date, support_type, support_info, serial, sys_code, rack_no, location].each { |custom_field|
        storage.custom_fields << custom_field
    }

    # ネットワークのカスタムフィールド設定
    [ip, os, sys_name, net_category, lan_category, model_name, model_type, inventory, templete, ship_date, due_date, support_type, support_info, serial, sys_code, rack_no, location].each { |custom_field|
        network.custom_fields << custom_field
    }

    # ポートリストのカスタムフィールド設定
    [switch_name, lan_category, port_no, port_device, netmask, subnet, mac_address, mac_vendor, device_type, location, host_found, admin_ip].each { |custom_field|
        portlist.custom_fields << custom_field
    }

    # ソフトウェアのカスタムフィールド設定
    [owner, sys_name, inventory, product, model_type, amount].each { |custom_field|
        software.custom_fields << custom_field
    }

    # 機器に管理者パスワードの設定
    # [ia_server, sparc_server, power_server, storage, network].each { |tracker|
    #     tracker.custom_fields << admin_pass
    # }

    # ロールとカスタムフィールドの関連付け
    # manager   = Role.find_by_position(1) # 管理者
    # developer = Role.find_by_position(2) # 開発者
    # reporter  = Role.find_by_position(3) # 報告者
    # [ip, os, platform, cpu, mem, disk, net, net_mng, section, location, sys_name, model_type, host_group, template, vendor].each { |custom_fields|
    #   [manager, developer, reporter].each { |role|
    #     role.custom_fields << custom_field
    #   }
    # }
    # manager.custom_fields << admin_pass

  end
end

class AddCustomFieldToProject < ActiveRecord::Migration
  include Redmine::I18n
  def up
    # Custom Field
    ip         = IssueCustomField.find_or_create_by!(name: '接続IP',           field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    os         = IssueCustomField.find_or_create_by!(name: 'OS名',             field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    platform   = IssueCustomField.find_or_create_by!(name: 'プラットフォーム', field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    cpu        = IssueCustomField.find_or_create_by!(name: 'CPU数',            field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    mem        = IssueCustomField.find_or_create_by!(name: 'MEM容量',          field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    disk       = IssueCustomField.find_or_create_by!(name: 'ディスク構成',     field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    net        = IssueCustomField.find_or_create_by!(name: 'ネットワーク構成', field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    net_mng    = IssueCustomField.find_or_create_by!(name: '管理LAN',          field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    section    = IssueCustomField.find_or_create_by!(name: '管理部門',         field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    location   = IssueCustomField.find_or_create_by!(name: '設置場所',         field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    sys_name   = IssueCustomField.find_or_create_by!(name: 'システム名',       field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    model_type = IssueCustomField.find_or_create_by!(name: 'モデル名',         field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    host_group = IssueCustomField.find_or_create_by!(name: 'ホストグループ',   field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    template   = IssueCustomField.find_or_create_by!(name: 'テンプレート',     field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    vendor     = IssueCustomField.find_or_create_by!(name: 'ベンダー',         field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)
    admin_pass = IssueCustomField.find_or_create_by!(name: '管理者パスワード', field_format: 'string', is_required: false, editable: true, visible: true, is_for_all: true)

    # トラッカーとカスタムフィールドの関連付け
    ia_server    = Tracker.find_by_name('IAサーバ')
    sparc_server = Tracker.find_by_name('SPARCサーバ')
    power_server = Tracker.find_by_name('POWERサーバ')
    storage      = Tracker.find_by_name('ストレージ')
    network      = Tracker.find_by_name('ネットワーク')
    portlist     = Tracker.find_by_name('ポートリスト')
    software     = Tracker.find_by_name('ソフトウェア')

    # サーバ用トラッカーのカスタムフィールド設定
    [ip, os, platform, cpu, mem, disk, net, net_mng, section, location, host_group, template].each { |custom_field|
        [ia_server, sparc_server, power_server].each { |tracker|
            tracker.custom_fields << custom_field
        }
    }

    # ストレージのカスタムフィールド設定
    [ip, sys_name, model_type, cpu, disk, net, net_mng, section, location, host_group, template].each { |custom_field|
        storage.custom_fields << custom_field
    }

    # ネットワークのカスタムフィールド設定
    [ip, sys_name, model_type, net, net_mng, section, location, host_group, template].each { |custom_field|
        network.custom_fields << custom_field
    }

    # ポートリストのカスタムフィールド設定
    [ip, location, vendor].each { |custom_field|
        portlist.custom_fields << custom_field
    }

    # ソフトウェアのカスタムフィールド設定
    [sys_name, model_type,location, vendor].each { |custom_field|
        software.custom_fields << custom_field
    }

    # 機器に管理者パスワードの設定
    [ia_server, sparc_server, power_server, storage, network].each { |tracker|
        tracker.custom_fields << admin_pass
    }

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

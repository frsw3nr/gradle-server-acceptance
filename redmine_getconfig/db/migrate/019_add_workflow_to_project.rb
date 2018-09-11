class AddWorkflowToProject < ActiveRecord::Migration
  include Redmine::I18n
  def up
    # Status
    plan    = IssueStatus.find_by_name('計画')
    ship    = IssueStatus.find_by_name('搬入')
    deploy  = IssueStatus.find_by_name('構築')
    operate = IssueStatus.find_by_name('運用')
    suspend = IssueStatus.find_by_name('遊休')
    disposal = IssueStatus.find_by_name('廃棄')
    statuses = [plan, ship, deploy, operate, suspend, disposal]

    # Role
    manager   = Role.find_by_position(1) # 管理者
    developer = Role.find_by_position(2) # 開発者
    reporter  = Role.find_by_position(3) # 報告者

    # Tracker
    ia_server = Tracker.find_by_name('IAサーバ')
    sparc_server = Tracker.find_by_name('SPARCサーバ')
    power_server = Tracker.find_by_name('POWERサーバ')
    storage = Tracker.find_by_name('ストレージ')
    network = Tracker.find_by_name('ネットワーク')
    portlist = Tracker.find_by_name('ポートリスト')
    software = Tracker.find_by_name('ソフトウェア')
    trackers = [ia_server, sparc_server, power_server, storage, network, portlist, software]

    # Workflow
    trackers.each { |t|
        WorkflowTransition.where(tracker_id: t.id).destroy_all
    }

    trackers.each { |t|
      statuses.each { |os|
        statuses.each { |ns|
          WorkflowTransition.create!(:tracker_id => t.id, :role_id => manager.id, :old_status_id => os.id, :new_status_id => ns.id) unless os == ns
        }
      }
    }

    trackers.each { |t|
      [plan, ship, deploy, operate, suspend].each { |os|
        [ship, deploy, operate, suspend, disposal].each { |ns|
          WorkflowTransition.create!(:tracker_id => t.id, :role_id => developer.id, :old_status_id => os.id, :new_status_id => ns.id) unless os == ns
        }
      }
    }

    trackers.each { |t|
      [plan, ship, deploy, operate, suspend].each { |os|
        [disposal].each { |ns|
          WorkflowTransition.create!(:tracker_id => t.id, :role_id => reporter.id, :old_status_id => os.id, :new_status_id => ns.id) unless os == ns
        }
      }
      WorkflowTransition.create!(:tracker_id => t.id, :role_id => reporter.id, :old_status_id => suspend.id, :new_status_id => operate.id)
    }
  end
end

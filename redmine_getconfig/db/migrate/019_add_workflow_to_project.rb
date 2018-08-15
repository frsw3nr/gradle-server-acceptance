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

    # Role
    manager   = Role.find_by_position(1) # 管理者
    developer = Role.find_by_position(2) # 開発者
    reporter  = Role.find_by_position(3) # 報告者

    # Workflow
    Tracker.all.each { |t|
      IssueStatus.all.each { |os|
        IssueStatus.all.each { |ns|
          WorkflowTransition.create!(:tracker_id => t.id, :role_id => manager.id, :old_status_id => os.id, :new_status_id => ns.id) unless os == ns
        }
      }
    }

    Tracker.all.each { |t|
      [plan, ship, deploy, operate, suspend].each { |os|
        [ship, deploy, operate, suspend, disposal].each { |ns|
          WorkflowTransition.create!(:tracker_id => t.id, :role_id => developer.id, :old_status_id => os.id, :new_status_id => ns.id) unless os == ns
        }
      }
    }

    Tracker.all.each { |t|
      [plan, ship, deploy, operate, suspend].each { |os|
        [disposal].each { |ns|
          WorkflowTransition.create!(:tracker_id => t.id, :role_id => reporter.id, :old_status_id => os.id, :new_status_id => ns.id) unless os == ns
        }
      }
      WorkflowTransition.create!(:tracker_id => t.id, :role_id => reporter.id, :old_status_id => suspend.id, :new_status_id => operate.id)
    }
  end
end

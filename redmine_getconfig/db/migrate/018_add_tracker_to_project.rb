class AddTrackerToProject < ActiveRecord::Migration
  def up
    plan = IssueStatus.find_by_name('計画')
    Tracker.create!(name: 'IAサーバ',     default_status_id: plan.id, is_in_chlog: true,  is_in_roadmap: false, position: 4)
    Tracker.create!(name: 'SPARCサーバ',  default_status_id: plan.id, is_in_chlog: true,  is_in_roadmap: false, position: 5)
    Tracker.create!(name: 'POWERサーバ',  default_status_id: plan.id, is_in_chlog: true,  is_in_roadmap: false, position: 6)
    Tracker.create!(name: 'ストレージ',   default_status_id: plan.id, is_in_chlog: true,  is_in_roadmap: false, position: 7)
    Tracker.create!(name: 'ネットワーク', default_status_id: plan.id, is_in_chlog: true,  is_in_roadmap: false, position: 8)
    Tracker.create!(name: 'ポートリスト', default_status_id: plan.id, is_in_chlog: true,  is_in_roadmap: false, position: 9)
    Tracker.create!(name: 'ソフトウェア', default_status_id: plan.id, is_in_chlog: true,  is_in_roadmap: false, position: 10)
  end
 
  def down
    Tracker.where(name: 'IAサーバ').first.try :destroy
    Tracker.where(name: 'SPARCサーバ').first.try :destroy
    Tracker.where(name: 'POWERサーバ').first.try :destroy
    Tracker.where(name: 'ストレージ').first.try :destroy
    Tracker.where(name: 'ネットワーク').first.try :destroy
    Tracker.where(name: 'ポートリスト').first.try :destroy
    Tracker.where(name: 'ソフトウェア').first.try :destroy
  end
end

class AddStatusToProject < ActiveRecord::Migration
  def up
    IssueStatus.create!(name: '計画', is_closed: false, position: 7)
    IssueStatus.create!(name: '搬入', is_closed: false, position: 8)
    IssueStatus.create!(name: '構築', is_closed: false, position: 9)
    IssueStatus.create!(name: '運用', is_closed: true,  position: 10)
    IssueStatus.create!(name: '遊休', is_closed: true,  position: 11)
    IssueStatus.create!(name: '廃棄', is_closed: true,  position: 12)
  end
 
  def down
    IssueStatus.where(name: '計画').first.try :destroy
    IssueStatus.where(name: '搬入').first.try :destroy
    IssueStatus.where(name: '構築').first.try :destroy
    IssueStatus.where(name: '運用').first.try :destroy
    IssueStatus.where(name: '遊休').first.try :destroy
    IssueStatus.where(name: '廃棄').first.try :destroy
  end
end

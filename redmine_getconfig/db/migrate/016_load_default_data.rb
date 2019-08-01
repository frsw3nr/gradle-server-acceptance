class LoadDefaultData < ActiveRecord::Migration
  def up
    if Redmine::DefaultData::Loader::no_data?
      Redmine::DefaultData::Loader::load(lang = 'ja')
    end
  end
end

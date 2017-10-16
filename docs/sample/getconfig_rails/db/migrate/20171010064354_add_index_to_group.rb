class AddIndexToGroup < ActiveRecord::Migration[5.1]
  def change
    add_index :groups, [:group_name], :unique => true, :name => 'uk_groups'
  end
end

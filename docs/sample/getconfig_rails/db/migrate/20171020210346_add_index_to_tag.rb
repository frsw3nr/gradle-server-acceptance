class AddIndexToTag < ActiveRecord::Migration[5.1]
  def change
    add_index :tags, [:tag_name], :unique => true, :name => 'uk_tags'
  end
end

class AddIndexToTagNode < ActiveRecord::Migration[5.1]
  def change
    add_index :tag_nodes, [:tag_id, :node_id], :unique => true, :name => 'uk_tag_nodes'
  end
end

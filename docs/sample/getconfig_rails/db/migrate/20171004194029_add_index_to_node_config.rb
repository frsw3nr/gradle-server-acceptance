class AddIndexToNodeConfig < ActiveRecord::Migration[5.1]
  def change
    add_index :node_configs, [:node_id, :platform_id, :item_name], :unique => true, :name => 'uk_node_configs'
  end
end

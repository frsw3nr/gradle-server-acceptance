class AddIndexToNodeConfig < ActiveRecord::Migration[5.1]
  def change
    add_index :node_configs, [:platform_id, :node_id], :unique => true, :name => 'uk_node_configs'
  end
end

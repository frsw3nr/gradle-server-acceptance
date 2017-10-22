class AddIndexToNodeConfigDetail < ActiveRecord::Migration[5.1]
  def change
    add_index :node_config_details, [:node_config_id, :item_name], :unique => true, :name => 'uk_config_details'
  end
end

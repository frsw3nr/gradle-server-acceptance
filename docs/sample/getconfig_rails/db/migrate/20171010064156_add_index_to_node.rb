class AddIndexToNode < ActiveRecord::Migration[5.1]
  def change
    add_index :nodes, [:tenant_id, :node_name], :unique => true, :name => 'uk_nodes'
  end
end

class CreateNodeConfigs < ActiveRecord::Migration
  def change
    create_table :node_configs do |t|
      t.references :platform
      t.references :node
      t.string :item_name, null: false, limit: 24
      t.string :value
    end
    add_index :node_configs, [:platform_id, :node_id, :item_name], unique: true, :name => 'uk_node_configs'
  end
end

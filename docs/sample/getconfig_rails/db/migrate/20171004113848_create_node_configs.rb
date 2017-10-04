class CreateNodeConfigs < ActiveRecord::Migration[5.1]
  def change
    create_table :node_configs do |t|
      t.references :platform, foreign_key: true
      t.references :node, foreign_key: true
      t.string :item_name
      t.string :value

      t.timestamps
    end
  end
end

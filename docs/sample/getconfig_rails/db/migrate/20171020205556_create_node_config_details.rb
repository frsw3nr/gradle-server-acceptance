class CreateNodeConfigDetails < ActiveRecord::Migration[5.1]
  def change
    create_table :node_config_details do |t|
      t.references :node_config, foreign_key: true
      t.string :item_name
      t.text :value

      t.timestamps
    end
  end
end

class CreateDeviceResults < ActiveRecord::Migration
  def change
    create_table :device_results do |t|
      t.references :node
      t.references :metric
      t.integer :seq
      t.string :item_name, null: false, limit: 24
      t.string :value
    end
    add_index :device_results, [:node_id, :metric_id, :seq, :item_name], unique: true, :name => 'uk_device_results'
  end
end

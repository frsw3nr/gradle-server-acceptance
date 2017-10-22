class CreateDeviceResults < ActiveRecord::Migration[5.1]
  def change
    create_table :device_results do |t|
      t.references :node, foreign_key: true
      t.references :metric, foreign_key: true
      t.integer :seq
      t.string :item_name
      t.text :value

      t.timestamps
    end
  end
end

class CreateMetrics < ActiveRecord::Migration[5.1]
  def change
    create_table :metrics do |t|
      t.references :platform, foreign_key: true
      t.string :metric_name
      t.integer :level
      t.boolean :device_flag

      t.timestamps
    end
  end
end

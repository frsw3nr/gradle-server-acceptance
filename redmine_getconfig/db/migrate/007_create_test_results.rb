class CreateTestResults < ActiveRecord::Migration
  def change
    create_table :test_results do |t|
      t.references :node
      t.references :metric
      t.string :value
      t.integer :verify
    end
    add_index :test_results, [:node_id, :metric_id], unique: true
  end
end

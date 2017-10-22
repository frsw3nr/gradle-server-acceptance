class CreateTestResults < ActiveRecord::Migration[5.1]
  def change
    create_table :test_results do |t|
      t.references :node, foreign_key: true
      t.references :metric, foreign_key: true
      t.boolean :verify
      t.text :value

      t.timestamps
    end
  end
end

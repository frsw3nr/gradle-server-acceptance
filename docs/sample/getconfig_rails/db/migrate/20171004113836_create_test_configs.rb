class CreateTestConfigs < ActiveRecord::Migration[5.1]
  def change
    create_table :test_configs do |t|
      t.references :verify_test, foreign_key: true
      t.string :item_name
      t.string :value

      t.timestamps
    end
  end
end
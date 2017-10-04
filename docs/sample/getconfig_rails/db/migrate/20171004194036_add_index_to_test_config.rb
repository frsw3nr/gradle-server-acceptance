class AddIndexToTestConfig < ActiveRecord::Migration[5.1]
  def change
    add_index :test_configs, [:verify_test_id, :item_name], :unique => true, :name => 'uk_test_configs'
  end
end

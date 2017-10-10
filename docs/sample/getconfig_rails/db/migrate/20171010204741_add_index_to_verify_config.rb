class AddIndexToVerifyConfig < ActiveRecord::Migration[5.1]
  def change
    add_index :verify_configs, [:verify_test_id, :item_name], :unique => true, :name => 'uk_verify_configs'
  end
end

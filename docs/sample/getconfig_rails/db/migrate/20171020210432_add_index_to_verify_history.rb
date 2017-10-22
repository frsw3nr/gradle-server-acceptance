class AddIndexToVerifyHistory < ActiveRecord::Migration[5.1]
  def change
    add_index :verify_histories, [:verify_test_id, :node_id, :metric_id], :unique => true, :name => 'uk_verify_histories'
  end
end

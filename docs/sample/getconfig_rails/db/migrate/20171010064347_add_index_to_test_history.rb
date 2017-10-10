class AddIndexToTestHistory < ActiveRecord::Migration[5.1]
  def change
    add_index :test_histories, [:verify_test_id, :metric_id], :unique => true, :name => 'uk_test_histories'
  end
end

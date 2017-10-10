class AddIndexToTestResult < ActiveRecord::Migration[5.1]
  def change
    add_index :test_results, [:node_id, :metric_id], :unique => true, :name => 'uk_test_results'
  end
end

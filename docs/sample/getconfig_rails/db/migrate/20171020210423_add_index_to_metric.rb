class AddIndexToMetric < ActiveRecord::Migration[5.1]
  def change
    add_index :metrics, [:platform_id, :metric_name], :unique => true, :name => 'uk_metrics'
  end
end

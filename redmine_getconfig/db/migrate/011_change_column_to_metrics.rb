class ChangeColumnToMetrics < ActiveRecord::Migration
  def change
    change_column :metrics, :metric_name, :string, limit: 48
  end
end

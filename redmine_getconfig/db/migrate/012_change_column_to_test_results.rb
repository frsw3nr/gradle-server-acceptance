class ChangeColumnToTestResults < ActiveRecord::Migration
  def change
    change_column :test_results, :value, :string, limit: 4000
  end
end

class ChangeColumnToDeviceResults < ActiveRecord::Migration
  def change
    change_column :device_results, :value, :string, limit: 4000
  end
end

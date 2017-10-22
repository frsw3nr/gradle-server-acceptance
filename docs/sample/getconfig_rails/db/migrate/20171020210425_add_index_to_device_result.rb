class AddIndexToDeviceResult < ActiveRecord::Migration[5.1]
  def change
    add_index :device_results, [:node_id, :metric_id, :seq, :item_name], :unique => true, :name => 'uk_device_results'
  end
end

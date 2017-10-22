class AddIndexToPlatformConfigDetail < ActiveRecord::Migration[5.1]
  def change
    add_index :platform_config_details, [:platform_id, :item_name], :unique => true, :name => 'uk_platform_config_details'
  end
end

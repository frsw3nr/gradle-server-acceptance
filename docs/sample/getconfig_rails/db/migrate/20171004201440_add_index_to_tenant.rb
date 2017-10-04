class AddIndexToTenant < ActiveRecord::Migration[5.1]
  def change
    add_index :tenants, [:tenant_name], :unique => true, :name => 'uk_tenants'
  end
end

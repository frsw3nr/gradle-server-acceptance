class CreateTenants < ActiveRecord::Migration
  def change
    create_table :tenants do |t|
      t.string :tenant_name, null: false, limit: 24
    end

    add_index :tenants, :tenant_name, unique: true

    Tenant.create!( tenant_name: "_Default" )
  end
end

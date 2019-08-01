class CreateNetworks < ActiveRecord::Migration
  def change
    create_table :networks do |t|
      t.string :network_name
      t.string :location
      t.integer :network_address,   limit: 8, null: false, default: 0
      t.integer :subnet_mask,       limit: 8, null: false, default: 0
      t.integer :broadcast_address, limit: 8, null: false, default: 0
    end
  end
end

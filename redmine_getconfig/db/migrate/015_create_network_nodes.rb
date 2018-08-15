class CreateNetworkNodes < ActiveRecord::Migration
  def change
    create_table :network_nodes do |t|
      t.references :network
      t.references :node
      t.string :ip
      t.integer :network_address,   limit: 8, null: false, default: 0
      t.integer :subnet_mask,       limit: 8, null: false, default: 0
    end
  end
end

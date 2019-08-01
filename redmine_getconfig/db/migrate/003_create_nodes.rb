class CreateNodes < ActiveRecord::Migration
  def change
    create_table :nodes do |t|
      t.references :tenant
      t.string :node_name, null: false, limit: 24
      t.string :ip
    end

    add_index :nodes, :node_name, unique: true
  end
end

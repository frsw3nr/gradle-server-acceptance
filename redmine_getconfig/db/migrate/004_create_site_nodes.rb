class CreateSiteNodes < ActiveRecord::Migration
  def change
    create_table :site_nodes do |t|
      t.references :site
      t.references :node
    end
    add_index :site_nodes, [:site_id, :node_id], unique: true
  end
end

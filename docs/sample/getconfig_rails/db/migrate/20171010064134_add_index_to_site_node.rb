class AddIndexToSiteNode < ActiveRecord::Migration[5.1]
  def change
    add_index :site_nodes, [:site_id, :node_id], :unique => true, :name => 'uk_site_nodes'
  end
end

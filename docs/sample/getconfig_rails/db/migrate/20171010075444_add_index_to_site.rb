class AddIndexToSite < ActiveRecord::Migration[5.1]
  def change
    add_index :sites, [:site_name], :unique => true, :name => 'uk_sites'
  end
end

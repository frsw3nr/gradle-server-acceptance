class CreateSiteNodes < ActiveRecord::Migration[5.1]
  def change
    create_table :site_nodes do |t|
      t.references :site, foreign_key: true
      t.references :node, foreign_key: true

      t.timestamps
    end
  end
end

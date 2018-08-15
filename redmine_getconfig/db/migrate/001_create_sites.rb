class CreateSites < ActiveRecord::Migration
  def change
    create_table :sites do |t|
      t.string :site_name, null: false, limit: 24
    end

    add_index :sites, :site_name, unique: true
  end
end

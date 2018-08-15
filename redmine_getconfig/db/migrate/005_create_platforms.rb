class CreatePlatforms < ActiveRecord::Migration
  def change
    create_table :platforms do |t|
      t.string :platform_name, null: false, limit: 24
    end

    add_index :platforms, :platform_name, unique: true
  end
end

class CreatePlatforms < ActiveRecord::Migration[5.1]
  def change
    create_table :platforms do |t|
      t.string :platform_name
      t.integer :build

      t.timestamps
    end
  end
end

class CreatePlatformConfigDetails < ActiveRecord::Migration[5.1]
  def change
    create_table :platform_config_details do |t|
      t.references :platform, foreign_key: true
      t.string :item_name
      t.text :value

      t.timestamps
    end
  end
end

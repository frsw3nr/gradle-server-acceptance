class CreatePlatformConfigDetails < ActiveRecord::Migration[5.1]
  def change
    create_table :platform_config_details do |t|
      t.references :platform, foreign_key: {on_delete: :cascade}
      t.string :item_name
      t.text :value

      t.timestamps
    end
  end
end

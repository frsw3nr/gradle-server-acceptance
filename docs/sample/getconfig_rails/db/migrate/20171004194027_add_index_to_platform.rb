class AddIndexToPlatform < ActiveRecord::Migration[5.1]
  def change
    add_index :platforms, [:platform_name], :unique => true, :name => 'uk_platforms'
  end
end

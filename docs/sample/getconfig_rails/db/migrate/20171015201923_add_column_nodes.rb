class AddColumnNodes < ActiveRecord::Migration[5.1]
  def change
    add_column :nodes, :alias_name, :string
  end
end

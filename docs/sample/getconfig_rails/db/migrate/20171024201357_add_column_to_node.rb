class AddColumnToNode < ActiveRecord::Migration[5.1]
  def change
    add_column :nodes, :compare_node, :string
  end
end

class CreateTagNodes < ActiveRecord::Migration[5.1]
  def change
    create_table :tag_nodes do |t|
      t.references :tag, foreign_key: {on_delete: :cascade}
      t.references :node, foreign_key: {on_delete: :cascade}

      t.timestamps
    end
  end
end

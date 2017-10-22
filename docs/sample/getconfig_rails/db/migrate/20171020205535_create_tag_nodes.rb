class CreateTagNodes < ActiveRecord::Migration[5.1]
  def change
    create_table :tag_nodes do |t|
      t.references :tag, foreign_key: true
      t.references :node, foreign_key: true

      t.timestamps
    end
  end
end

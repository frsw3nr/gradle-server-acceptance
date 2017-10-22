class CreateNodes < ActiveRecord::Migration[5.1]
  def change
    create_table :nodes do |t|
      t.references :group, foreign_key: true
      t.string :node_name
      t.string :ip
      t.string :specific_password
      t.string :alias_name

      t.timestamps
    end
  end
end

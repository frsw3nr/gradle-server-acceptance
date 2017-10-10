class CreateNodes < ActiveRecord::Migration[5.1]
  def change
    create_table :nodes do |t|
      t.references :tenant, foreign_key: true
      t.string :node_name
      t.string :ip
      t.string :specific_password

      t.timestamps
    end
  end
end

class CreateAccounts < ActiveRecord::Migration[5.1]
  def change
    create_table :accounts do |t|
      t.references :node, foreign_key: true
      t.references :platform, foreign_key: true
      t.string :account_name
      t.string :user_name
      t.string :password

      t.timestamps
    end
  end
end

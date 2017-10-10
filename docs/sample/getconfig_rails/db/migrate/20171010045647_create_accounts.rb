class CreateAccounts < ActiveRecord::Migration[5.1]
  def change
    create_table :accounts do |t|
      t.string :account_name
      t.string :user_name
      t.string :password
      t.string :remote_ip

      t.timestamps
    end
  end
end

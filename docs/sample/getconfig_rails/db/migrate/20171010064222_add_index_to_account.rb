class AddIndexToAccount < ActiveRecord::Migration[5.1]
  def change
    add_index :accounts, [:account_name], :unique => true, :name => 'uk_accounts'
  end
end

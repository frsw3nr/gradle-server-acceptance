class AddIndexToAccount < ActiveRecord::Migration[5.1]
    add_index :accounts, [:node_id, :platform_id, :account_name], :unique => true, :name => 'uk_accounts'
  def change
  end
end

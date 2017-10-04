class AddIndexToVerifyTest < ActiveRecord::Migration[5.1]
  def change
    add_index :verify_tests, [:test_name], :unique => true, :name => 'uk_verify_tests'
  end
end

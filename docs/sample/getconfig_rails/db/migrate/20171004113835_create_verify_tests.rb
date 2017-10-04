class CreateVerifyTests < ActiveRecord::Migration[5.1]
  def change
    create_table :verify_tests do |t|
      t.string :test_name

      t.timestamps
    end
  end
end

class CreateVerifyHistories < ActiveRecord::Migration[5.1]
  def change
    create_table :verify_histories do |t|
      t.references :verify_test, foreign_key: true
      t.references :metric, foreign_key: true
      t.boolean :verified

      t.timestamps
    end
  end
end

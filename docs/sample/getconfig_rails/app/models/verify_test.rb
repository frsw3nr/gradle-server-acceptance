class VerifyTest < ApplicationRecord
  has_many :test_histories
  has_many :metrics, through: :test_histories
  has_many :test_configs
end

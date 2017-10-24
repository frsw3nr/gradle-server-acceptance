class VerifyTest < ApplicationRecord
  has_many :verify_histories
  has_many :nodes, through: :verify_histories
  accepts_nested_attributes_for :verify_histories

  has_many :verify_configs, dependent: :destroy
  accepts_nested_attributes_for :verify_configs, allow_destroy: true
end

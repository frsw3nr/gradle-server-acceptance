class Platform < ApplicationRecord
  has_many :accounts
  has_many :nodes, through: :accounts
  has_many :node_configs
  has_many :nodes, through: :node_configs
end

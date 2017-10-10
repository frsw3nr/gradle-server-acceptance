class Platform < ApplicationRecord
  has_many :node_configs
  has_many :nodes, through: :node_configs
  has_many :metrics
end

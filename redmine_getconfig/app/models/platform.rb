class Platform < ActiveRecord::Base
  unloadable
  has_many :metrics
  has_many :nodes, through: :node_configs
end

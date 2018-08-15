class Network < ActiveRecord::Base
  unloadable
  has_many :network_nodes
  has_many :nodes, through: :network_nodes
end

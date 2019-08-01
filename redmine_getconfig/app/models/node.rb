class Node < ActiveRecord::Base
  unloadable
  belongs_to :tenant
  has_many :site_nodes
  has_many :sites, through: :site_nodes
  has_many :network_nodes
  has_many :networks, through: :network_nodes
  has_many :node_configs
  has_many :platforms, through: :node_configs
  has_many :test_results
  has_many :metrics, through: :test_results
  has_many :device_results
  has_many :metrics, through: :device_results
end

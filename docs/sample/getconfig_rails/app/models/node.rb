class Node < ApplicationRecord
  belongs_to :tenant
  has_many :site_nodes
  has_many :sites, through: :site_nodes
  has_many :node_configs
  has_many :platforms, through: :node_configs
  accepts_nested_attributes_for :platforms
  has_many :accounts
  has_many :platforms, through: :accounts
  has_many :test_results
  has_many :metrics, through: :test_results
  has_many :device_results
  has_many :metrics, through: :device_results
  paginates_per 200
end

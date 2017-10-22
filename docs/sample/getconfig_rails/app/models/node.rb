class Node < ApplicationRecord
  belongs_to :group
  has_many :tag_nodes, dependent: :destroy
  has_many :tags, through: :tag_nodes
  accepts_nested_attributes_for :tag_nodes, allow_destroy: true
  has_many :node_configs, dependent: :destroy
  has_many :platforms, through: :node_configs
  accepts_nested_attributes_for :node_configs, allow_destroy: true
  has_many :test_results
  has_many :metrics, through: :test_results
  has_many :device_results
  has_many :metrics, through: :device_results

  validates :node_name, uniqueness: { scope: [:group_id] }
  paginates_per 200
end

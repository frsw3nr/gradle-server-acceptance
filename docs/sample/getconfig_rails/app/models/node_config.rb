class NodeConfig < ApplicationRecord
  belongs_to :platform
  belongs_to :node
  belongs_to :account, optional: true
  has_many :node_config_details
  accepts_nested_attributes_for :node_config_details, allow_destroy: true
end

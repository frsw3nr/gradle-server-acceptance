class NodeConfig < ApplicationRecord
  belongs_to :platform
  belongs_to :node
  belongs_to :account, optional: true
  has_many :node_config_details, dependent: :destroy
  accepts_nested_attributes_for :node_config_details, allow_destroy: true
  accepts_nested_attributes_for :platform, :reject_if => :all_blank

  validates :platform_id, uniqueness: { scope: [:node_id] }
end

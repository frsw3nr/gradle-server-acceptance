class NodeConfig < ApplicationRecord
  belongs_to :platform
  belongs_to :node
  belongs_to :account
end

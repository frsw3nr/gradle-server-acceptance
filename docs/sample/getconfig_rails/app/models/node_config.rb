class NodeConfig < ApplicationRecord
  belongs_to :platform
  belongs_to :node
end

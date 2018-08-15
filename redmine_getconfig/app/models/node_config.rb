class NodeConfig < ActiveRecord::Base
  unloadable
  belongs_to :platform
  belongs_to :node
end

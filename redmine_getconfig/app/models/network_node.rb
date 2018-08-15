class NetworkNode < ActiveRecord::Base
  unloadable
  belongs_to :network
  belongs_to :node
end

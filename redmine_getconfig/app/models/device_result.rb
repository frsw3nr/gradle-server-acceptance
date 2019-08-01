class DeviceResult < ActiveRecord::Base
  unloadable
  belongs_to :metric
  belongs_to :node
  paginates_per 200
end

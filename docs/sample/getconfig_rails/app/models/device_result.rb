class DeviceResult < ApplicationRecord
  belongs_to :node
  belongs_to :metric
  paginates_per 10
end

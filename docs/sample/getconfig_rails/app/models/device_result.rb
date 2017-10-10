class DeviceResult < ApplicationRecord
  belongs_to :node
  belongs_to :metric
end

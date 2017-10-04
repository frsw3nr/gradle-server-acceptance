class Account < ApplicationRecord
  belongs_to :node
  belongs_to :platform
end

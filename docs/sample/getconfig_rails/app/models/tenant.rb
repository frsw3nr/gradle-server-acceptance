class Tenant < ApplicationRecord
  has_many :nodes
end

class Tenant < ActiveRecord::Base
  unloadable
  has_many :nodes
end

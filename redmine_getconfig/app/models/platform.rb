class Platform < ActiveRecord::Base
  unloadable
  has_many :metrics
end

class Metric < ActiveRecord::Base
  unloadable
  belongs_to :platform
  has_many :test_results
  has_many :nodes, through: :test_results
  has_many :device_results
  has_many :nodes, through: :device_results
end

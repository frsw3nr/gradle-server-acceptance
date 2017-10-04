class Site < ApplicationRecord
  has_many :site_nodes
  has_many :nodes, through: :site_nodes
end

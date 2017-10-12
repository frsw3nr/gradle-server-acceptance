class Tag < ApplicationRecord
  has_many :tag_nodes
  has_many :nodes, through: :tag_nodes
end

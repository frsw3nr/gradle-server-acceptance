class TagNode < ApplicationRecord
  belongs_to :tag
  belongs_to :node
end

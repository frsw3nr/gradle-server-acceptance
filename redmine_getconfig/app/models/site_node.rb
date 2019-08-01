class SiteNode < ActiveRecord::Base
  unloadable
  belongs_to :site
  belongs_to :node
end

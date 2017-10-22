class VerifyHistory < ApplicationRecord
  belongs_to :verify_test
  belongs_to :node
  belongs_to :metric
end

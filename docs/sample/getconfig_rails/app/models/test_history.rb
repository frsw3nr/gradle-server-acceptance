class TestHistory < ApplicationRecord
  belongs_to :verify_test
  belongs_to :metric
end

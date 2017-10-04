require 'test_helper'

class TestResultControllerTest < ActionDispatch::IntegrationTest
  test "should get index" do
    get test_result_index_url
    assert_response :success
  end

end

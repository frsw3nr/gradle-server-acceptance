require 'test_helper'

class VerifyTestsControllerTest < ActionDispatch::IntegrationTest
  setup do
    @verify_test = verify_tests(:one)
  end

  test "should get index" do
    get verify_tests_url
    assert_response :success
  end

  test "should get new" do
    get new_verify_test_url
    assert_response :success
  end

  test "should create verify_test" do
    assert_difference('VerifyTest.count') do
      post verify_tests_url, params: { verify_test: {  } }
    end

    assert_redirected_to verify_test_url(VerifyTest.last)
  end

  test "should show verify_test" do
    get verify_test_url(@verify_test)
    assert_response :success
  end

  test "should get edit" do
    get edit_verify_test_url(@verify_test)
    assert_response :success
  end

  test "should update verify_test" do
    patch verify_test_url(@verify_test), params: { verify_test: {  } }
    assert_redirected_to verify_test_url(@verify_test)
  end

  test "should destroy verify_test" do
    assert_difference('VerifyTest.count', -1) do
      delete verify_test_url(@verify_test)
    end

    assert_redirected_to verify_tests_url
  end
end

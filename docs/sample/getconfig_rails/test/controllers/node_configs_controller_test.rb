require 'test_helper'

class NodeConfigsControllerTest < ActionDispatch::IntegrationTest
  setup do
    @node_config = node_configs(:one)
  end

  test "should get index" do
    get node_configs_url
    assert_response :success
  end

  test "should get new" do
    get new_node_config_url
    assert_response :success
  end

  test "should create node_config" do
    assert_difference('NodeConfig.count') do
      post node_configs_url, params: { node_config: {  } }
    end

    assert_redirected_to node_config_url(NodeConfig.last)
  end

  test "should show node_config" do
    get node_config_url(@node_config)
    assert_response :success
  end

  test "should get edit" do
    get edit_node_config_url(@node_config)
    assert_response :success
  end

  test "should update node_config" do
    patch node_config_url(@node_config), params: { node_config: {  } }
    assert_redirected_to node_config_url(@node_config)
  end

  test "should destroy node_config" do
    assert_difference('NodeConfig.count', -1) do
      delete node_config_url(@node_config)
    end

    assert_redirected_to node_configs_url
  end
end

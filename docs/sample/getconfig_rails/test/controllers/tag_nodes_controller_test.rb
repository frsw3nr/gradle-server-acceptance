require 'test_helper'

class TagNodesControllerTest < ActionDispatch::IntegrationTest
  setup do
    @tag_node = tag_nodes(:one)
  end

  test "should get index" do
    get tag_nodes_url
    assert_response :success
  end

  test "should get new" do
    get new_tag_node_url
    assert_response :success
  end

  test "should create tag_node" do
    assert_difference('TagNode.count') do
      post tag_nodes_url, params: { tag_node: {  } }
    end

    assert_redirected_to tag_node_url(TagNode.last)
  end

  test "should show tag_node" do
    get tag_node_url(@tag_node)
    assert_response :success
  end

  test "should get edit" do
    get edit_tag_node_url(@tag_node)
    assert_response :success
  end

  test "should update tag_node" do
    patch tag_node_url(@tag_node), params: { tag_node: {  } }
    assert_redirected_to tag_node_url(@tag_node)
  end

  test "should destroy tag_node" do
    assert_difference('TagNode.count', -1) do
      delete tag_node_url(@tag_node)
    end

    assert_redirected_to tag_nodes_url
  end
end

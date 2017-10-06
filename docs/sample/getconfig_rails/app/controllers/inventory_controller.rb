class InventoryController < ApplicationController
  def wildcard(keyword = nil)
    (keyword.blank?) ? '%' : "%#{keyword}%"
  end

  def index
    @tenant   = params[:tenant] || '%'
    @node     = params[:node]   || '%'
    @platform = params[:platform] || '%'
    @metric   = params[:metric] || '%'
binding.pry
    node_ids   = Node.joins(:tenant).where(
                    'tenants.tenant_name like ? and node_name like ?',
                    wildcard(@tenant),
                    wildcard(@node)).ids
    metric_ids = Metric.joins(:platform).where(
                    'platforms.platform_name like ? and metric_name like ?',
                    wildcard(@platform),
                    wildcard(@metric)).ids
    @inventories = TestResult.where(
                        node_id: node_ids, metric_id: metric_ids
                    ).includes(:node, :metric).page(params[:page])
    # @devices = Metric.joins(:platform).where(
    #            device_flag: true, id:@inventories.pluck(:metric_id))
    @devices = Metric.joins(:platform).where(
                'platforms.platform_name like ? and device_flag = 1', wildcard(@platform))
  end
end

class InventoryController < ApplicationController
  unloadable
  include Common

  def index
    # respond_to do |format|
    #   format.html
    #   format.json do
    #     render json: InventoryDatatable.new(view_context)
    #   end
    # end
    @tenant   = params[:tenant] || '%'
    @node     = params[:node]   || '%'
    @platform = params[:platform] || '%'
    @metric   = params[:metric] || '%'

    # @project = Project.find(session[:query][:project_id])
    # @project = Project.find(1)
    # @project = Project.find(params[:id] || session[:project_id] || session[:issue_query][:project_id])

    # project_id をURLパラメータ、セッションから取得。取得できない場合は1をセット
    project_id = params[:id] || session[:project_id]
    if project_id.nil?
      if session[:issue_query].present?
        project_id = session[:issue_query][:project_id]
      else
        project_id = 1
      end
    end
    @project = Project.find(project_id)
    session[:project_id] = @project.id

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
    # @devices = Metric.joins(:platform).where(
    #             'platforms.platform_name like ? and device_flag = 1', wildcard(@platform))

    sql = <<-EOS
    select distinct
        metric_id,
        platform_name,
        metric_name
    from
        device_results,
        metrics,
        nodes,
        platforms
    where
        device_results.metric_id = metrics.id
    and metrics.platform_id = platforms.id
    and device_results.node_id = nodes.id
    and platforms.platform_name like '%s'
    and nodes.node_name like '%s'
    EOS
    @devices  = ActiveRecord::Base.connection.select_all(sql % [wildcard(@platform),
                                                                wildcard(@node)])
    # binding.pry
  end

end

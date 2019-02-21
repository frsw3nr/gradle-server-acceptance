class DeviceInventoryController < ApplicationController
  unloadable
  include Common

  # def wildcard(keyword = nil)
  #   (keyword.blank?) ? '%' : "%#{keyword}%"
  # end

  def index
    @tenant   = params[:tenant] || '%'
    @node     = params[:node]   || '%'
    @platform = params[:platform] || '%'

# binding.pry
    # @project = Project.find(session[:query][:project_id])
    @project = Project.find(session[:project_id])

    nodes = Node.joins(:tenant).where(
           'tenants.tenant_name like ? and node_name like ?',
           wildcard(@tenant),
           wildcard(@node))

    return head(:not_found) if nodes.ids.blank?

    @metric_id = params[:device][:id]
    device_rows = DeviceResult.where(
                node_id: nodes.ids, metric_id: @metric_id
            ).select(
                :node_id, :metric_id, :seq
            ).uniq
    # モデルから検索した配列に対してページ分割
    @rows = Kaminari.paginate_array(device_rows).page(params[:page])
    # binding.pry
    # @rows = DeviceResult.where(
    #             node_id: nodes.ids, metric_id: @metric_id
    #         ).select(
    #             :node_id, :metric_id, :seq
    #         ).uniq.page(
    #             params[:page]
    #         )

    @tables = []
    @rows.each do |row|
        DeviceResult.where(
            node_id: row.node_id, metric_id: row.metric_id, seq: row.seq
        ).includes(
            :node, :metric
        ).group_by(&:seq).each do |seq, devices|
          record = {}
          record[:seq] = seq
          record[:node] = devices[0].node.node_name
          devices.each do |device|
            record[device.item_name] = device.value
          end
          @tables.append(record)
        end
    end
    @headers = []
    # return head(:not_found) if @tables.blank?
    if !@tables.blank?
      @headers = @tables[0].keys
    end

    platform_name = Metric.find(@metric_id).platform.platform_name
    @devices = Metric.joins(:platform).where(
                'device_flag = 1', platform_name)
  end

end

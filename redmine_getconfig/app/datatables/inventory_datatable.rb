class InventoryDatatable < AjaxDatatablesRails::Base
  # uncomment the appropriate paginator module,
  # depending on gems available in your project.
  include AjaxDatatablesRails::Extensions::Kaminari
  # include AjaxDatatablesRails::Extensions::WillPaginate
  # include AjaxDatatablesRails::Extensions::SimplePaginator

  def initialize(view_context, node_name)
    @node_name = node_name
    super(view_context)
    binding.pry
  end

  def sortable_columns
    # list columns inside the Array in string dot notation.
    # Example: 'users.email'
    @sortable_columns ||= []
  end

  def searchable_columns
    @searchable_columns ||= ['inventorys.metric.metric_name']
  end

  private

  def data
    records.map do |record|
      [
        record.node.tenant.tenant_name,
        record.metric.platform.platform_name,
        record.metric.metric_name,
        record.value,
        record.verify
      ]
    end
  end

  def get_raw_records
    binding.pry
    Node.find_by(node_name: @node_name).test_results
  end

  # ==== Insert 'presenter'-like methods below if necessary
end

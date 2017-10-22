class NodesController < ApplicationController
  before_action :set_node, only: [:show, :edit, :update, :destroy]
  after_action :set_node_config, only: [:create, :update]

  def wildcard(keyword = nil)
    (keyword.blank?) ? '%' : "%#{keyword}%"
  end

  # GET /nodes
  # GET /nodes.json
  def index
    @filter_group = params[:filter_group] || '%'
    @filter_node   = params[:filter_node]   || '%'
    @tags  = Tag.all
    if (params[:tag_id])
      session[:tag_id] = (params[:tag_id] == "")? nil : params[:tag_id]
    end

    if (session[:tag_id])
      @nodes = Node.distinct.joins(:group, :tags).where(
        'groups.group_name like ? and tags.id = ? and node_name like ?',
        wildcard(@filter_group), session[:tag_id], wildcard(@filter_node)
      ).page(params[:page])
    elsif
      @nodes = Node.distinct.joins(:group).where(
        'groups.group_name like ? and node_name like ?',
        wildcard(@filter_group), wildcard(@filter_node)
      ).page(params[:page])
    end
  end

  # GET /nodes/1
  # GET /nodes/1.json
  def show
  end

  # GET /nodes/new
  def new
    @node = Node.new
  end

  # GET /nodes/1/edit
  def edit
  end

  # POST /nodes
  # POST /nodes.json
  def create
    binding.pry
    @node = Node.new(node_params)
    respond_to do |format|
      if @node.save
        format.html { redirect_to edit_node_url @node.id, notice: 'Node was successfully created.' }
        format.json { render :show, status: :created, location: @node }
      else
        format.html { render :new }
        format.json { render json: @node.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /nodes/1
  # PATCH/PUT /nodes/1.json
  def update
    respond_to do |format|
      if @node.update(node_params)
        format.html { redirect_to edit_node_url @node.id, notice: 'Node was successfully updated.' }
        format.json { render :show, status: :ok, location: @node }
      else
        format.html { render :edit }
        format.json { render json: @node.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /nodes/1
  # DELETE /nodes/1.json
  def destroy
    @node.destroy
    respond_to do |format|
      format.html { redirect_to nodes_url, notice: 'Node was successfully destroyed.' }
      format.json { head :no_content }
    end
  end

  # GET /nodes/copy/1
  # GET /nodes/copy/1.json
  def copy
    src = Node.find(params[:id])
    @node = src.dup
    @node.tags << src.tags
    src.node_configs.each do |node_config|
      n = node_config.dup
      n.node_id = nil
      node_config.node_config_details.each do |node_config_detail|
        nd = node_config_detail.dup
        nd.node_config_id = nil
        n.node_config_details << nd
      end
      @node.node_configs << n
    end
    @node.node_name  += ' - new'
    @node.alias_name += ' - new'
    @node.ip = nil
    @node.save
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_node
      @node = Node.find(params[:id])
    end

    def set_node_config
      @node.node_configs.each do |n|
        if (n.node_config_details.empty?) then
          PlatformConfigDetail.where(platform_id: n.platform_id).each do |d|
            n.node_config_details <<
             NodeConfigDetail.new(
              node_config_id: n.id, item_name: d.item_name, value: d.value)
          end
        end
      end
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def node_params
      params.fetch(:node, {}).permit(
        :group_id, :node_name, :ip, :specific_password, :alias_name,
        node_configs_attributes: [:id, :_destroy, :platform_id, :node_id, :account_id,
          node_config_details_attributes: [:id, :_destroy, :node_config_id, :item_name, :value]
        ]
      );

    end
end

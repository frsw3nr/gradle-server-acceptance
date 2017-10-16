class NodesController < ApplicationController
  before_action :set_node, only: [:show, :edit, :update, :destroy]
  before_action :set_platform, only: [:copy, :new, :show, :edit]

  def wildcard(keyword = nil)
    (keyword.blank?) ? '%' : "%#{keyword}%"
  end

  # GET /nodes
  # GET /nodes.json
  def index
    @filter_group = params[:group] || '%'
    @filter_node   = params[:node]   || '%'
    @filter_tag    = '%'
    @tag_id = params.dig(*%w/tag id/) || session[:tag_id]
    @tag_id = (@tag_id=="")?nil:@tag_id
# binding.pry
    if (@tag_id)
      @nodes = Node.distinct.joins(:group, :tags).where('groups.group_name like ? and tags.id = ? and node_name like ?', wildcard(@filter_group), @tag_id, wildcard(@filter_node)).page(params[:page])
      session[:tag_id] = @tag_id
    elsif
      @nodes = Node.distinct.joins(:group).where('groups.group_name like ? and node_name like ?', wildcard(@filter_group), wildcard(@filter_node)).page(params[:page])
      session[:tag_id] = nil
    end
    @tags  = Tag.all
  end

  # GET /nodes/1
  # GET /nodes/1.json
  def show
  end

  # GET /nodes/copy/1
  # GET /nodes/copy/1.json
  def copy
    src = Node.find(params[:id])
    @node = src.dup
    @node.tags << src.tags
    @node.node_configs << src.node_configs
    idx = 0
    src.node_configs.each do |node_config|
      @node.node_configs[idx].node_config_details << node_config.node_config_details
      idx += 1
    end

    @node.node_name = nil
    @node.alias_name = nil
    @node.ip = nil
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
    @node = Node.new(node_params)
    if (session[:tag_id]) then
      @node.tags << Tag.find(session[:tag_id])
    end

    respond_to do |format|
      if @node.save
        format.html { redirect_to nodes_url, notice: 'Node was successfully created.' }
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
        format.html { redirect_to nodes_url, notice: 'Node was successfully updated.' }
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

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_node
      @node = Node.find(params[:id])
    end

    def set_platform
      @platforms = Platform.all
      if (session[:tag_id]) then
        @tag = Tag.find(session[:tag_id])
      end
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def node_params
      params.fetch(:node, {}).permit(:group_id, :node_name, :ip, :specific_password,
                                     { :tag_ids => [] },
                                     { :platform_ids => [] },
                                     :platforms => [:id,
                                       :node_configs => [:id, :platform_id, :node_id]
                                     ])
    end
end

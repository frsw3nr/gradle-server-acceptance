class TagNodesController < ApplicationController
  before_action :set_tag_node, only: [:show, :edit, :update, :destroy]

  # GET /tag_nodes
  # GET /tag_nodes.json
  def index
    @tag_nodes = TagNode.all
  end

  # GET /tag_nodes/1
  # GET /tag_nodes/1.json
  def show
  end

  # GET /tag_nodes/new
  def new
    @tag_node = TagNode.new
  end

  # GET /tag_nodes/1/edit
  def edit
  end

  # POST /tag_nodes
  # POST /tag_nodes.json
  def create
    @tag_node = TagNode.new(tag_node_params)

    respond_to do |format|
      if @tag_node.save
        format.html { redirect_to @tag_node, notice: 'Tag node was successfully created.' }
        format.json { render :show, status: :created, location: @tag_node }
      else
        format.html { render :new }
        format.json { render json: @tag_node.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /tag_nodes/1
  # PATCH/PUT /tag_nodes/1.json
  def update
    respond_to do |format|
      if @tag_node.update(tag_node_params)
        format.html { redirect_to @tag_node, notice: 'Tag node was successfully updated.' }
        format.json { render :show, status: :ok, location: @tag_node }
      else
        format.html { render :edit }
        format.json { render json: @tag_node.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /tag_nodes/1
  # DELETE /tag_nodes/1.json
  def destroy
    @tag_node.destroy
    respond_to do |format|
      format.html { redirect_to tag_nodes_url, notice: 'Tag node was successfully destroyed.' }
      format.json { head :no_content }
    end
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_tag_node
      @tag_node = TagNode.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def tag_node_params
      params.fetch(:tag_node, {})
    end
end

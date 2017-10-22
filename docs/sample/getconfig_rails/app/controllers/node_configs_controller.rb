class NodeConfigsController < ApplicationController
  before_action :set_node_config, only: [:show, :edit, :update, :destroy]

  # GET /node_configs
  # GET /node_configs.json
  def index
    @node_configs = NodeConfig.all
  end

  # GET /node_configs/1
  # GET /node_configs/1.json
  def show
  end

  # GET /node_configs/new
  def new
    @node_config = NodeConfig.new
  end

  # GET /node_configs/1/edit
  def edit
  end

  # POST /node_configs
  # POST /node_configs.json
  def create
    @node_config = NodeConfig.new(node_config_params)

    respond_to do |format|
      if @node_config.save
        format.html { redirect_to @node_config, notice: 'Node config was successfully created.' }
        format.json { render :show, status: :created, location: @node_config }
      else
        format.html { render :new }
        format.json { render json: @node_config.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /node_configs/1
  # PATCH/PUT /node_configs/1.json
  def update
    respond_to do |format|
      if @node_config.update(node_config_params)
        format.html { redirect_to @node_config, notice: 'Node config was successfully updated.' }
        format.json { render :show, status: :ok, location: @node_config }
      else
        format.html { render :edit }
        format.json { render json: @node_config.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /node_configs/1
  # DELETE /node_configs/1.json
  def destroy
    @node_config.destroy
    respond_to do |format|
      format.html { redirect_to edit_node_url @node_config.node_id, notice: 'Node config was successfully destroyed.' }
      format.json { head :no_content }
    end
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_node_config
      @node_config = NodeConfig.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def node_config_params
      params.fetch(:node_config, {})
    end
end

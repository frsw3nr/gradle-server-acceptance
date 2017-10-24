class VerifyTestsController < ApplicationController
  before_action :set_verify_test, only: [:show, :edit, :update, :destroy]

  # GET /verify_tests
  # GET /verify_tests.json
  def index
    @verify_tests = VerifyTest.all
  end

  # GET /verify_tests/1
  # GET /verify_tests/1.json
  def show
  end

  # GET /verify_tests/new
  def new
    @verify_test = VerifyTest.new
    ['mode_dry_run', 'mode_silent', 'mode_difference',
      'filter_tag', 'filter_platforms', 'filter_tests',
      'command_timeout', 'report_dir'].each do |item_name|
        @verify_test.verify_configs.build(item_name: item_name)
    end
  end

  # GET /verify_tests/1/edit
  def edit
  end

  # POST /verify_tests
  # POST /verify_tests.json
  def create
    @verify_test = VerifyTest.new(verify_test_params)

    respond_to do |format|
      if @verify_test.save
        format.html { redirect_to @verify_test, notice: 'Verify test was successfully created.' }
        format.json { render :show, status: :created, location: @verify_test }
      else
        format.html { render :new }
        format.json { render json: @verify_test.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /verify_tests/1
  # PATCH/PUT /verify_tests/1.json
  def update
    respond_to do |format|
      if @verify_test.update(verify_test_params)
        format.html { redirect_to @verify_test, notice: 'Verify test was successfully updated.' }
        format.json { render :show, status: :ok, location: @verify_test }
      else
        format.html { render :edit }
        format.json { render json: @verify_test.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /verify_tests/1
  # DELETE /verify_tests/1.json
  def destroy
    @verify_test.destroy
    respond_to do |format|
      format.html { redirect_to verify_tests_url, notice: 'Verify test was successfully destroyed.' }
      format.json { head :no_content }
    end
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_verify_test
      @verify_test = VerifyTest.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def verify_test_params
      params.fetch(:verify_test, {}).permit(
        :test_name,
        verify_configs_attributes: [:id, :_destroy, :verify_test_id, :item_name, :value],
        verify_histories_attributes: [:id, :_destroy, :verify_test_id, :node_id, :metric_id]
      );

    end
end

# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 20171024201357) do

  create_table "accounts", force: :cascade do |t|
    t.string "account_name"
    t.string "user_name"
    t.string "password"
    t.string "remote_ip"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["account_name"], name: "uk_accounts", unique: true
  end

  create_table "device_results", force: :cascade do |t|
    t.integer "node_id"
    t.integer "metric_id"
    t.integer "seq"
    t.string "item_name"
    t.text "value"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["metric_id"], name: "index_device_results_on_metric_id"
    t.index ["node_id", "metric_id", "seq", "item_name"], name: "uk_device_results", unique: true
    t.index ["node_id"], name: "index_device_results_on_node_id"
  end

  create_table "groups", force: :cascade do |t|
    t.string "group_name"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["group_name"], name: "uk_groups", unique: true
  end

  create_table "metrics", force: :cascade do |t|
    t.integer "platform_id"
    t.string "metric_name"
    t.integer "level"
    t.boolean "device_flag"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["platform_id", "metric_name"], name: "uk_metrics", unique: true
    t.index ["platform_id"], name: "index_metrics_on_platform_id"
  end

  create_table "node_config_details", force: :cascade do |t|
    t.integer "node_config_id"
    t.string "item_name"
    t.text "value"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["node_config_id", "item_name"], name: "uk_config_details", unique: true
    t.index ["node_config_id"], name: "index_node_config_details_on_node_config_id"
  end

  create_table "node_configs", force: :cascade do |t|
    t.integer "platform_id"
    t.integer "node_id"
    t.integer "account_id"
    t.string "node_config_name"
    t.string "value"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["account_id"], name: "index_node_configs_on_account_id"
    t.index ["node_id"], name: "index_node_configs_on_node_id"
    t.index ["platform_id", "node_id"], name: "uk_node_configs", unique: true
    t.index ["platform_id"], name: "index_node_configs_on_platform_id"
  end

  create_table "nodes", force: :cascade do |t|
    t.integer "group_id"
    t.string "node_name"
    t.string "ip"
    t.string "specific_password"
    t.string "alias_name"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.string "compare_node"
    t.index ["group_id", "node_name"], name: "uk_nodes", unique: true
    t.index ["group_id"], name: "index_nodes_on_group_id"
  end

  create_table "platform_config_details", force: :cascade do |t|
    t.integer "platform_id"
    t.string "item_name"
    t.text "value"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["platform_id", "item_name"], name: "uk_platform_config_details", unique: true
    t.index ["platform_id"], name: "index_platform_config_details_on_platform_id"
  end

  create_table "platforms", force: :cascade do |t|
    t.string "platform_name"
    t.integer "build"
    t.string "upload_file_name"
    t.binary "upload_file"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["platform_name"], name: "uk_platforms", unique: true
  end

  create_table "tag_nodes", force: :cascade do |t|
    t.integer "tag_id"
    t.integer "node_id"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["node_id"], name: "index_tag_nodes_on_node_id"
    t.index ["tag_id", "node_id"], name: "uk_tag_nodes", unique: true
    t.index ["tag_id"], name: "index_tag_nodes_on_tag_id"
  end

  create_table "tags", force: :cascade do |t|
    t.string "tag_name"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["tag_name"], name: "uk_tags", unique: true
  end

  create_table "test_results", force: :cascade do |t|
    t.integer "node_id"
    t.integer "metric_id"
    t.boolean "verify"
    t.text "value"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["metric_id"], name: "index_test_results_on_metric_id"
    t.index ["node_id", "metric_id"], name: "uk_test_results", unique: true
    t.index ["node_id"], name: "index_test_results_on_node_id"
  end

  create_table "verify_configs", force: :cascade do |t|
    t.integer "verify_test_id"
    t.string "item_name"
    t.text "value"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["verify_test_id", "item_name"], name: "uk_verify_configs", unique: true
    t.index ["verify_test_id"], name: "index_verify_configs_on_verify_test_id"
  end

  create_table "verify_histories", force: :cascade do |t|
    t.integer "verify_test_id"
    t.integer "node_id"
    t.integer "metric_id"
    t.boolean "verified"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["metric_id"], name: "index_verify_histories_on_metric_id"
    t.index ["node_id"], name: "index_verify_histories_on_node_id"
    t.index ["verify_test_id", "node_id", "metric_id"], name: "uk_verify_histories", unique: true
    t.index ["verify_test_id"], name: "index_verify_histories_on_verify_test_id"
  end

  create_table "verify_tests", force: :cascade do |t|
    t.string "test_name"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["test_name"], name: "uk_verify_tests", unique: true
  end

end

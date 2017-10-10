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

ActiveRecord::Schema.define(version: 20171010075444) do

  create_table "accounts", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.string "account_name"
    t.string "user_name"
    t.string "password"
    t.string "remote_ip"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["account_name"], name: "uk_accounts", unique: true
  end

  create_table "device_results", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.bigint "node_id"
    t.bigint "metric_id"
    t.integer "seq"
    t.string "item_name"
    t.string "value"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["metric_id"], name: "index_device_results_on_metric_id"
    t.index ["node_id", "metric_id", "seq", "item_name"], name: "uk_device_results", unique: true
    t.index ["node_id"], name: "index_device_results_on_node_id"
  end

  create_table "metrics", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.bigint "platform_id"
    t.string "metric_name"
    t.integer "level"
    t.boolean "device_flag"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["platform_id", "metric_name"], name: "uk_metrics", unique: true
    t.index ["platform_id"], name: "index_metrics_on_platform_id"
  end

  create_table "node_config_details", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.bigint "node_config_id"
    t.string "item_name"
    t.string "value"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["node_config_id", "item_name"], name: "uk_node_config_details", unique: true
    t.index ["node_config_id"], name: "index_node_config_details_on_node_config_id"
  end

  create_table "node_configs", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.bigint "platform_id"
    t.bigint "node_id"
    t.string "node_config_name"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["node_id", "platform_id"], name: "uk_node_configs", unique: true
    t.index ["node_id"], name: "index_node_configs_on_node_id"
    t.index ["platform_id"], name: "index_node_configs_on_platform_id"
  end

  create_table "nodes", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.bigint "tenant_id"
    t.string "node_name"
    t.string "ip"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["tenant_id", "node_name"], name: "uk_nodes", unique: true
    t.index ["tenant_id"], name: "index_nodes_on_tenant_id"
  end

  create_table "platforms", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.string "platform_name"
    t.integer "build"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["platform_name"], name: "uk_platforms", unique: true
  end

  create_table "site_nodes", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.bigint "site_id"
    t.bigint "node_id"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["node_id"], name: "index_site_nodes_on_node_id"
    t.index ["site_id", "node_id"], name: "uk_site_nodes", unique: true
    t.index ["site_id"], name: "index_site_nodes_on_site_id"
  end

  create_table "sites", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.string "site_name"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["site_name"], name: "uk_sites", unique: true
  end

  create_table "tenants", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.string "tenant_name"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["tenant_name"], name: "uk_tenants", unique: true
  end

  create_table "test_configs", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.bigint "verify_test_id"
    t.string "item_name"
    t.string "value"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["verify_test_id", "item_name"], name: "uk_test_configs", unique: true
    t.index ["verify_test_id"], name: "index_test_configs_on_verify_test_id"
  end

  create_table "test_histories", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.bigint "verify_test_id"
    t.bigint "metric_id"
    t.boolean "verified"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["metric_id"], name: "index_test_histories_on_metric_id"
    t.index ["verify_test_id", "metric_id"], name: "uk_test_histories", unique: true
    t.index ["verify_test_id"], name: "index_test_histories_on_verify_test_id"
  end

  create_table "test_results", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.bigint "node_id"
    t.bigint "metric_id"
    t.boolean "verify"
    t.string "value"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["metric_id"], name: "index_test_results_on_metric_id"
    t.index ["node_id", "metric_id"], name: "uk_test_results", unique: true
    t.index ["node_id"], name: "index_test_results_on_node_id"
  end

  create_table "verify_tests", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.string "test_name"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["test_name"], name: "uk_verify_tests", unique: true
  end

  add_foreign_key "device_results", "metrics"
  add_foreign_key "device_results", "nodes"
  add_foreign_key "metrics", "platforms"
  add_foreign_key "node_config_details", "node_configs"
  add_foreign_key "node_configs", "nodes"
  add_foreign_key "node_configs", "platforms"
  add_foreign_key "nodes", "tenants"
  add_foreign_key "site_nodes", "nodes"
  add_foreign_key "site_nodes", "sites"
  add_foreign_key "test_configs", "verify_tests"
  add_foreign_key "test_histories", "metrics"
  add_foreign_key "test_histories", "verify_tests"
  add_foreign_key "test_results", "metrics"
  add_foreign_key "test_results", "nodes"
end

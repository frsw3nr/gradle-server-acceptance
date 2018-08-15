ajax-datatables-rails-v0-4-0試行
================================

モデル追加
----------

スクリプトコピー

20151025182510_create_cities.rb
tar cvf - app/models/* db/migrate/00* | gzip > ~/work/rails/ajax-datatables-rails-v-0-4-0-how-to/lib.tar.gz

tar cvf - ajax-datatables-rails-v-0-4-0-how-to/ | gzip > ajax-datatables-rails-v-0-4-0-how-to.tar.gz

DB移行
------

rakeコマンド

tar xvf lib.tar.gz
bundle exec rake db:migrate

DBインポート

mysqldump -u root -p redmine > redmine.dmp

mysql -u root -p
CREATE DATABASE datatables DEFAULT CHARACTER SET utf8;

mysql -u root -p datatables < redmine.dmp

  adapter: mysql2
  database: datatables
  host: localhost
  username: root
  password: "getperf"
  encoding: utf8

gem "mysql2"

bundle exec rake db:seed

./bin/rails console -e development

Metric.joins(:nodes).where(nodes: {node_name: 'ostrich'}, device_flag: 1).distinct
@inventories = Node.find_by(node_name: node_name).test_results

Node.find_by(node_name: 'ostrich').test_results


Node.where('node_name like ?', 'ostrich')
Node.where('node_name like ?', '%')

Node.joins(:test_results, :metrics).where('node_name like ?', 'ostrich').select('metrics.metric_name')

検索モデル

検索条件

node_ids   = Node.where('node_name like ?', 'ostrich').ids
metric_ids = Metric.where('metric_name like ?', '%').ids

検索結果

inventories = TestResult.where(node_id: node_ids, metric_id: metric_ids).includes(:node, :metric)

inventories[0].id
inventories[0].node.tenant.tenant_name
inventories[0].metric.platform.platform_name
inventories[0].node.node_name
inventories[0].metric.metric_name
inventories[0].value


コントローラ/ビュー修正
-----------------------

rails generate controller inventory
  app/controllers/inventory_controller.rb
  app/helpers/inventory_helper.rb
  app/assets/javascripts/inventory.coffee
  app/assets/stylesheets/inventory.scss

rails generate datatable Inventory
  app/datatables/inventory_datatable.rb

ルート編集,index サンプル

http://ostrich:3000/inventory/

ビュー作成

データテーブル編集

  # Id
  # Tenant
  # Platform
  # Node
  # Metric
  # Value

Redmine::Plugin.register :redmine_getconfig do
  name 'Getconfig'
  author 'Minoru Furusawa'
  description 'Gradle server acceptance plugin for Redmine'
  version '0.1.34'
  url 'http://github.com/frsw3nr/redmine_getconfig'
  author_url 'http://github.com/frsw3nr/'

  menu :project_menu, :inventory, { :controller => 'inventory', :action => 'index' }, :caption => :inventory

  permission :view_inventory, :inventory => :index
  permission :view_device_inventory, :device_inventory => :index
end

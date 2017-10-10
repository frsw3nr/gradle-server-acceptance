# This file should contain all the record creation needed to seed the database with its default values.
# The data can then be loaded with the rails db:seed command (or created alongside the database with db:setup).
#
# Examples:
#
#   movies = Movie.create([{ name: 'Star Wars' }, { name: 'Lord of the Rings' }])
#   Character.create(name: 'Luke', movie: movies.first)

case Rails.env
when "development", "test"

puts 'Regist Account'
accounts = {}
[['Linux', 'someuser'], ['Windows', 'Administrator'], ['vCenter', 'guest', '192.168.10.10']].each { |info|
    passwd = 'P@ssword'
    remote_ip = info[2] || nil
    accounts[info[0]] = Account.find_or_create_by(account_name: info[0], user_name: info[1], password: passwd, remote_ip: remote_ip)
}

puts 'Regist Platform'
platforms = {}
['Linux', 'Windows', 'vCenter'].each {|name|
    platforms[name] = Platform.find_or_create_by(platform_name: name, build: 1)
}

puts 'Regist Site'
sites = {}
['Tokyo01', 'Tokyo02', 'Nagoya01', 'Nagoya02'].each {|name|
    sites[name] = Site.find_or_create_by(site_name: name)
}

puts 'Regist Tenant'
tenants = {}
['Test01', 'Test02'].each {|name|
    tenants[name] = Tenant.find_or_create_by(tenant_name: name)
}

puts 'Regist Node,NodeConfig,NodeConfigDetail'
[['ostrich', 'Linux', '192.168.10.1'], ['w2016', 'Windows', '192.168.10.2']].each {|info|
    host, platform, ip = info
    node = Node.find_or_create_by(node_name: host, ip: ip)
    node.tenant = tenants['Test01']
    node.save
    SiteNode.find_or_create_by(site_id: sites['Tokyo01'].id, node_id:node.id)
    [platform, 'vCenter'].each{|pf|
        node_config = NodeConfig.find_or_create_by(node_config_name: "%s - %s"%[host, pf],
                                                   platform_id: platforms[pf].id,
                                                   node_id: node.id)
# binding.pry
        node_config.account_id=accounts[pf].id
        node_config.platform_id=platforms[pf].id
        node_config.save
    }

}

end



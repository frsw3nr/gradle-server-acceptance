# This file should contain all the record creation needed to seed the database with its default values.
# The data can then be loaded with the rails db:seed command (or created alongside the database with db:setup).
#
# Examples:
#
#   movies = Movie.create([{ name: 'Star Wars' }, { name: 'Lord of the Rings' }])
#   Character.create(name: 'Luke', movie: movies.first)

# マスター参照用配列
platforms = {}
tags = {}
groups = {}
accounts = {}

puts 'Regist default Platform,Group'
groups['_Default'] = Group.find_or_create_by(group_name: '_Default')
['Linux', 'Windows', 'vCenter'].each {|name|
  platforms[name] = Platform.find_or_create_by(platform_name: name, build: 1)
  case name when 'vCenter'
    PlatformConfigDetail.find_or_create_by(platform_id: platforms[name].id, item_name: 'cpu',     value: nil)
    PlatformConfigDetail.find_or_create_by(platform_id: platforms[name].id, item_name: 'memory',  value: nil)
    PlatformConfigDetail.find_or_create_by(platform_id: platforms[name].id, item_name: 'storage', value: nil)
  end
}

case Rails.env
when "development", "test"

  puts 'Regist Account'
  [['Linux', 'someuser'], ['Windows', 'Administrator'], ['vCenter', 'guest', '192.168.10.10']].each { |info|
    passwd = 'P@ssword'
    remote_ip = info[2] || nil
    accounts[info[0]] = Account.find_or_create_by(account_name: info[0] + 'Account1', user_name: info[1], password: passwd, remote_ip: remote_ip)
  }

  puts 'Regist Tag'
  ['Deploy01', 'Deploy02', 'Deploy03', 'Deploy04'].each {|name|
    tags[name] = Tag.find_or_create_by(tag_name: name)
  }

  puts 'Regist group'
  ['System01', 'System02'].each {|name|
    groups[name] = Group.find_or_create_by(group_name: name)
  }

  puts 'Regist Node,NodeConfig,NodeConfigDetail'
  [['ostrich', 'Linux', '192.168.10.1', 2, 4, 40], ['w2016', 'Windows', '192.168.10.2', 2, 4, 20]].each {|info|
    host, platform, ip, num_cpu, memory_size, disk_size = info
    node = Node.find_or_create_by(node_name: host, ip: ip, alias_name: host)
    node.group = groups['System01']
    node.save
    TagNode.find_or_create_by(tag_id: tags['Deploy01'].id, node_id:node.id)
    [platform, 'vCenter'].each{|pf|
      node_config = NodeConfig.find_or_create_by(node_config_name: "%s - %s"%[host, pf],
                                                 platform_id: platforms[pf].id,
                                                 node_id: node.id)
      node_config.account_id=accounts[pf].id
      node_config.platform_id=platforms[pf].id
      node_config.save

      case pf when 'vCenter'
        NodeConfigDetail.find_or_create_by(node_config_id: node_config.id, item_name: 'cpu',     value: num_cpu)
        NodeConfigDetail.find_or_create_by(node_config_id: node_config.id, item_name: 'memory',  value: memory_size)
        NodeConfigDetail.find_or_create_by(node_config_id: node_config.id, item_name: 'storage', value: disk_size)
      end
    }
  }

  puts 'Regist VerifyTest,VerifyConfig,VerifyHistory'
  verify_test = VerifyTest.find_or_create_by(test_name: 'Deploy01A')
  VerifyConfig.find_or_create_by(verify_test_id: verify_test.id, item_name: 'node_config_file_path', value: nil)

end


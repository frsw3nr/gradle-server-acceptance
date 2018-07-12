def map2 = [
    "net_ip": [
        "null": 1,
        "192.168.10.1": 1,
        "192.168.10.2": 1,
        "192.168.10.3": null,
        "192.168.10.4": "null",
    ],
    "net_onboot": [
        "eth0": 1
    ]
]


map2.each { metric, map ->
    println "METRIC:$metric, MAP:$map"
    while(map.values().remove(null));
    // println "MAP:$map"
    // MAP:[null:1, 192.168.10.1:1, 192.168.10.2:1, 192.168.10.4:null]

    while(map.values().remove("null"));
    // println "MAP:$map"
    // MAP:[null:1, 192.168.10.1:1, 192.168.10.2:1]

    while(map.keySet().remove("null"));
    // MAP:[192.168.10.1:1, 192.168.10.2:1]
    println "METRIC2:$metric, MAP:$map"
}

println "MAP2:$map2"

// // 再帰的には実行されない
// println "MAP:$map2"
// while(map2.values().remove(null));
// while(map2.values().remove("null"));
// while(map2.keySet().remove("null"));
// println "MAP2:$map2"


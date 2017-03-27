Solaris検査シナリオ
===================

共通
====

uptime
------

```
kstat -p unix:0:system_misc:boot_time
unix:0:system_misc:boot_time    1490480704
```

pakages
-------

```
/usr/bin/pkginfo -l

   PKGINST:  SUNWzsh
      NAME:  Z shell (zsh)
  CATEGORY:  system
      ARCH:  i386
   VERSION:  11.10.0,REV=2005.01.08.01.09
   BASEDIR:  /
    VENDOR:  Sun Microsystems, Inc.
      DESC:  Z shell (zsh)
    PSTAMP:  sfw10-x20050108014711
  INSTDATE:  Aug 27 2015 21:54
```

kernel
------

```
bash-3.2$ uname -s
SunOS
bash-3.2$ uname -r
5.10
bash-3.2$ uname -v
Generic_147148-26
bash-3.2$ uname -m
i86pc
bash-3.2$ uname -p
i386
```

hostname
--------

```
hostname
sol
```

plugins/solaris2
================

cpu
---

```
kstat -p cpu_info
cpu_info:0:cpu_info0:brand      Intel(r) Core(tm) i5-4460  CPU @ 3.20GHz
cpu_info:0:cpu_info0:chip_id    0
cpu_info:0:cpu_info0:clock_MHz  3193
cpu_info:0:cpu_info0:core_id    0
cpu_info:0:cpu_info0:cpu_type   i386
cpu_info:0:cpu_info0:current_cstate     0
cpu_info:0:cpu_info0:ncore_per_chip     1
cpu_info:0:cpu_info0:ncpu_per_chip      1
cpu_info:0:cpu_info0:state      on-line
cpu_info:0:cpu_info0:vendor_id  GenuineIntel
```

dmi
---

SNMP経由でDMI?という構成要素を取得できる

Solstice Enterprise Agents (SEA) の技術を採用すれば、SNMP で通信する管理アプリケーションから、
snmpXdmid という DMI マッパーを介して DMI の利用可能な構成要素に対してアクセスできます

filesystem
----------

```
df -ha
ファイルシステム     サイズ 使用済み 使用可能 容量      マウント先
/dev/dsk/c1t0d0s0      6.9G   5.4G   1.4G    80%    /
/devices                 0K     0K     0K     0%    /devices
ctfs                     0K     0K     0K     0%    /system/contract
proc                     0K     0K     0K     0%    /proc
mnttab                   0K     0K     0K     0%    /etc/mnttab
swap                   3.0G   996K   3.0G     1%    /etc/svc/volatile
objfs                    0K     0K     0K     0%    /system/object
sharefs                  0K     0K     0K     0%    /etc/dfs/sharetab
/usr/lib/libc/libc_hwcap1.so.1
                       6.9G   5.4G   1.4G    80%    /lib/libc.so.1
fd                       0K     0K     0K     0%    /dev/fd
swap                   3.0G   184K   3.0G     1%    /tmp
swap                   3.0G    32K   3.0G     1%    /var/run
/dev/dsk/c1t0d0s7       12G   1.7G    10G    15%    /export/home
-hosts                   0K     0K     0K     0%    /net
auto_home                0K     0K     0K     0%    /home
sol:vold(pid560)         0K     0K     0K     0%    /vol
```

memory
------

```
/usr/sbin/prtconf |grep Memory
Memory size: 3072 Megabytes
```

network
-------

```
/usr/sbin/ifconfig -a
lo0: flags=2001000849<UP,LOOPBACK,RUNNING,MULTICAST,IPv4,VIRTUAL> mtu 8232 index 1
        inet 127.0.0.1 netmask ff000000
e1000g0: flags=1000843<UP,BROADCAST,RUNNING,MULTICAST,IPv4> mtu 1500 index 2
        inet 192.168.10.3 netmask ffffff00 broadcast 192.168.10.255
e1000g0:1: flags=1000842<BROADCAST,RUNNING,MULTICAST,IPv4> mtu 1500 index 2
        inet 0.0.0.0 netmask 0
```

```
/usr/sbin/arp -an
Net to Media Table: IPv4
Device   IP Address               Mask      Flags      Phys Addr
------ -------------------- --------------- -------- ---------------
e1000g0 192.168.10.1         255.255.255.255 o        00:0c:29:ca:44:db
e1000g0 192.168.10.254       255.255.255.255 o        00:a0:de:27:02:ed
e1000g0 192.168.10.3         255.255.255.255 SPLA     00:0c:29:6f:38:cf
e1000g0 224.0.0.0            240.0.0.0       SM       01:00:5e:00:00:00
```

```
/usr/sbin/route -v -n get default
newrt->ri_dst: inet 0.0.0.0; newrt->ri_mask: inet 0.0.0.0; newrt->ri_ifp: link ; RTM_GET: Report Metrics: len 360, pid: 0, seq 1, errno 0, flags:<GATEWAY,STATIC>
locks:  inits:
sockaddrs: <DST,NETMASK,IFP>
 default default
   route to: default
destination: default
       mask: default
    gateway: 192.168.10.254
  interface: e1000g0 index 2 address 00 0c 29 6f 38 cf
      flags: <UP,GATEWAY,DONE,STATIC>
 recvpipe  sendpipe  ssthresh    rtt,ms rttvar,ms  hopcount      mtu     expire
       0         0         0         0         0         0      1500         0
locks:  inits: <rttvar,rtt,ssthresh,sendpipe,recvpipe,mtu>
sockaddrs: <DST,GATEWAY,NETMASK,IFP,IFA>
 default 192.168.10.254 default e1000g0:0.c.29.6f.38.cf 192.168.10.3
```

platform
--------

```
/sbin/uname -X
System = SunOS
Node = sol
Release = 5.10
KernelID = Generic_147148-26
Machine = i86pc
BusType = <unknown>
Serial = <unknown>
Users = <unknown>
OEM# = 0
Origin# = 1
NumCPU = 1
```

virtualization
--------------

```
/usr/sbin/zoneadm list -p
0:global:running:/::native:shared
```


```
/usr/sbin/psrinfo -pv
物理プロセッサは 1 個の仮想 プロセッサ を持ちます (0)
  x86 (chipid 0x0 GenuineIntel family 6 model 60 step 3 clock 3193 MHz)
        Intel(r) Core(tm) i5-4460  CPU @ 3.20GHz
```

```
/usr/sbin/zoneadm list -pc
0:global:running:/::native:shared
```

zpools
------

```
/usr/sbin/zpool list -H -o name,size,alloc,free,cap,dedup,health,version
```


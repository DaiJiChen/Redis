# master-slave replication

master-slave replication allows replica Redis instances to be exact copies of master instances. The replica will automatically reconnect to the master every time the link breaks, and will attempt to be an exact copy of it regardless of what happens to the master.

Master is mainly responsible for write  
Slave is mainly responsible for read

### usefulness：  
1. Read/Write Spliting
2. Backup and Disaster Recovery

https://redis.io/topics/replication


### set slave (it's slaves' responsibility to identify their master)

```
127.0.0.1:6380> slaveof 127.0.0.1 6379
OK

127.0.0.1:6380> slaveof no one
OK
```

### you cannot write on a slave

```
127.0.0.1:6380> set hello world
(error) READONLY You can't write against a read only slave.
```

### Three common strategy

1. One master, two slaves
2. master -> slave1 -> slave2 -> slave3
3. Sentinel

### Situation#1: If the master SHUTDOWN, slave won't change their role automatically. Slave will wait for the master until master recovered.

First, we shutdown the master

```
127.0.0.1:6379> SHUTDOWN
not connected> exit
```

As we can see, slave is still a slave, and the master_link_status is down

```
127.0.0.1:6380> info replication
# Replication
role:slave
master_host:127.0.0.1
master_port:6379
master_link_status:down
master_last_io_seconds_ago:-1
master_sync_in_progress:0
slave_repl_offset:1223
master_link_down_since_seconds:13
slave_priority:100
slave_read_only:1
connected_slaves:0
master_repl_offset:0
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0
```

```
127.0.0.1:6381> info replication
# Replication
role:slave
master_host:127.0.0.1
master_port:6379
master_link_status:down
master_last_io_seconds_ago:-1
master_sync_in_progress:0
slave_repl_offset:1223
master_link_down_since_seconds:19
slave_priority:100
slave_read_only:1
connected_slaves:0
master_repl_offset:0
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0
```

Then, we restart the master, and set a new key
```
[ec2-user@ip-172-31-33-9 myRedis]$ ../src/redis-server redisMaster.conf
[ec2-user@ip-172-31-33-9 myRedis]$ redis-cli -p 6379
127.0.0.1:6379> set k3 v3
OK
```

As we can see, slaves also changed, they are still slaves of the master.

```
127.0.0.1:6380> keys *
1) "k2"
2) "k1"
3) "k3"
```

```
127.0.0.1:6381> keys *
1) "k3"
2) "k1"
3) "k2"
```

### Situation#2: If a slave SHUTDOWN, master and other slaves won't be influenced. This slave is no longer a slave after reboot unless you configure it in redis.conf -> REPLICATION: `slaveof <masterip> <masterport>`

First, we shutdown a slave

```
127.0.0.1:6380> SHUTDOWN
not connected>
```

Now, the master only have one slave remaining

```
127.0.0.1:6379> info replication
# Replication
role:master
connected_slaves:1
slave0:ip=127.0.0.1,port=6381,state=online,offset=935,lag=0
master_repl_offset:935
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:2
repl_backlog_histlen:934
```

Then, restart this slave. we can see that it is no longer a slave.

```
[ec2-user@ip-172-31-33-9 myRedis]$ ../src/redis-server redisSlave1.conf
[ec2-user@ip-172-31-33-9 myRedis]$ redis-cli -p 6380
127.0.0.1:6380> info replication
# Replication
role:master
connected_slaves:0
master_repl_offset:0
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0
```

## Sentinel

### capabilities:

- Monitoring. Sentinel constantly checks if your master and replica instances are working as expected.
- Notification. Sentinel can notify the system administrator, or other computer programs, via an API, that something is wrong with one of the monitored Redis instances.
- Automatic failover. If a master is not working as expected, Sentinel can start a failover process where a replica is promoted to master, the other additional replicas are reconfigured to use the new master, and the applications using the Redis server are informed about the new address to use when connecting.
- Configuration provider. Sentinel acts as a source of authority for clients service discovery: clients connect to Sentinels in order to ask for the address of the current Redis master responsible for a given service. If a failover occurs, Sentinels will report the new address.

### How to use

1. Build a configuration file called sentinel.conf

`[ec2-user@ip-172-31-33-9 myRedis]$ touch sentinel.conf`

2. Specify the master and how many votes are needed to become new master after this master died in sentinel.conf

`sentinel monitor master 127.0.0.1 6379 1`

3. Start sentinel

```
[ec2-user@ip-172-31-33-9 myRedis]$ ../src/redis-sentinel sentinel.conf
                _._                                                  
           _.-``__ ''-._                                             
      _.-``    `.  `_.  ''-._           Redis 3.0.4 (00000000/0) 64 bit
  .-`` .-```.  ```\/    _.,_ ''-._                                   
 (    '      ,       .-`  | `,    )     Running in sentinel mode
 |`-._`-...-` __...-.``-._|'` _.-'|     Port: 26379
 |    `-._   `._    /     _.-'    |     PID: 16072
  `-._    `-._  `-./  _.-'    _.-'                                   
 |`-._`-._    `-.__.-'    _.-'_.-'|                                  
 |    `-._`-._        _.-'_.-'    |           http://redis.io        
  `-._    `-._`-.__.-'_.-'    _.-'                                   
 |`-._`-._    `-.__.-'    _.-'_.-'|                                  
 |    `-._`-._        _.-'_.-'    |                                  
  `-._    `-._`-.__.-'_.-'    _.-'                                   
      `-._    `-.__.-'    _.-'                                       
          `-._        _.-'                                           
              `-.__.-'                                               

16072:X 18 Feb 06:09:33.219 # WARNING: The TCP backlog setting of 511 cannot be enforced because /proc/sys/net/core/somaxconn is set to the lower value of 128.
16072:X 18 Feb 06:09:33.219 # Sentinel runid is 2fc52db767cc38cafcf4fddd4182ba1afcb7d89c
16072:X 18 Feb 06:09:33.219 # +monitor master master 127.0.0.1 6379 quorum 1
16072:X 18 Feb 06:09:33.220 * +slave slave 127.0.0.1:6381 127.0.0.1 6381 @ master 127.0.0.1 6379
16072:X 18 Feb 06:09:33.221 * +slave slave 127.0.0.1:6380 127.0.0.1 6380 @ master 127.0.0.1 6379
```

4. Now SHUTDOWN the master, master will switch automatically.

```
127.0.0.1:6379> SHUTDOWN
not connected> exit
```

We will see below messages in sentinel, current master is 6380

```
16072:X 18 Feb 06:12:18.651 # +sdown master master 127.0.0.1 6379
16072:X 18 Feb 06:12:18.651 # +odown master master 127.0.0.1 6379 #quorum 1/1
16072:X 18 Feb 06:12:18.651 # +new-epoch 1
16072:X 18 Feb 06:12:18.651 # +try-failover master master 127.0.0.1 6379
16072:X 18 Feb 06:12:18.652 # +vote-for-leader 2fc52db767cc38cafcf4fddd4182ba1afcb7d89c 1
16072:X 18 Feb 06:12:18.652 # +elected-leader master master 127.0.0.1 6379
16072:X 18 Feb 06:12:18.652 # +failover-state-select-slave master master 127.0.0.1 6379
16072:X 18 Feb 06:12:18.735 # +selected-slave slave 127.0.0.1:6380 127.0.0.1 6380 @ master 127.0.0.1 6379
16072:X 18 Feb 06:12:18.735 * +failover-state-send-slaveof-noone slave 127.0.0.1:6380 127.0.0.1 6380 @ master 127.0.0.1 6379
16072:X 18 Feb 06:12:18.812 * +failover-state-wait-promotion slave 127.0.0.1:6380 127.0.0.1 6380 @ master 127.0.0.1 6379
16072:X 18 Feb 06:12:19.707 # +promoted-slave slave 127.0.0.1:6380 127.0.0.1 6380 @ master 127.0.0.1 6379
16072:X 18 Feb 06:12:19.707 # +failover-state-reconf-slaves master master 127.0.0.1 6379
16072:X 18 Feb 06:12:19.762 * +slave-reconf-sent slave 127.0.0.1:6381 127.0.0.1 6381 @ master 127.0.0.1 6379
16072:X 18 Feb 06:12:20.724 * +slave-reconf-inprog slave 127.0.0.1:6381 127.0.0.1 6381 @ master 127.0.0.1 6379
16072:X 18 Feb 06:12:20.724 * +slave-reconf-done slave 127.0.0.1:6381 127.0.0.1 6381 @ master 127.0.0.1 6379
16072:X 18 Feb 06:12:20.814 # +failover-end master master 127.0.0.1 6379
16072:X 18 Feb 06:12:20.814 # +switch-master master 127.0.0.1 6379 127.0.0.1 6380
16072:X 18 Feb 06:12:20.814 * +slave slave 127.0.0.1:6381 127.0.0.1 6381 @ master 127.0.0.1 6380
```

We can see below info in slave 6380 and 6381. Current master is 6380 

```
127.0.0.1:6380> info replication
# Replication
role:master
connected_slaves:1
slave0:ip=127.0.0.1,port=6381,state=online,offset=10335,lag=1
master_repl_offset:10466
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:2
repl_backlog_histlen:10465
```

```
127.0.0.1:6381> info replication
# Replication
role:slave
master_host:127.0.0.1
master_port:6380
master_link_status:up
master_last_io_seconds_ago:2
master_sync_in_progress:0
slave_repl_offset:13287
slave_priority:100
slave_read_only:1
connected_slaves:0
master_repl_offset:0
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0
```

5. Now, restart the old master 6379, it will be a slave of 6380

```
16072:X 18 Feb 06:12:20.814 * +slave slave 127.0.0.1:6379 127.0.0.1 6379 @ master 127.0.0.1 6380
16072:X 18 Feb 06:12:50.831 # +sdown slave 127.0.0.1:6379 127.0.0.1 6379 @ master 127.0.0.1 6380
16072:X 18 Feb 06:18:56.492 # -sdown slave 127.0.0.1:6379 127.0.0.1 6379 @ master 127.0.0.1 6380
16072:X 18 Feb 06:19:06.440 * +convert-to-slave slave 127.0.0.1:6379 127.0.0.1 6379 @ master 127.0.0.1 6380
```

```
[ec2-user@ip-172-31-33-9 myRedis]$ redis-cli -p 6379

127.0.0.1:6379> info replication
# Replication
role:slave
master_host:127.0.0.1
master_port:6380
master_link_status:up
master_last_io_seconds_ago:2
master_sync_in_progress:0
slave_repl_offset:26835
slave_priority:100
slave_read_only:1
connected_slaves:0
master_repl_offset:0
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0
```

```
127.0.0.1:6380> info replication
# Replication
role:master
connected_slaves:2
slave0:ip=127.0.0.1,port=6381,state=online,offset=29366,lag=0
slave1:ip=127.0.0.1,port=6379,state=online,offset=29366,lag=1
master_repl_offset:29366
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:2
repl_backlog_histlen:29365
```


### a sentinel can monitor several masters

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
```

### you cannot write on a slave

```
127.0.0.1:6380> set hello world
(error) READONLY You can't write against a read only slave.
```


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

### Situation#2: If a slave SHUTDOWN, master and other slaves won't be influenced. This slave is no longer a slave after reboot unless you configure it in redis.conf

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

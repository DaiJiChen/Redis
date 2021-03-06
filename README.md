# Redis (REmote DIctionary Server)

A `distributed`, `in-memory` `key–value` database,

[Data_types.md](Data_Types.md)

[Redis_Config.md](Redis_Config.md)

[Persistence.md](persistence.md)

[Transactions.md](Transactions.md)

[Replication.md](Replication.md)


## 1. introduction

### NoSQL = Not Only SQL

 SQL support `ACID` (Atomicity, Consistency, Isolation, Durability)
 
 NoSQL support `CAP` (Consistency, Availibility, Partition tolerance)
 
 There are no relations between NoSQL data, so it is very easy to extend it.
 
 
 
### Types of NoSQL databases

NoSQL databases fall into four main categories:

1. Key value Stores — Riak, Voldemort, and Redis

2. Wide Column Stores — Cassandra and HBase.

3. Document databases — MongoDB

4. Graph databases — Neo4J and HyperGraphDB.

![4_kind_NoSQL.jpeg](/images/4_kind_NoSQL.jpeg)


### CAP (you can only select 2/3)

![CAP_Theorem](/images/CAP_Theorem.png)

#### For a distributed system, P must be selected

- CA: Relational database such as `Oracle`, `MySQL`
- CP: Choice of most large scaled Website such as Amazon, Taobao.
- AP: Redis, MongoDB


### BASE (Basically Available, Soft state, Eventual consistency).

**Solution for the 2/3 selection of CAP: Lower the requirement of Consistency to satisfy basically Availibility**

- Basically Available: Guarantees the availability of the data . There will be a response to any request (can be failure too).

- Soft state: The state of the system could change over time.

- Eventual consistency: The system will eventually become consistent once it stops receiving input.


## 2. Intro to Redis

A `distributed`, `in-memory` `key–value` database

1. Support persistence
2. Not only `key-value` string, also support `list`, `set`, `zset`, `hash`
3. Support backup: `master-slave model`

### all redis commands

redisdoc.com

### Install

1. check if you have gcc

2. run commands below
```
wget https://download.redis.io/releases/redis-6.0.10.tar.gz
tar xzf redis-6.0.10.tar.gz
cd redis-6.0.10
make
make install
```

#### start redis
```
cd usr/local/bin
redis-server
```

#### check running redis server
```
ps -ef|grep redis

# ps: process status
# -e: every process
# -f: full output format

```

#### connect to redis server
```
[ec2-user@ip-172-31-33-9 ~]$ redis-cli -p 6379

127.0.0.1:6379> ping
PONG

127.0.0.1:6379> SHUTDOWN

not connected> exit
```

#### kill redis mandatorily
```
pkill -9 redis
```


### other knowledge

![binFolder.jpg](/images/binFolder.jpg/)

#### see redis performance

There is a`redis-benchmark` file under usr/local/bin, we can run it to see the performance of redis on our machine

```
====== PING_INLINE ======
  100000 requests completed in 2.16 seconds
  50 parallel clients
  3 bytes payload
  keep alive: 1

99.90% <= 1 milliseconds
100.00% <= 1 milliseconds
46210.72 requests per second

====== PING_BULK ======
  100000 requests completed in 2.19 seconds
  50 parallel clients
  3 bytes payload
  keep alive: 1

99.84% <= 1 milliseconds
100.00% <= 1 milliseconds
45745.65 requests per second

====== SET ======
  100000 requests completed in 2.23 seconds
  50 parallel clients
  3 bytes payload
  keep alive: 1

98.15% <= 1 milliseconds
99.94% <= 2 milliseconds
99.94% <= 3 milliseconds
99.94% <= 4 milliseconds
99.95% <= 5 milliseconds
100.00% <= 5 milliseconds
44802.86 requests per second

====== GET ======
.
.
.
```

#### redis uses a single thread

Redis is, mostly, `a single-threaded server` from the POV of commands execution (actually modern versions of Redis use threads for different things). 

It is not designed to benefit from multiple CPU cores. People are supposed to launch several Redis instances to scale out on several cores if needed

#### redis default has 16 databases

```
[ec2-user@ip-172-31-33-9 redis-3.0.4]$ vim myRedis/redis.conf

# Set the number of databases. The default database is DB 0, you can select
# a different one on a per-connection basis using SELECT <dbid> where
# dbid is a number between 0 and 'databases'-1
databases 16
```

#### Switch database using `select n`

we can put different things in different DB

```
127.0.0.1:6379> select 15
OK
127.0.0.1:6379[15]>  # now we are using database 16
```

#### dbsize: size of data in current DB

```
127.0.0.1:6379[1]> dbsize
(integer) 0

127.0.0.1:6379[1]> set "k1" "Hello"
OK

127.0.0.1:6379[1]> dbsize
(integer) 1
```

#### flushdb: clear current dbid

#### flushAll: clear all DB

#### index in redis starts from 0

#### default port is 6379




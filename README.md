# Redis (REmote DIctionary Server)

A `distributed`, `in-memory` `key–value` database,

## 1. pre knowledge

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


### five data type in redis

Redis is `not a plain key-value store`

It is actually a `data structures server`, supporting different kinds of `values`.

1. `Binary-safe strings`: the string value in redis can support any data such as `HTML fragment` or `jpg file` or `serialized object`, it can be no more than 512M

2. `Lists`:(a linked list) collections of string elements sorted according to the order of insertion. They are basically linked lists.

3. `Sets`: collections of unique, unsorted string elements.

4. `Sorted sets`, similar to Sets but where every string element is associated to a floating number value, called score. The elements are always taken sorted by their score, so unlike Sets it is possible to retrieve a range of elements (for example you may ask: give me the top 10, or the bottom 10).

5. `Hashes`: which are maps composed of fields associated with values. Both the field and the value are strings. This is very similar java Map

Ablve 5 is common used data type

6. `Bit arrays (or simply bitmaps)`: it is possible, using special commands, to handle String values like an array of bits: you can set and clear individual bits, count all the bits set to 1, find the first set or unset bit, and so forth.

7. `HyperLogLogs`: this is a probabilistic data structure which is used in order to estimate the cardinality of a set. Don't be scared, it is simpler than it seems... See later in the HyperLogLog section of this tutorial.

8. `Streams`: append-only collections of map-like entries that provide an abstract log data type. They are covered in depth in the Introduction to Redis Streams.


### key

`keys *` return all keys in this DB

`exists key` check if a key exists

`move key db` move a key to a certain db`

`expire key seconds` set the expire time of a key

`ttl key` get the expire time of a key

`type key` type of value of a key

`del key` delete a key

```
127.0.0.1:6379[1]> keys *
1) "k2"
2) "k3"
3) "k1"

127.0.0.1:6379[1]> exists k1
(integer) 1

127.0.0.1:6379[1]> move k1 2
(integer) 1

127.0.0.1:6379[1]> keys *
1) "k2"
2) "k3"

127.0.0.1:6379[1]> expire k2 60
(integer) 1


127.0.0.1:6379[1]> ttl k2
(integer) 48

127.0.0.1:6379[1]> ttl k3
(integer) -1

127.0.0.1:6379[1]> ttl k2
(integer) -2

127.0.0.1:6379[1]> keys *
1) "k3"


127.0.0.1:6379[1]> type k3
string
```


### String type

`set` `get` `del` `append` `strlen` 

Used on number string: `incr key` `decr key` `incrby key N` `decrby key N`

Get substring of key: `getrange` `setrange` (inclusive in both left and right)

`setex`: set with expire time

`setnx`: set if not exist

`mset` `mget` `msetnx`: multiple set, multiple get

```
127.0.0.1:6379[1]> get k3
"v3"

127.0.0.1:6379[1]> append k3 12345
(integer) 7

127.0.0.1:6379[1]> get k3
"v312345"

127.0.0.1:6379[1]> strlen k3
(integer) 7
```

```
127.0.0.1:6379[1]> set k4 0
OK

127.0.0.1:6379[1]> incr k4
(integer) 1

127.0.0.1:6379[1]> get k4
"1"
```

```
127.0.0.1:6379[1]> getrange k3 0 3
"v312"

127.0.0.1:6379[1]> setrange k3 0 xxx
(integer) 7

127.0.0.1:6379[1]> get k3
"xxx2345"
```

```
127.0.0.1:6379[1]> setex k5 10 v5
OK

127.0.0.1:6379[1]> ttl k5
(integer) 5

127.0.0.1:6379[1]> setnx k3 v3  # failed
(integer) 0

127.0.0.1:6379[1]> get k3
"xxx2345" # value didn't change

127.0.0.1:6379[1]> setnx k6 v6 # succeed
(integer) 1

127.0.0.1:6379[1]> get k6
"v6"
```

```
127.0.0.1:6379[1]> mset k1 v1 k2 v2
OK

127.0.0.1:6379[1]> mget k1 k2
1) "v1"
2) "v2"
```

### list 

single key multiple value

if there is no value, key will also be deleted

linkelist, manipunate in head or tail is efficient

`lpush` `rpush` `lrange` : left push, right push, get data in range

`lpop` `rpop`: pop the left element, pop the right element

`lindex`: get elements according to index from left (redis index start from 0)

`llen`: get list length

`lrem value N` : delete a certain value for N times

`ltrim key N1 N2`: only reserve values from N1 to N2

`rpoplpush list1 list2`: rpop a element from list1 and lpush to list2

`linsert key before/after value value`: insert a value before/ater certain value

```
127.0.0.1:6379[1]> LPUSH list1 1 2 3 4 5
(integer) 5

127.0.0.1:6379[1]> LRANGE list1 0 -1
1) "5"
2) "4"
3) "3"
4) "2"
5) "1"

127.0.0.1:6379[1]> lpop list1
"5"

127.0.0.1:6379[1]> rpop list1
"1"

127.0.0.1:6379[1]> RPUSH list2 1 2 3 4 5
(integer) 5

127.0.0.1:6379[1]> LRANGE list2 0 -1
1) "1"
2) "2"
3) "3"
4) "4"
5) "5"
```

```
127.0.0.1:6379[1]> lpush l3 1 2 3 4 5
(integer) 5

127.0.0.1:6379[1]> ltrim l3 0 2
OK

127.0.0.1:6379[1]> lrange l3 0 -1
1) "5"
2) "4"
3) "3"
```

```
127.0.0.1:6379[1]> lrange l3 0 -1
1) "5"
2) "4"
3) "3"

127.0.0.1:6379[1]> linsert l3 before 4 java
(integer) 4

127.0.0.1:6379[1]> lrange l3 0 -1
1) "5"
2) "java"
3) "4"
4) "3"
```

### Set

`sadd` `smumbers` `sismumber`: add element, get all numbers, check if a value exist
`scard`: get set size
`srem key value` delete 
`srandmember key`: select several item randomly
`spop key` pop one item randomly
`smove key1 key2`

`sdiff` `sinter` `sunion`


```
127.0.0.1:6379[1]> sadd set1 1 1 2 2 3 3
(integer) 3

127.0.0.1:6379[1]> smembers set1
1) "1"
2) "2"
3) "3"

127.0.0.1:6379[1]> sismember set1 1
(integer) 1

127.0.0.1:6379[1]> sismember set1 4
(integer) 0

127.0.0.1:6379[1]> scard set1
(integer) 3

127.0.0.1:6379[1]> srem set1 3
(integer) 1
127.0.0.1:6379[1]> smembers set1
1) "1"
2) "2"

127.0.0.1:6379[1]> sadd set2 1 2 3 4 5 6 7 8
(integer) 8
127.0.0.1:6379[1]> srandmember set2 3
1) "5"
2) "4"
3) "3"
127.0.0.1:6379[1]> srandmember set2 3
1) "5"
2) "6"
3) "4"

127.0.0.1:6379[1]> spop set2
"2"
127.0.0.1:6379[1]> spop set2
"5"

127.0.0.1:6379[1]> smove set1 set2 4
(integer) 0
127.0.0.1:6379[1]> smembers set2
1) "1"
2) "3"
3) "4"
4) "6"
5) "7"
6) "8"
```
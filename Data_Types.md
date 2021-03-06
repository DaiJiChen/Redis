#  data type in redis

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


### Hash

`hset` `hget` `hmset` `hmget`  `hgetall` `hdel`

`hlen`

`hexist key`

`hkeys` `hvals`

`hincrby` `hincrbyfloat`

`hsetnx`: set if not exist

```
127.0.0.1:6379> hset user id 11
(integer) 1

127.0.0.1:6379> hget user id
"11"

127.0.0.1:6379> hmset user name jichen age 25
OK

127.0.0.1:6379> hmget user name id age
1) "jichen"
2) "11"
3) "25"

127.0.0.1:6379> hgetall user
1) "id"
2) "11"
3) "name"
4) "jichen"
5) "age"
6) "25"

127.0.0.1:6379> hdel user name
(integer) 1

127.0.0.1:6379> hlen user
(integer) 2

127.0.0.1:6379> hexists user age
(integer) 1

127.0.0.1:6379> hkeys user
1) "id"
2) "age"

127.0.0.1:6379> hvals user
1) "11"
2) "25"

127.0.0.1:6379> hincrby user age 3
(integer) 28
```

### ZSet

- set: k1 v1 v2

- Zset: k1 score1 v1 svore2 v2

#### commands

`zadd` 

`zrange`

`zrangebyscore`

	- no parameter: left include, right include
	- `(` : not include
	- withscores: print score
	- limit n1 n2: start from index n1, only return n2 records
	
`zremkey` 

`zcard`: return size

`zcount key score1 score2` 

`zrank key value` get rank of a certain value

`zscore key value` get score of a certain value

`zrevrange`

`zrevrangebyscore`

```
127.0.0.1:6379> zadd zset1 60 v1 70 v2 80 v3 90 v4
(integer) 4

127.0.0.1:6379> zrange zset1 0 -1
1) "v1"
2) "v2"
3) "v3"
4) "v4"

127.0.0.1:6379> zrange zset1 0 -1 withscores
1) "v1"
2) "60"
3) "v2"
4) "70"
5) "v3"
6) "80"
7) "v4"
8) "90"
```

```
127.0.0.1:6379> zrangebyscore zset1 60 80
1) "v1"
2) "v2"
3) "v3"

127.0.0.1:6379> zrangebyscore zset1 (60 (80
1) "v2"

127.0.0.1:6379> zrangebyscore zset1 60 80 withscores
1) "v1"
2) "60"
3) "v2"
4) "70"
5) "v3"
6) "80"

127.0.0.1:6379> zrangebyscore zset1 60 80 limit 0 2
1) "v1"
2) "v2"
```

```
127.0.0.1:6379> zrem zset1 v4
(integer) 1

127.0.0.1:6379> zcard set01
(integer) 0

127.0.0.1:6379> zcount zset1 60 80
(integer) 3

127.0.0.1:6379> zrank zset1 v1
(integer) 0

127.0.0.1:6379> zscore zset1 v1
"60"
```

```
127.0.0.1:6379> zrange zset1 0 -1
1) "v1"
2) "v2"
3) "v3"

127.0.0.1:6379> zrevrange zset1 0 -1
1) "v3"
2) "v2"
3) "v1"

127.0.0.1:6379> zrangebyscore zset1 60 70
1) "v1"
2) "v2"

127.0.0.1:6379> zrevrangebyscore zset1 70 60
1) "v2"
2) "v1"
```

# Redis Persistence

[RDB](RDB)

[AOF](AOF)
    
Redis provides a different range of persistence options:
- The RDB persistence performs point-in-time snapshots of your dataset at specified intervals.
- The AOF persistence logs every write operation received by the server, that will be played again at server startup, reconstructing the original dataset. Commands are logged using the same format as the Redis protocol itself, in an append-only fashion. Redis is able to rewrite the log in the background when it gets too big.
- If you wish, you can disable persistence completely, if you want your data to just exist as long as the server is running.
- It is possible to combine both AOF and RDB in the same instance. Notice that, in this case, when Redis restarts the AOF file will be used to reconstruct the original dataset since it is guaranteed to be the most complete.

**The most important thing to understand is the different trade-offs between the `RDB` and `AOF` persistence**

[more on redis.io/topics/persistence](https://redis.io/topics/persistence)

## RDB(Redis DataBase)

Redis will fork a child process  for persistence, it first write data into a temporary file, and replace the original persistence file with this temporary file after the persistence process is finished.

Since the main process don't do any I/O operation, the efficiency is very high.

The disadvantage is that you may lost the data of last persistence. And fork() can be time consuming if the dataset is big

The advantage is that RDB is suitable for recovering large amount of data compared to AOF. It is very good for disaster recorvey.

### databasse filename

dump.rdb

### redis.conf -> SNAPSHOTTING

```
# Save the DB on disk:
#
#   save <seconds> <changes>
#
#   Will save the DB if both the given number of seconds and the given
#   number of write operations against the DB occurred.
#
#   In the example below the behaviour will be to save:
#   after 900 sec (15 min) if at least 1 key changed
#   after 300 sec (5 min) if at least 10 keys changed
#   after 60 sec if at least 10000 keys changed
#
#   Note: you can disable saving completely by commenting out all "save" lines.
#
#   It is also possible to remove all the previously configured save
#   points by adding a save directive with a single empty string argument
#   like in the following example:
#
#   save ""

save 900 1
save 300 10
save 60 10000

```

### `save` and `bgsave` command: update dump.rdb immediately

### `FLUSHALL` command will also update dump.rdb immediately, but the file is empty.

### `stop-writes-on-bgsave-error yes` 

disable write if backup encountered an error

### How to do recovery

move your backuped dump.rdb (ususally in other palce or another machine) to the install dir(e.g. opt/redis-3.0.4)





## AOF(Append Only File)

log all the write commands to a file. When reconstruction, redis will read and execute loged commands one by one.

### aof file: appendonly.aof

### redis.conf -> APPEND ONLD MODE

```
############################## APPEND ONLY MODE ###############################

# By default Redis asynchronously dumps the dataset on disk. This mode is
# good enough in many applications, but an issue with the Redis process or
# a power outage may result into a few minutes of writes lost (depending on
# the configured save points).
#
# The Append Only File is an alternative persistence mode that provides
# much better durability. For instance using the default data fsync policy
# (see later in the config file) Redis can lose just one second of writes in a
# dramatic event like a server power outage, or a single write if something
# wrong with the Redis process itself happens, but the operating system is
# still running correctly.
#
# AOF and RDB persistence can be enabled at the same time without problems.
# If the AOF is enabled on startup Redis will load the AOF, that is the file
# with the better durability guarantees.
#
# Please check http://redis.io/topics/persistence for more information.

appendonly no
```

### Three different synchronize stratege

```
# appendfsync always
appendfsync everysec
# appendfsync no
```

### when start redis, it will first read appendonly.aof. It there is no appendonly.aof, it will read dump.rdb

### rewrite, use command `bgrewriteaof`

When to rewrite: when the size of current AOF file is 2 times larger than the size after last rewrite and size > 64M

```
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb
```

fork(): a new process is forked to do rewrite

### advantage: can sync very frequently ( always, everysec)

### disadvantage: aof file is larger, and recovery is much slower.

## Trade Off

**Official document**

```
The general indication is that you should use both persistence methods if you want a degree of data safety 
comparable to what PostgreSQL can provide you.
```
```
If you care a lot about your data, but still can live with a few minutes 
of data loss in case of disasters, you can simply use RDB alone.
```
```
There are many users using AOF alone, but we discourage it since to have an RDB snapshot 
from time to time is a great idea for doing database backups, for faster restarts,
and in the event of bugs in the AOF engine.
```
```
Note: for all these reasons we'll likely end up unifying AOF and RDB into 
a single persistence model in the future (long term plan).
```


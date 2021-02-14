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



## Trade Off

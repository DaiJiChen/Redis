# Transaction

A group of commands to be executed in a single isolated operation.

- All the commands in a transaction are serialized and executed sequentially.
- either all of the commands or none are processed.(Some may fail and some may success, there is no rollback)

https://redis.io/topics/transactions

## Usage

A Redis transaction is entered using the MULTI command. 
The command always replies with OK. At this point the user can issue multiple commands. 
Instead of executing these commands, Redis will queue them. All the commands are executed once EXEC is called.

Calling DISCARD instead will flush the transaction queue and will exit the transaction.

The following example increments keys foo and bar atomically.

```
> MULTI
OK
> INCR foo
QUEUED
> INCR bar
QUEUED
> EXEC
1) (integer) 1
2) (integer) 1
```

### Errors inside a transaction

During a transaction it is possible to encounter two kind of command errors:

A command may fail to be queued, so there may be an error before EXEC is called. For instance the command may be syntactically wrong (wrong number of arguments, wrong command name, ...), or there may be some critical condition like an out of memory condition.

A command may fail after EXEC is called, for instance since we performed an operation against a key with the wrong value (like calling a list operation against a string value).

### Why Redis does not support roll backs?

Different from relational database, redis commands can fail during a transaction, but still execute the rest of the transaction instead of rolling back.

**Because**: redis is simplified and faster because it does not need to roll back.

## watch

WATCH is used to provide a check-and-set (CAS) behavior to Redis transactions.

WATCHed keys are monitored in order to detect changes against them. If at least one watched key is modified before the EXEC command, the whole transaction aborts, and EXEC returns a Null reply to notify that the transaction failed.

### Optimistic lock / Pessimistic lock

Optimistic locking is when you check if the record was updated by someone else before you commit the transaction.

Pessimistic locking is when you take an exclusive lock so that no one else can start modifying the record.

```
127.0.0.1:6379> set balance 100
OK

127.0.0.1:6379> watch balance
OK

127.0.0.1:6379> multi
OK
127.0.0.1:6379> decrby balance 20
QUEUED
127.0.0.1:6379> decrby balance 30
QUEUED
127.0.0.1:6379> exec
1) (integer) 80
2) (integer) 50

127.0.0.1:6379> get balance
"50"
```

## three stage of transaction:

1. start: MULTI
2. enqueue
3. execute: EXEC

### Three properties of Transaction

1. A transaction is a single oeration, commands is serialized, executed in sequence.
2. There is **no isolation level** in redis
3. Atomic is not guaranteed, if one command of a transaction failed, other commands will still be executed, there is no rollback.

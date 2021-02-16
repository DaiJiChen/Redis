# Transaction

A group of commands to be executed in a single isolated operation.

- All the commands in a transaction are serialized and executed sequentially.
- A Redis transaction is also atomic, either all of the commands or none are processed

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


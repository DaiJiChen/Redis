# master-slave replication

master-slave replication allows replica Redis instances to be exact copies of master instances. The replica will automatically reconnect to the master every time the link breaks, and will attempt to be an exact copy of it regardless of what happens to the master.

Master is mainly responsible for write  
Slave is mainly responsible for read

### usefulness：  
1. Read/Write Spliting
2. Backup and Disaster Recovery

https://redis.io/topics/replication




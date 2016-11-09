# Team3-MongoDB


## Introduction
MongoDB benchmarking measures the performance of different data modeling with different set of nodes and clients. With comparison of different data modeling, this allows us to find out the optimized database schema design for MongoDB.

## Instructions
### 1. Install MongoDB RHEL 7 (>=3.2.0) in temp folder
```
cd /temp
wget https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-rhel70-3.2.10.tgz
tar zxvf mongodb-linux-x86_64-rhel70-3.2.10.tgz
```

### 2. Install Maven(>=3.3.9) in temp folder
```
cd /temp
wget http://download.nus.edu.sg/mirror/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
tar xzvf apache-maven-3.3.9-bin.tar.gz
```
### 3. Create / Configure .bash_profile file
```
cd
vim .bash_profile
```
Add these lines into .bash_profile file:
```
export PATH=/temp/apache-maven-3.3.9/bin:$PATH
export LANG=en_US.utf-8
export LC_ALL=en_US.utf-8
```
Save:
```
shift + z + z
source .bash_profile
```
### 4. Starting MongoDB server using screen
If you are starting server for first time, create a data folder in /temp:
```
mkdir /temp/data/team3
```
Start mongo server and exit screen:
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
screen
./mongod --dbpath /temp/data
ctrl+A, D
```
### 5. Setting up configuration server and query router for Three replica cluster
a. Create replica set folder
```
mkdir /temp/data
```
b. Start the configuration server 
```
./mongod --configsvr --replSet "team3" --dbpath /temp/data/team3

--------------- old: ./mongod --shardsvr --replSet "team3" --dbpath /temp/data/team3
./mongos --configdb 192.168.48.225:21000,192.168.48.226:21000,192.168.48.227:21000 --port 20000
./mongod --shardsvr --replSet shard1 --port 22001 --dbpath /temp/data/shard1 
./mongod --shardsvr --replSet shard2 --port 22002 --dbpath /temp/data/shard2 
./mongod --shardsvr --replSet shard3 --port 22003 --dbpath /temp/data/shard3
```
c. Connect to one of the config servers. ([X] = server number)
```
./mongo --host xcnd[X].comp.nus.edu.sg --port 27019
```
d. Initiate the replica set. ([X] = server number)
```
rs.initiate(
  {
    _id : "team3",
    members: [
      { _id : 6, host : "xcnd6.comp.nus.edu.sg:27019" },
      { _id : 7, host : "xcnd7.comp.nus.edu.sg:27019" },
      { _id : 8, host : "xcnd8.comp.nus.edu.sg:27019" }
    ]
  }
)
```
e. Check the status of connection. There should be three members.
```
rs.status()
```
f. Connect mongos to the cluster
```
./mongos --configdb team3/xcnd6.comp.nus.edu.sg:27017,xcnd7.comp.nus.edu.sg:27017,xcnd8.comp.nus.edu.sg:27017
```
g. Connect to the mongos.
```
mongo --host xcnd[X].comp.nus.edu.sg --port 27017
```
h. Add Shards to the Cluster
```
sh.addShard("xcnd6.comp.nus.edu.sg:27017")
sh.addShard("xcnd7.comp.nus.edu.sg:27017")
sh.addShard("xcnd8.comp.nus.edu.sg:27017")
```

### 7. Project directory
Before running the scripts, make sure the project folder is uploaded into the home folder. Change directory to the project folder to prepare for benchmarking.
```
cd Team3-MongoDB 
```
### 8. Bulkload data
The benchmark.sh script requires 2 arguments that represents the type of dataset (D8 or D40) and number of clients. </br>
a) D8 datasets with 1 node, run `bash bulkload.sh 8 1`. </br>
b) D8 datasets with 3 nodes, run `bash bulkload.sh 8 3`. </br>
c) D40 datasets with 1 node, run `bash bulkload.sh 40 1`. </br>
d) D40 datasets with 3 nodes, run `bash bulkload.sh 40 3`. 

### 9. Run benchmark
The benchmark.sh script requires 2 arguments that represents the type of dataset (D8 or D40) and number of clients. </br>
a) D8 datasets with 10 clients, run `bash benchmark.sh 8 10`.</br>
b) D8 datasets with 20 clients, run `bash benchmark.sh 8 20`.</br>
c) D8 datasets with 40 clients, run `bash benchmark.sh 8 40`.</br>
d) D40 datasets with 10 clients, run `bash benchmark.sh 40 10`.</br>
e) D40 datasets with 20 clients, run `bash benchmark.sh 40 20`.</br>
f) D40 datasets with 40 clients, run `bash benchmark.sh 40 40`.</br>

### 10. Stop server
```
killall -9 mongo
killall -9 mongod
```

## References
https://docs.mongodb.com/manual/tutorial/deploy-replica-set/
https://docs.mongodb.com/manual/tutorial/deploy-shard-cluster/
https://www.digitalocean.com/community/tutorials/how-to-create-a-sharded-cluster-in-mongodb-using-an-ubuntu-12-04-vps

mongod --configsvr --dbpath /temp/data --port 21000


mongos  --configdb 192.168.48.225:21000,192.168.48.226:21000,192.168.48.227:21000  --port 20000

mongod --shardsvr --replSet shard1 --port 22001 --dbpath /temp/data/shard1
mongod --shardsvr --replSet shard2 --port 22002 --dbpath /temp/data/shard2
mongod --shardsvr --replSet shard3 --port 22003 --dbpath /temp/data/shard3

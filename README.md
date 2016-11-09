# Team3-MongoDB


## Introduction
MongoDB benchmarking measures the performance of different data modeling with different set of nodes and clients. With comparison of different data modeling, this allows us to find out the optimized database schema design for MongoDB.

## Installing instructions
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
## Running on single node
### 1. Create a data folder in /temp
```
mkdir /temp/single-node
```
### 2. Start mongodb server
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongod --dbpath /temp/single-node
```
### 3. Go to Project directory
Before running the scripts, make sure the project folder is uploaded into the home folder. Change directory to the project folder to prepare for benchmarking.
```
cd Team3-MongoDB 
```
### 4. Bulkload database
The benchmark.sh script requires 1 argument that represents the type of dataset (D8 or D40). </br>
a) D8 datasets, run `bash bulkload.sh 8`. </br>
c) D40 datasets, run `bash bulkload.sh 40`. </br>

### 5. Run benchmark
The benchmark.sh script requires 2 arguments that represents the type of dataset (D8 or D40) and number of clients. </br>
a) D8 datasets with 10 clients, run `bash benchmark.sh 8 10`.</br>
b) D8 datasets with 20 clients, run `bash benchmark.sh 8 20`.</br>
c) D8 datasets with 40 clients, run `bash benchmark.sh 8 40`.</br>
d) D40 datasets with 10 clients, run `bash benchmark.sh 40 10`.</br>
e) D40 datasets with 20 clients, run `bash benchmark.sh 40 20`.</br>
f) D40 datasets with 40 clients, run `bash benchmark.sh 40 40`.</br>

## Running with three nodes
### 1. Set up initial replica set (21000)
#### For each member of replica set:
a. Create replica set folder
```
mkdir /temp/rs-data
```
b. Start the mongodb server
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongod --replSet "rs-data" --dbpath /temp/rs-data --port 21000
```
#### Use one of the members:
c. Connect using the mongo shell ([X] = server number)
```
./mongo --host xcnd[X].comp.nus.edu.sg --port 21000
```
d. Initiate the replica set via the mongo shell. ([X] = server number)

Example: 

Using port number = 21000

hostname = {xcnd6.comp.nus.edu.sg, xcnd7.comp.nus.edu.sg, xcnd8.comp.nus.edu.sg}

```
rs.initiate(
  {
    _id : "rs-data",
    members: [
      { _id : 0, host : "xcnd6.comp.nus.edu.sg:21000" },
      { _id : 1, host : "xcnd7.comp.nus.edu.sg:21000" },
      { _id : 2, host : "xcnd8.comp.nus.edu.sg:21000" }
    ]
  }
)
```
e. Check the status of connection. There should be three members in the set.
```
rs.status()
```
### 2. Set-up configuration server (27019) and query router (27017)
#### For each member of replica set:
a. Create replica set folder
```
mkdir /temp/config_rs
```
b. Start the configuration server 
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongod --configsvr --replSet "config_rs" --dbpath /temp/config_rs --port 27019
```
#### Use one of the members:
c. Connect to config server via mongo shell. ([X] = server number)
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongo --host xcnd[X].comp.nus.edu.sg --port 27019
```
d. Initiate the replica set.

Example: 

Using port number = 27019

hostname = {xcnd6.comp.nus.edu.sg, xcnd7.comp.nus.edu.sg, xcnd8.comp.nus.edu.sg}
```
rs.initiate(
  {
    _id : "config_rs",
    members: [
      { _id : 6, host : "xcnd6.comp.nus.edu.sg:27019" },
      { _id : 7, host : "xcnd7.comp.nus.edu.sg:27019" },
      { _id : 8, host : "xcnd8.comp.nus.edu.sg:27019" }
    ]
  }
)
```
e. Check the status of connection. There should be three members in the set.
```
rs.status()
```
### 3. Sharding
#### Using the primary member:

Assuming hostnames = xcnd6.comp.nus.edu.sg, xcnd7.comp.nus.edu.sg, xcnd8.comp.nus.edu.sg

a. Connect mongos to the cluster using port# 27019
```
./mongos --configdb config_rs/xcnd6.comp.nus.edu.sg:27019,xcnd7.comp.nus.edu.sg:27019,xcnd8.comp.nus.edu.sg:27019
```
b. Connect to the mongos via mongo shell using port # 27017

Example: Primary member hostname = xcnd6.comp.nus.edu.sg

```
./mongo --host xcnd6.comp.nus.edu.sg --port 27017
```
c. Shard the initial replica set (21000) 
```
sh.addShard( "rs-data/xcnd6.comp.nus.edu.sg:21000,xcnd7.comp.nus.edu.sg:21000,xcnd8.comp.nus.edu.sg:21000" )
```
d. Check the status of the shard. There should be three hostnames in the 'shards' field.
```
sh.status()
```
### 4. Go to Project directory
Before running the scripts, make sure the project folder is uploaded into the home folder. Change directory to the project folder to prepare for benchmarking.
```
cd Team3-MongoDB
```
### 5. Bulkload data
The benchmark.sh script requires 1 argument that represents the type of dataset (D8 or D40). </br>
a) D8 datasets, run `bash bulkload.sh 8`. </br>
c) D40 datasets, run `bash bulkload.sh 40`. </br>

### 6. Sharding collections
#### Use primary member (Example: hostname = xcnd6.comp.nus.edu.sg)

a. Connect to mongos (27017) via mongo shell

```
./mongo --host xcnd6.comp.nus.edu.sg --port 27017
```
b. Enable sharding
```
sh.enableSharding("team3")
```
c. Add sharding keys
```
sh.shardCollection("team3.warehouseDistrict", {w_id: 1})
sh.shardCollection("team3.customer", { c_w_id: 1})
sh.shardCollection(“team3.stockItem”, { s_w_id : 1})
sh.shardCollection(“team3.orderOrderLine", { o_w_id: 1 })
```
### 7. Run benchmark
The benchmark.sh script requires 2 arguments that represents the type of dataset (D8 or D40) and number of clients. </br>
a) D8 datasets with 10 clients, run `bash benchmark.sh 8 10`.</br>
b) D8 datasets with 20 clients, run `bash benchmark.sh 8 20`.</br>
c) D8 datasets with 40 clients, run `bash benchmark.sh 8 40`.</br>
d) D40 datasets with 10 clients, run `bash benchmark.sh 40 10`.</br>
e) D40 datasets with 20 clients, run `bash benchmark.sh 40 20`.</br>
f) D40 datasets with 40 clients, run `bash benchmark.sh 40 40`.</br>

## Stop server
```
killall -9 mongo
killall -9 mongod
```

## References
https://docs.mongodb.com/manual/tutorial/deploy-replica-set/ <br>
https://docs.mongodb.com/manual/tutorial/deploy-shard-cluster/ <br>
https://www.digitalocean.com/community/tutorials/how-to-create-a-sharded-cluster-in-mongodb-using-an-ubuntu-12-04-vps <br>
https://docs.mongodb.com/v3.2/tutorial/convert-replica-set-to-replicated-shard-cluster/ <br>

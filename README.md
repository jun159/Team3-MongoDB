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
Paste these lines into .bash_profile file and save:
```
export PATH=/temp/apache-maven-3.3.9/bin:$PATH
export LANG=en_US.utf-8
export LC_ALL=en_US.utf-8
```
Source the bash_profile file.
```
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
cd
cd Team3-MongoDB 
```
### 4. Bulkload database
The benchmark.sh script requires 1 argument that represents the type of dataset (D8 or D40). </br>
a) D8 datasets, run `bash bulkload.sh 8`</br>
c) D40 datasets, run `bash bulkload.sh 40` </br>

### 5. Run benchmark
The benchmark.sh script requires 2 arguments that represents the type of dataset (D8 or D40) and number of clients. </br>
a) D8 datasets with 10 clients, run `bash benchmark.sh 8 10`</br>
b) D8 datasets with 20 clients, run `bash benchmark.sh 8 20`</br>
c) D8 datasets with 40 clients, run `bash benchmark.sh 8 40`</br>
d) D40 datasets with 10 clients, run `bash benchmark.sh 40 10`</br>
e) D40 datasets with 20 clients, run `bash benchmark.sh 40 20`</br>
f) D40 datasets with 40 clients, run `bash benchmark.sh 40 40`</br>

## Running with three nodes
### 1. Set up initial replica set (Port# 21000)
#### For each member of replica set:
##### a. Create replica set folder
```
mkdir /temp/rs-data
```
##### b. Start the mongodb server
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongod --replSet "rs-data" --dbpath /temp/rs-data --port 21000
```
#### Use one of the members:
##### c. Connect using the mongo shell
Change the hostname
```
./mongo --host <hostname of one member> --port 21000
```
Example:
```
./mongo --host xcnd6.comp.nus.edu.sg --port 21000
```
##### d. Initiate the replica set via the mongo shell.

Change the hostnames
```
rs.initiate(
  {
    _id : "rs-data",
    members: [
      { _id : 0, host : "<hostname 1>:21000" },
      { _id : 1, host : "<hostname 2>:21000" },
      { _id : 2, host : "<hostname 3>:21000" }
    ]
  }
)
```

Example: 

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
##### e. Check the status of connection. There should be three members in the set.
```
rs.status()
```
### 2. Set-up configuration server (Port# 27019) and query router (Port# 27017)
#### For each member of replica set:
##### a. Create replica set folder
```
mkdir /temp/config_rs
```
##### b. Start the configuration server 
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongod --configsvr --replSet "config_rs" --dbpath /temp/config_rs --port 27019
```
#### Use one of the members:
##### c. Connect to config server via mongo shell.
Change the hostnames
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongo --host <hostname of primary member> --port 27019
```
Example:
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongo --host xcnd6.comp.nus.edu.sg --port 27019
```
##### d. Initiate the replica set.
```
rs.initiate(
  {
    _id : "config_rs",
    members: [
      { _id : 6, host : "<hostname 1>:27019" },
      { _id : 7, host : "<hostname 2>:27019" },
      { _id : 8, host : "<hostname 3>:27019" }
    ]
  }
)
```

Example: 

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
##### e. Check the status of connection. There should be three members in the set.
```
rs.status()
```
### 3. Sharding
#### Using Primary member:

##### a. Connect mongos to the cluster (Port# 27019)

Change the hostnames
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongos --configdb config_rs/<hostname 1>:27019,<hostname 2>:27019,<hostname 3>:27019
```
Example:

hostname = {xcnd6.comp.nus.edu.sg, xcnd7.comp.nus.edu.sg, xcnd8.comp.nus.edu.sg}

```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongos --configdb config_rs/xcnd6.comp.nus.edu.sg:27019,xcnd7.comp.nus.edu.sg:27019,xcnd8.comp.nus.edu.sg:27019
```
##### b. Connect to the mongos via mongo shell (Port# 27017)

Use the primary member as the hostname
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongo --host <hostname of primary member> --port 27017
```

Example: Primary member hostname = xcnd6.comp.nus.edu.sg

```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongo --host xcnd6.comp.nus.edu.sg --port 27017
```
##### c. Add shards using the hostnames of the three members in the initial replica set (Port# 21000) 
```
sh.addShard( "rs-data/<hostname 1>:21000" )
sh.addShard( "rs-data/<hostname 2>:21000" )
sh.addShard( "rs-data/<hostname 3>:21000" )
```
Example:
```
sh.addShard( "rs-data/xcnd6.comp.nus.edu.sg:21000" )
sh.addShard( "rs-data/xcnd7.comp.nus.edu.sg:21000" )
sh.addShard( "rs-data/xcnd8.comp.nus.edu.sg:21000" )
```
##### d. Check the status of the shard. There should be three hostnames in the 'shards' field.
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
a) D8 datasets, run `bash bulkload.sh 8`</br>
c) D40 datasets, run `bash bulkload.sh 40`</br>

### 6. Sharding collections (Choose option 1 or 2)

#### Option 1: Using shard.sh
##### a. Go to Project directory
```
cd Team3-MongoDB
```
##### b. Run shard script
Specify hostname of the primary member as first argument
```
bash shard.sh <hostname of primary member>
```
Example:
```
bash shard.sh xcnd6.comp.nus.edu.sg
```

#### Option 2: Manually

##### a. Connect to mongos (Port# 27017) via mongo shell
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongo --host <hostname of primary member> --port 27017 team3
```

Example: hostname of primary member = xcnd6.comp.nus.edu.sg
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongo --host xcnd6.comp.nus.edu.sg --port 27017 team3
```
##### b. Enable sharding
```
sh.enableSharding("team3")
```
##### c. Add sharding keys
```
sh.shardCollection("team3.warehouseDistrict", {w_id: 1})
sh.shardCollection("team3.customer", { c_w_id: 1, c_d_id: 1, c_id: 1 })
sh.shardCollection("team3.stockItem", { s_w_id : 1, s_i_id: 1})
sh.shardCollection("team3.orderOrderLine", { o_w_id: 1, o_d_id: 1, o_id: 1 })
sh.shardCollection("team3.warehouse", { w_id: 1 })
sh.shardCollection("team3.order", { o_w_id: 1, o_d_id: 1, o_id: 1 })
sh.shardCollection("team3.stock", { s_w_id : 1, s_i_id: 1 })
```
##### d. Run balancer to partition the data:
```
sh.startBalancer()
```
##### e. Check the status of the shards.
```
sh.status()
```
In the databases, there should be shard keys and different indicated chunks for each collection.

### 7. Run benchmark
The benchmark.sh script requires 2 arguments that represents the type of dataset (D8 or D40) and number of clients. </br>
```
cd Team3-MongoDB
```
a) D8 datasets with 10 clients, run `bash benchmark.sh 8 10`</br>
b) D8 datasets with 20 clients, run `bash benchmark.sh 8 20`</br>
c) D8 datasets with 40 clients, run `bash benchmark.sh 8 40`</br>
d) D40 datasets with 10 clients, run `bash benchmark.sh 40 10`</br>
e) D40 datasets with 20 clients, run `bash benchmark.sh 40 20`</br>
f) D40 datasets with 40 clients, run `bash benchmark.sh 40 40`</br>

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
http://www.mongodbspain.com/en/2015/03/03/two-steps-to-shard-a-mongodb-collection/ <br>

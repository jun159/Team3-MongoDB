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
mkdir /temp/data
```
Start mongo server and exit screen:
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
screen
./mongod --dbpath /temp/data
ctrl+A, D
```
### 5. Create Replica set
Create a replica set folder
```
mkdir /temp/data/rs
```
Set the name of replica set
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
./mongod --replSet "rs" --dbpath /temp/data/rs
```
Initiate the replica set and verify configuration
```
./mongo
rs.initiate()
```
Add remaining members into replica set

EXAMPLE: Servers = {xcnd6, xcnd7, xcnd8}, add via mongo shell

xcnd6 server:
```
rs.add("xcnd7.comp.nus.edu.sg:27017")
rs.add("xcnd8.comp.nus.edu.sg:27017")
```
xcnd7 server:
```
rs.add("xcnd6.comp.nus.edu.sg:27017")
rs.add("xcnd8.comp.nus.edu.sg:27017")
```
xcnd8 server:
```
rs.add("xcnd6.comp.nus.edu.sg:27017")
rs.add("xcnd7.comp.nus.edu.sg:27017")
```
Check the status of connection:
```
rs.status()
```

### 6. Create the Shard Replica Sets
Specify the replSet and --shardsvr parameters
```
cd /temp/mongodb-linux-x86_64-rhel70-3.2.10/bin
mongod --shardsvr --replSet "rs"
```
Connect to a member of the shard replica set via mongo shell
```
rs.initiate()
```
Connect a mongos to the cluster
```
./mongos --configdb rs/xcnd6.comp.nus.edu.sg:27017,xcnd7.comp.nus.edu.sg:27017,xcnd8.comp.nus.edu.sg:27017
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
